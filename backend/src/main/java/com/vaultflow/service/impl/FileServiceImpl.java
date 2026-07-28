package com.vaultflow.service.impl;

import com.vaultflow.dto.request.MoveFileRequest;
import com.vaultflow.dto.request.RenameFileRequest;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.entity.ActivityType;
import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.Folder;
import com.vaultflow.entity.User;
import com.vaultflow.event.ActivityEvent;
import com.vaultflow.exception.BadRequestException;
import com.vaultflow.exception.ResourceNotFoundException;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.repository.specification.FileSpecification;
import com.vaultflow.service.FileService;
import com.vaultflow.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "docx", "txt", "zip", "png", "jpg", "jpeg"
    );

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public FileResponse uploadFile(String userEmail, MultipartFile file, UUID folderId) {
        User user = getUserByEmail(userEmail);

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("Filename must not be empty.");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("File extension ." + extension + " is not supported. Allowed: " + ALLOWED_EXTENSIONS);
        }

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findByIdAndUser(folderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));
        }

        String uniqueId = UUID.randomUUID().toString();
        String storedName = user.getId() + "/" + uniqueId + "." + extension;
        String storagePath = storageService.store(file, storedName);

        FileItem fileItem = FileItem.builder()
                .originalName(originalFilename)
                .storedName(storedName)
                .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .extension(extension.toLowerCase())
                .sizeBytes(file.getSize())
                .storagePath(storagePath)
                .user(user)
                .folder(folder)
                .isTrashed(false)
                .build();

        FileItem saved = fileRepository.save(fileItem);

        eventPublisher.publishEvent(new ActivityEvent(
                this, user, ActivityType.FILE_UPLOADED, "FILE", saved.getId(), saved.getOriginalName(),
                "Uploaded file (" + formatFileSize(saved.getSizeBytes()) + ")"
        ));

        return mapToFileResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> searchFiles(String userEmail, String query, String extension, UUID folderId, Instant startDate, Instant endDate) {
        User user = getUserByEmail(userEmail);
        Specification<FileItem> spec = FileSpecification.getSearchSpecification(
                user, query, extension, folderId, startDate, endDate
        );
        List<FileItem> files = fileRepository.findAll(spec);
        return files.stream().map(this::mapToFileResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(String userEmail, UUID fileId) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        if (fileItem.getIsTrashed()) {
            throw new BadRequestException("Cannot download a trashed file.");
        }

        eventPublisher.publishEvent(new ActivityEvent(
                this, user, ActivityType.FILE_DOWNLOADED, "FILE", fileItem.getId(), fileItem.getOriginalName(), "Downloaded file binary"
        ));

        return storageService.loadAsResource(fileItem.getStoragePath());
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponse getFileMetadata(String userEmail, UUID fileId) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));
        return mapToFileResponse(fileItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> getFiles(String userEmail, UUID folderId) {
        User user = getUserByEmail(userEmail);
        List<FileItem> files = fileRepository.findByUserAndFolderIdAndIsTrashedFalse(user, folderId);
        return files.stream().map(this::mapToFileResponse).toList();
    }

    @Override
    @Transactional
    public FileResponse renameFile(String userEmail, UUID fileId, RenameFileRequest request) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        String oldName = fileItem.getOriginalName();
        fileItem.setOriginalName(request.getNewName().trim());
        FileItem updated = fileRepository.save(fileItem);

        eventPublisher.publishEvent(new ActivityEvent(
                this, user, ActivityType.FILE_RENAMED, "FILE", updated.getId(), updated.getOriginalName(),
                "Renamed from " + oldName + " to " + updated.getOriginalName()
        ));

        return mapToFileResponse(updated);
    }

    @Override
    @Transactional
    public FileResponse moveFile(String userEmail, UUID fileId, MoveFileRequest request) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        Folder targetFolder = null;
        if (request.getTargetFolderId() != null) {
            targetFolder = folderRepository.findByIdAndUser(request.getTargetFolderId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", request.getTargetFolderId()));
        }

        fileItem.setFolder(targetFolder);
        FileItem updated = fileRepository.save(fileItem);

        eventPublisher.publishEvent(new ActivityEvent(
                this, user, ActivityType.FILE_MOVED, "FILE", updated.getId(), updated.getOriginalName(),
                "Moved file directory"
        ));

        return mapToFileResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFile(String userEmail, UUID fileId) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        fileItem.setIsTrashed(true);
        fileRepository.save(fileItem);

        eventPublisher.publishEvent(new ActivityEvent(
                this, user, ActivityType.FILE_DELETED, "FILE", fileItem.getId(), fileItem.getOriginalName(),
                "Moved file to trash"
        ));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
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
