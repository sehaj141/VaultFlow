package com.vaultflow.repository.specification;

import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileSpecification {

    public static Specification<FileItem> getSearchSpecification(
            User user,
            String query,
            String extension,
            UUID folderId,
            Instant startDate,
            Instant endDate,
            Long minSizeBytes,
            Long maxSizeBytes
    ) {
        return (root, queryCriteria, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory User Scoping
            predicates.add(cb.equal(root.get("user"), user));

            // 2. Exclude Trashed Items
            predicates.add(cb.equal(root.get("isTrashed"), false));

            // 3. Filename ILIKE partial match
            if (query != null && !query.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("originalName")), "%" + query.trim().toLowerCase() + "%"));
            }

            // 4. Exact Extension Filter
            if (extension != null && !extension.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("extension")), extension.trim().toLowerCase()));
            }

            // 5. Folder Scope
            if (folderId != null) {
                predicates.add(cb.equal(root.get("folder").get("id"), folderId));
            }

            // 6. Date Range Filters
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            // 7. Size Predicates
            if (minSizeBytes != null && minSizeBytes > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sizeBytes"), minSizeBytes));
            }
            if (maxSizeBytes != null && maxSizeBytes > 0) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sizeBytes"), maxSizeBytes));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<FileItem> getSearchSpecification(
            User user,
            String query,
            String extension,
            UUID folderId,
            Instant startDate,
            Instant endDate
    ) {
        return getSearchSpecification(user, query, extension, folderId, startDate, endDate, null, null);
    }
}
