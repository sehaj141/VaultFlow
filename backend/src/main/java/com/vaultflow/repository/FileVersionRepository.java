package com.vaultflow.repository;

import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, UUID> {
    List<FileVersion> findByFileOrderByVersionNumberDesc(FileItem file);
    Optional<FileVersion> findByFileAndVersionNumber(FileItem file, Integer versionNumber);
    Optional<FileVersion> findByIdAndFile(UUID id, FileItem file);
}
