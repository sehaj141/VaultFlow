package com.vaultflow.repository;

import com.vaultflow.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import com.vaultflow.dto.response.FileTypeBreakdownResponse;
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    List<FileEntity> findByUserIdAndFolderIdAndDeletedAtIsNull(UUID userId, UUID folderId);

    List<FileEntity> findByUserIdAndFolderIdIsNullAndDeletedAtIsNull(UUID userId);

    Optional<FileEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndFolderIdAndNameIgnoreCase(UUID userId, UUID folderId, String name);

    boolean existsByUserIdAndFolderIdIsNullAndNameIgnoreCase(UUID userId, String name);

    List<FileEntity> findTop10ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId); // Phase 4 dashboard
    @Query("SELECT COUNT(f) FROM FileEntity f WHERE f.userId = :userId AND f.deletedAt IS NULL")
long countActiveFilesByUserId(@Param("userId") UUID userId);

@Query("""
    SELECT new com.vaultflow.dto.response.FileTypeBreakdownResponse(
        f.extension, COUNT(f), SUM(f.sizeBytes)
    )
    FROM FileEntity f
    WHERE f.userId = :userId AND f.deletedAt IS NULL
    GROUP BY f.extension
    ORDER BY COUNT(f) DESC
    """)
List<FileTypeBreakdownResponse> getFileTypeBreakdown(@Param("userId") UUID userId);
}