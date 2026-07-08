package com.vaultflow.repository;

import com.vaultflow.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findByUserIdAndParentIdAndDeletedAtIsNull(UUID userId, UUID parentId);

    // top-level folders (parentId IS NULL) need a separate method since JPA derived queries
    // can't express "parentId = null" via a parameter cleanly
    List<Folder> findByUserIdAndParentIdIsNullAndDeletedAtIsNull(UUID userId);

    Optional<Folder> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndParentIdAndNameIgnoreCase(UUID userId, UUID parentId, String name);

    boolean existsByUserIdAndParentIdIsNullAndNameIgnoreCase(UUID userId, String name);

    /**
     * Returns true if `candidateAncestorId` is the same as, or an ancestor of, `folderId`.
     * Used to block moves that would create a cycle (moving a folder into its own descendant).
     */
    @Query(value = """
        WITH RECURSIVE ancestors AS (
            SELECT id, parent_id FROM folders WHERE id = :folderId
            UNION ALL
            SELECT f.id, f.parent_id
            FROM folders f
            INNER JOIN ancestors a ON f.id = a.parent_id
        )
        SELECT EXISTS (SELECT 1 FROM ancestors WHERE id = :candidateAncestorId)
        """, nativeQuery = true)
    boolean isSameOrAncestor(@Param("folderId") UUID folderId, @Param("candidateAncestorId") UUID candidateAncestorId);

    /**
     * Full breadcrumb trail from root down to the given folder, inclusive.
     */
    @Query(value = """
        WITH RECURSIVE trail AS (
            SELECT id, name, parent_id, 0 AS depth FROM folders WHERE id = :folderId
            UNION ALL
            SELECT f.id, f.name, f.parent_id, t.depth + 1
            FROM folders f
            INNER JOIN trail t ON f.id = t.parent_id
        )
        SELECT id, name FROM trail ORDER BY depth DESC
        """, nativeQuery = true)
    List<Object[]> findBreadcrumbTrail(@Param("folderId") UUID folderId);
    @Query("SELECT COUNT(f) FROM Folder f WHERE f.userId = :userId AND f.deletedAt IS NULL")
long countActiveFoldersByUserId(@Param("userId") UUID userId);
}