package com.vaultflow.repository;

import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileItem, UUID>, JpaSpecificationExecutor<FileItem> {
    Optional<FileItem> findByIdAndUser(UUID id, User user);

    @Query("SELECT f FROM FileItem f WHERE f.user = :user AND (:folderId IS NULL AND f.folder IS NULL OR f.folder.id = :folderId) AND f.isTrashed = false")
    List<FileItem> findByUserAndFolderIdAndIsTrashedFalse(@Param("user") User user, @Param("folderId") UUID folderId);

    List<FileItem> findByUserAndIsTrashedTrue(User user);

    List<FileItem> findByUserAndIsTrashedFalse(User user);

    long countByUserAndIsTrashedFalse(User user);

    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM FileItem f WHERE f.user = :user AND f.isTrashed = false")
    Long sumSizeBytesByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM FileItem f WHERE f.user = :user AND f.isTrashed = false")
    Long getTotalStorageUsedByUser(@Param("user") User user);

    List<FileItem> findTop5ByUserAndIsTrashedFalseOrderByCreatedAtDesc(User user);
}
