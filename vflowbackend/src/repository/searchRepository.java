package com.vaultflow.repository;

import com.vaultflow.entity.FileEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SearchRepository {

    private final EntityManager entityManager;

    /**
     * Dynamic search query: combines full-text search (when a query is provided)
     * with zero or more optional filters (extension, date range, folder).
     *
     * We build this with EntityManager + native SQL rather than JPQL Criteria API
     * because tsvector/tsquery/ts_rank are Postgres-native constructs that JPQL
     * has no mapping for. The alternative is a QueryDSL or JOOQ dependency;
     * using EntityManager keeps the dependency footprint minimal.
     */
    @SuppressWarnings("unchecked")
    public List<FileEntity> search(
            UUID userId,
            String query,
            String extension,
            LocalDate uploadedAfter,
            LocalDate uploadedBefore,
            UUID folderId,
            int offset,
            int limit
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT f.id, f.name, f.extension, f.mime_type, f.size_bytes,
                   f.storage_path, f.user_id, f.folder_id, f.deleted_at,
                   f.created_at, f.updated_at
            FROM files f
            WHERE f.user_id = :userId
              AND f.deleted_at IS NULL
            """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (query != null && !query.isBlank()) {
            // plainto_tsquery converts a plain string ("machine learning pdf")
            // into a tsquery without requiring the caller to know tsquery syntax.
            // to_tsquery would require explicit operators: "machine & learning & pdf"
            sql.append(" AND f.search_vector @@ plainto_tsquery('english', :query)");
            params.put("query", query.trim());
        }

        if (extension != null && !extension.isBlank()) {
            sql.append(" AND LOWER(f.extension) = LOWER(:extension)");
            params.put("extension", extension.trim());
        }

        if (uploadedAfter != null) {
            sql.append(" AND f.created_at >= :uploadedAfter");
            params.put("uploadedAfter", uploadedAfter.atStartOfDay());
        }

        if (uploadedBefore != null) {
            sql.append(" AND f.created_at <= :uploadedBefore");
            params.put("uploadedBefore", uploadedBefore.plusDays(1).atStartOfDay());
        }

        if (folderId != null) {
            sql.append(" AND f.folder_id = :folderId");
            params.put("folderId", folderId);
        }

        // When a text query is present, sort by relevance (ts_rank) then recency.
        // When filtering only (no text), sort purely by recency.
        if (query != null && !query.isBlank()) {
            sql.append("""
                ORDER BY ts_rank(f.search_vector, plainto_tsquery('english', :query)) DESC,
                         f.created_at DESC
                """);
        } else {
            sql.append(" ORDER BY f.created_at DESC");
        }

        sql.append(" LIMIT :limit OFFSET :offset");
        params.put("limit", limit);
        params.put("offset", offset);

        Query nativeQuery = entityManager.createNativeQuery(sql.toString(), FileEntity.class);
        params.forEach(nativeQuery::setParameter);

        return nativeQuery.getResultList();
    }

    /**
     * Count query mirrors the search query without ORDER BY / LIMIT / OFFSET.
     * Required for computing totalPages in the paginated response.
     * We run this as a separate COUNT(*) query rather than fetching all rows —
     * the alternative (fetch everything, count in Java) would negate pagination entirely.
     */
    public long count(
            UUID userId,
            String query,
            String extension,
            LocalDate uploadedAfter,
            LocalDate uploadedBefore,
            UUID folderId
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM files f
            WHERE f.user_id = :userId
              AND f.deleted_at IS NULL
            """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (query != null && !query.isBlank()) {
            sql.append(" AND f.search_vector @@ plainto_tsquery('english', :query)");
            params.put("query", query.trim());
        }
        if (extension != null && !extension.isBlank()) {
            sql.append(" AND LOWER(f.extension) = LOWER(:extension)");
            params.put("extension", extension.trim());
        }
        if (uploadedAfter != null) {
            sql.append(" AND f.created_at >= :uploadedAfter");
            params.put("uploadedAfter", uploadedAfter.atStartOfDay());
        }
        if (uploadedBefore != null) {
            sql.append(" AND f.created_at <= :uploadedBefore");
            params.put("uploadedBefore", uploadedBefore.plusDays(1).atStartOfDay());
        }
        if (folderId != null) {
            sql.append(" AND f.folder_id = :folderId");
            params.put("folderId", folderId);
        }

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        params.forEach(nativeQuery::setParameter);

        return ((Number) nativeQuery.getSingleResult()).longValue();
    }
}