package com.vaultflow.service.impl;

import com.vaultflow.dto.response.BreadcrumbResponse;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.dto.response.TrashResponse;
import com.vaultflow.entity.ActivityType;
import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.Folder;
import com.vaultflow.entity.User;
import com.vaultflow.event.ActivityEvent;
import com.vaultflow.exception.ResourceNotFoundException;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.service.TrashService;
import com.vaultflow.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrashServiceImpl implements TrashService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public TrashResponse getTrashedItems(String userEmail) {
        User user = getUserByEmail(userEmail);

        List<FileItem> trashedFiles = fileRepository.findByUserAndIsTrashedTrue(user);
        List<Folder> trashedFolders = folderRepository.findByUserAndIsTrashedTrue(user);

        return TrashResponse.builder()
                .files(trashedFiles.stream().map(this::mapToFileResponse).toList())
                .folders(trashedFolders.stream().map(this::mapToFolderResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public FileResponse restoreFileFromTrash(String userEmail, UUID fileId) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        fileItem.setIsTrashed(false);
        FileItem restored = fileRepository.save(fileItem);

        eventPublisher.publishEvent(new ActivityEvent(
                this, user, ActivityType.FILE_RESTORED, "FILE", restored.getId(), restored.getOriginalName(), "Restored file from trash"
        ));

        return mapToFileResponse(restored);
    }

    @Override
    @Transactional
    public FolderResponse restoreFolderFromTrash(String userEmail, UUID folderId) {
        User user = getUserByEmail(userEmail);
        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        folder.setIsTrashed(false);
        Folder restored = folderRepository.save(folder);

        // Recursively restore subfolders and contained files
        restoreSubtree(user, folder);

        eventPublisher.publishEvent(new ActivityEvent(
                this, user, ActivityType.FILE_RESTORED, "FOLDER", restored.getId(), restored.getName(), "Restored folder from trash"
        ));

        return mapToFolderResponse(restored);
    }

    @Override
    @Transactional
    public void permanentlyDeleteFile(String userEmail, UUID fileId) {
        User user = getUserByEmail(userEmail);
        FileItem fileItem = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        // 1. Physically delete binary from cloud/local storage
        try {
            storageService.delete(fileItem.getStoragePath());
        } catch (Exception e) {
            // Log storage deletion warning
        }

        // 2. Remove database row
        fileRepository.delete(fileItem);
    }

    @Override
    @Transactional
    public void permanentlyDeleteFolder(String userEmail, UUID folderId) {
        User user = getUserByEmail(userEmail);
        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        purgeFolderTree(user, folder);
    }

    @Override
    @Transactional
    public void emptyTrashBin(String userEmail) {
        User user = getUserByEmail(userEmail);

        List<FileItem> trashedFiles = fileRepository.findByUserAndIsTrashedTrue(user);
        for (FileItem f : trashedFiles) {
            try {
                storageService.delete(f.getStoragePath());
            } catch (Exception ignored) {}
            fileRepository.delete(f);
        }

        List<Folder> trashedFolders = folderRepository.findByUserAndIsTrashedTrue(user);
        for (Folder f : trashedFolders) {
            purgeFolderTree(user, f);
        }
    }

    private void restoreSubtree(User user, Folder parentFolder) {
        List<FileItem> files = fileRepository.findByUserAndFolderIdAndIsTrashedFalse(user, parentFolder.getId());
        for (FileItem f : files) {
            f.setIsTrashed(false);
            fileRepository.save(f);
        }

        List<Folder> subfolders = folderRepository.findByUserAndParentIdAndIsTrashedFalse(user, parentFolder.getId());
        for (Folder sf : subfolders) {
            sf.setIsTrashed(false);
            folderRepository.save(sf);
            restoreSubtree(user, sf);
        }
    }

    private void purgeFolderTree(User user, Folder folder) {
        List<FileItem> files = fileRepository.findByUserAndFolderIdAndIsTrashedFalse(user, folder.getId());
        for (FileItem f : files) {
            try {
                storageService.delete(f.getStoragePath());
            } catch (Exception ignored) {}
            fileRepository.delete(f);
        }

        List<Folder> subfolders = folderRepository.findByUserAndParentIdAndIsTrashedFalse(user, folder.getId());
        for (Folder sf : subfolders) {
            purgeFolderTree(user, sf);
        }

        folderRepository.delete(folder);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
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

    private FolderResponse mapToFolderResponse(Folder folder) {
        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
                .path(folder.getPath())
                .depth(folder.getDepth())
                .isTrashed(folder.getIsTrashed())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .breadcrumbs(buildBreadcrumbs(folder))
                .subfolderCount(folder.getSubfolders() != null ? (int) folder.getSubfolders().stream().filter(f -> !f.getIsTrashed()).count() : 0)
                .build();
    }

    private List<BreadcrumbResponse> buildBreadcrumbs(Folder folder) {
        List<BreadcrumbResponse> breadcrumbs = new ArrayList<>();
        Folder current = folder;
        while (current != null) {
            breadcrumbs.add(0, BreadcrumbResponse.builder()
                    .id(current.getId())
                    .name(current.getName())
                    .build());
            current = current.getParent();
        }
        return breadcrumbs;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
