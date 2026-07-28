package com.vaultflow.repository;

import com.vaultflow.entity.SharedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SharedLinkRepository extends JpaRepository<SharedLink, UUID> {
    Optional<SharedLink> findByToken(String token);
    Optional<SharedLink> findByFileIdAndIsActiveTrue(UUID fileId);
    Optional<SharedLink> findByFolderIdAndIsActiveTrue(UUID folderId);
}
