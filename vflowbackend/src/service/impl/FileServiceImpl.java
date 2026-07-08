package com.vaultflow.service.impl;

import com.vaultflow.constant.SupportedFileTypes;
import com.vaultflow.dto.request.MoveFileRequest;
import com.vaultflow.dto.request.RenameFileRequest;
import com.vaultflow.dto.response.DownloadUrlResponse;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.entity.FileEntity;
import com.vaultflow.entity.User;
import com.vaultflow.exception.*;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.service.FileService;
import com.vaultflow.service.StorageService;
import com.vaultflow.util.FileTypeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final FileTypeValidator fileTypeValidator;

    @Override
    @Transactional
    public FileResponse uploadFile(UUID userId, UUID folderId, MultipartFile file) {
        // 1. Size check — cheapest check first, fail fast before touching file contents
        if (file.getSize() > SupportedFileTypes.MAX_FILE_SIZE_BYTES) {
            throw new FileSizeLimitExceededException(
                    "File exceeds the 50MB limit (size: " + file.getSize() + " bytes)");
        }

        // 2. Real content-based type validation (not extension/Content-Type trust)
        String detectedMimeType = fileTypeValidator.detectAndValidateMimeType(file);
        String extension = SupportedFileTypes.ALLOWED_MIME_TO_EXTENSION.get(detectedMimeType);

        // 3. Folder ownership check, if uploading into a folder
        if (folderId != null) {
            folderRepository.findByIdAndUserId(folderId, userId)
                    .orElseThrow(() -> new FileNotFoundException("Target folder not found"));
        }

        // 4. Storage quota check
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        if (user.getStorageUsedBytes() + file.getSize() > user.getStorageLimitBytes()) {
            throw new StorageQuotaExceededException("Storage limit exceeded. Please free up space.");
        }

        // 5. Duplicate name check within the same folder
        String displayName = sanitizeFileName(file.getOriginalFilename());
        boolean nameTaken = folderId == null
                ? fileRepository.existsByUserIdAndFolderIdIsNullAndNameIgnoreCase(userId, displayName)
                : fileRepository.existsByUserIdAndFolderIdAndNameIgnoreCase(userId, folderId, displayName);

        if (nameTaken) {
            displayName = appendUniqueSuffix(displayName);
        }

        // 6. Generate a storage path that does NOT depend on the user-controlled filename
        UUID fileId = UUID.randomUUID();
        String storagePath = userId + "/" + fileId + "." + extension;

        try {
            storageService.upload(storagePath, file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to storage", e);
        }

        FileEntity entity = FileEntity.builder()
                .id(fileId)
                .name(displayName)
                .extension(extension)
                .mimeType(detectedMimeType)
                .sizeBytes(file.getSize())
                .storagePath(storagePath)
                .userId(userId)
                .folderId(folderId)
                .build();

        fileRepository.save(entity);

        // 7. Update running storage usage total (the denormalized counter from Phase 1)
        user.setStorageUsedBytes(user.getStorageUsedBytes() + file.getSize());
        userRepository.save(user);

        return toResponse(entity);
    }

    @Override
    public List<FileResponse> listFiles(UUID userId, UUID folderId) {
        List<FileEntity> files = folderId == null
                ? fileRepository.findByUserIdAndFolderIdIsNullAndDeletedAtIsNull(userId)
                : fileRepository.findByUserIdAndFolderIdAndDeletedAtIsNull(userId, folderId);

        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public DownloadUrlResponse getDownloadUrl(UUID userId, UUID fileId) {
        FileEntity file = getOwnedFileOrThrow(userId, fileId);
        int expirySeconds = 300; // 5 minutes — short-lived by design
        String url = storageService.generateSignedDownloadUrl(file.getStoragePath(), expirySeconds);
        return DownloadUrlResponse.builder().url(url).expiresInSeconds(expirySeconds).build();
    }

    @Override
    @Transactional
    public FileResponse renameFile(UUID userId, UUID fileId, RenameFileRequest request) {
        FileEntity file = getOwnedFileOrThrow(userId, fileId);

        boolean nameTaken = file.getFolderId() == null
                ? fileRepository.existsByUserIdAndFolderIdIsNullAndNameIgnoreCase(userId, request.getName())
                : fileRepository.existsByUserIdAndFolderIdAndNameIgnoreCase(userId, file.getFolderId(), request.getName());

        if (nameTaken && !file.getName().equalsIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("A file named '" + request.getName() + "' already exists here");
        }

        file.setName(request.getName());
        file = fileRepository.save(file);
        return toResponse(file);
    }

    @Override
    @Transactional
    public FileResponse moveFile(UUID userId, UUID fileId, MoveFileRequest request) {
        FileEntity file = getOwnedFileOrThrow(userId, fileId);
        UUID newFolderId = request.getNewFolderId();

        if (newFolderId != null) {
            folderRepository.findByIdAndUserId(newFolderId, userId)
                    .orElseThrow(() -> new FileNotFoundException("Destination folder not found"));
        }

        boolean nameTaken = newFolderId == null
                ? fileRepository.existsByUserIdAndFolderIdIsNullAndNameIgnoreCase(userId, file.getName())
                : fileRepository.existsByUserIdAndFolderIdAndNameIgnoreCase(userId, newFolderId, file.getName());

        if (nameTaken) {
            throw new IllegalArgumentException("A file with this name already exists in the destination");
        }

        file.setFolderId(newFolderId);
        file = fileRepository.save(file);
        return toResponse(file);
    }

    @Override
    @Transactional
    public void deleteFile(UUID userId, UUID fileId) {
        FileEntity file = getOwnedFileOrThrow(userId, fileId);

        // NOTE: hard delete for now. Phase 9 replaces this with soft-delete into Trash.
        storageService.delete(file.getStoragePath());
        fileRepository.delete(file);

        User user = userRepository.findById(userId).orElseThrow();
        user.setStorageUsedBytes(Math.max(0, user.getStorageUsedBytes() - file.getSizeBytes()));
        userRepository.save(user);
    }

    private FileEntity getOwnedFileOrThrow(UUID userId, UUID fileId) {
        return fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new FileNotFoundException("File not found"));
    }

    private String sanitizeFileName(String originalName) {
        if (originalName == null) return "untitled";
        // Strip path separators and control characters to prevent path traversal
        // if this name is ever used in any file-system-adjacent context.
        return originalName.replaceAll("[/\\\\\\x00-\\x1F]", "_").trim();
    }

    private String appendUniqueSuffix(String name) {
        int dotIndex = name.lastIndexOf('.');
        String base = dotIndex > 0 ? name.substring(0, dotIndex) : name;
        String ext = dotIndex > 0 ? name.substring(dotIndex) : "";
        return base + " (" + System.currentTimeMillis() % 10000 + ")" + ext;
    }

    private FileResponse toResponse(FileEntity file) {
        return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .extension(file.getExtension())
                .mimeType(file.getMimeType())
                .sizeBytes(file.getSizeBytes())
                .folderId(file.getFolderId())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
}