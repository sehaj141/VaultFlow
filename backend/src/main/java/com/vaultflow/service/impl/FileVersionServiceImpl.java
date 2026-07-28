package com.vaultflow.service.impl;

import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.FileVersionResponse;
import com.vaultflow.dto.response.UserSummaryResponse;
import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.FileVersion;
import com.vaultflow.entity.User;
import com.vaultflow.exception.BadRequestException;
import com.vaultflow.exception.ResourceNotFoundException;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.FileVersionRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.service.FileVersionService;
import com.vaultflow.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileVersionServiceImpl implements FileVersionService {

    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public FileResponse uploadNewVersion(String userEmail, UUID fileId, MultipartFile file) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded version file is empty.");
        }

        List<FileVersion> existingVersions = fileVersionRepository.findByFileOrderByVersionNumberDesc(fileItem);
        int currentMaxVersion = existingVersions.isEmpty() ? 1 : existingVersions.get(0).getVersionNumber();

        // 1. Snapshot current active file state into FileVersion v(currentMaxVersion) if not already snapshotted
        if (existingVersions.isEmpty()) {
            FileVersion v1 = FileVersion.builder()
                    .file(fileItem)
                    .versionNumber(1)
                    .storedName(fileItem.getStoredName())
                    .storagePath(fileItem.getStoragePath())
                    .sizeBytes(fileItem.getSizeBytes())
                    .mimeType(fileItem.getMimeType())
                    .uploadedBy(fileItem.getUser())
                    .build();
            fileVersionRepository.save(v1);
        }

        // 2. Store new binary object
        int newVersionNumber = (existingVersions.isEmpty() ? 1 : currentMaxVersion) + 1;
        String uniqueId = UUID.randomUUID().toString();
        String newStoredName = user.getId() + "/" + uniqueId + "-v" + newVersionNumber + "." + fileItem.getExtension();
        String newStoragePath = storageService.store(file, newStoredName);

        // 3. Save new FileVersion entry v(newVersionNumber)
        FileVersion newVersion = FileVersion.builder()
                .file(fileItem)
                .versionNumber(newVersionNumber)
                .storedName(newStoredName)
                .storagePath(newStoragePath)
                .sizeBytes(file.getSize())
                .mimeType(file.getContentType() != null ? file.getContentType() : fileItem.getMimeType())
                .uploadedBy(user)
                .build();
        fileVersionRepository.save(newVersion);

        // 4. Update FileItem pointer to new version binary
        fileItem.setStoredName(newStoredName);
        fileItem.setStoragePath(newStoragePath);
        fileItem.setSizeBytes(file.getSize());
        if (file.getContentType() != null) {
            fileItem.setMimeType(file.getContentType());
        }

        FileItem updatedFile = fileRepository.save(fileItem);
        return mapToFileResponse(updatedFile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileVersionResponse> getVersionTimeline(String userEmail, UUID fileId) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        List<FileVersion> versions = fileVersionRepository.findByFileOrderByVersionNumberDesc(fileItem);
        return versions.stream()
                .map(this::mapToFileVersionResponse)
                .toList();
    }

    @Override
    @Transactional
    public FileResponse restoreVersion(String userEmail, UUID fileId, UUID versionId) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        FileVersion targetVersion = fileVersionRepository.findByIdAndFile(versionId, fileItem)
                .orElseThrow(() -> new ResourceNotFoundException("FileVersion", "id", versionId));

        // Restore fileItem pointers to target version
        fileItem.setStoredName(targetVersion.getStoredName());
        fileItem.setStoragePath(targetVersion.getStoragePath());
        fileItem.setSizeBytes(targetVersion.getSizeBytes());
        fileItem.setMimeType(targetVersion.getMimeType());

        FileItem restored = fileRepository.save(fileItem);
        return mapToFileResponse(restored);
    }

    @Override
    public Resource downloadVersion(String userEmail, UUID fileId, UUID versionId) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        FileVersion targetVersion = fileVersionRepository.findByIdAndFile(versionId, fileItem)
                .orElseThrow(() -> new ResourceNotFoundException("FileVersion", "id", versionId));

        return storageService.loadAsResource(targetVersion.getStoragePath());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private FileVersionResponse mapToFileVersionResponse(FileVersion v) {
        return FileVersionResponse.builder()
                .id(v.getId())
                .fileId(v.getFile().getId())
                .versionNumber(v.getVersionNumber())
                .sizeBytes(v.getSizeBytes())
                .formattedSize(formatFileSize(v.getSizeBytes()))
                .mimeType(v.getMimeType())
                .uploadedBy(UserSummaryResponse.builder()
                        .id(v.getUploadedBy().getId())
                        .fullName(v.getUploadedBy().getFullName())
                        .email(v.getUploadedBy().getEmail())
                        .role(v.getUploadedBy().getRole().name())
                        .build())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private FileResponse mapToFileResponse(FileItem fileItem) {
        return FileResponse.builder()
                .id(fileItem.getId())
                .originalName(fileItem.getOriginalName())
                .mimeType(fileItem.getMimeType())
                .extension(fileItem.getExtension())
                .sizeBytes(fileItem.getSizeBytes())
                .formattedSize(formatFileSize(fileItem.getSizeBytes()))
                .folderId(fileItem.getFolder() != null ? fileItem.getFolder().getId() : null)
                .isTrashed(fileItem.getIsTrashed())
                .createdAt(fileItem.getCreatedAt())
                .updatedAt(fileItem.getUpdatedAt())
                .build();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
