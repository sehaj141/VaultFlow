package com.vaultflow.repository;

import com.vaultflow.entity.Folder;
import com.vaultflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {
    Optional<Folder> findByIdAndUser(UUID id, User user);

    @Query("SELECT f FROM Folder f WHERE f.user = :user AND (:parentId IS NULL AND f.parent IS NULL OR f.parent.id = :parentId) AND f.isTrashed = false")
    List<Folder> findByUserAndParentIdAndIsTrashedFalse(@Param("user") User user, @Param("parentId") UUID parentId);

    List<Folder> findByUserAndParentIsNullAndIsTrashedFalse(User user);

    List<Folder> findByUserAndIsTrashedTrue(User user);

    long countByUserAndIsTrashedFalse(User user);

    boolean existsByUserAndParentAndName(User user, Folder parent, String name);

    boolean existsByUserAndParentIsNullAndName(User user, String name);

    @Query("SELECT f FROM Folder f WHERE f.user = :user AND f.path LIKE CONCAT(:pathPrefix, '/%') AND f.isTrashed = false")
    List<Folder> findAllDescendants(@Param("user") User user, @Param("pathPrefix") String pathPrefix);

    List<Folder> findTop5ByUserAndIsTrashedFalseOrderByCreatedAtDesc(User user);
}
