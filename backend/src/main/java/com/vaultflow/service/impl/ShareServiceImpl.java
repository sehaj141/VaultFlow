package com.vaultflow.service.impl;

import com.vaultflow.dto.request.AccessShareLinkRequest;
import com.vaultflow.dto.request.CreateShareLinkRequest;
import com.vaultflow.dto.response.BreadcrumbResponse;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.dto.response.PublicSharedResourceResponse;
import com.vaultflow.dto.response.ShareLinkResponse;
import com.vaultflow.entity.*;
import com.vaultflow.exception.BadRequestException;
import com.vaultflow.exception.ResourceNotFoundException;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.repository.SharedLinkRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.service.ShareService;
import com.vaultflow.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final SharedLinkRepository sharedLinkRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    @Override
    @Transactional
    public ShareLinkResponse createShareLink(String userEmail, CreateShareLinkRequest request) {
        User creator = getUserByEmail(userEmail);

        if (request.getFileId() == null && request.getFolderId() == null) {
            throw new BadRequestException("Must specify either a fileId or folderId to create a share link.");
        }

        FileItem fileItem = null;
        Folder folder = null;

        if (request.getFileId() != null) {
            fileItem = fileRepository.findByIdAndUser(request.getFileId(), creator)
                    .orElseThrow(() -> new ResourceNotFoundException("File", "id", request.getFileId()));
        } else {
            folder = folderRepository.findByIdAndUser(request.getFolderId(), creator)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", request.getFolderId()));
        }

        // Generate unique token
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            passwordHash = passwordEncoder.encode(request.getPassword().trim());
        }

        Instant expiresAt = null;
        if (request.getExpirationHours() != null && request.getExpirationHours() > 0) {
            expiresAt = Instant.now().plus(request.getExpirationHours(), ChronoUnit.HOURS);
        }

        SharedLink sharedLink = SharedLink.builder()
                .token(token)
                .creator(creator)
                .file(fileItem)
                .folder(folder)
                .role(request.getRole() != null ? request.getRole() : PermissionRole.VIEWER)
                .passwordHash(passwordHash)
                .expiresAt(expiresAt)
                .accessCount(0L)
                .isActive(true)
                .build();

        SharedLink saved = sharedLinkRepository.save(sharedLink);
        return mapToShareLinkResponse(saved);
    }

    @Override
    @Transactional
    public PublicSharedResourceResponse getPublicSharedResource(String token) {
        SharedLink sharedLink = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Share Link", "token", token));

        validateShareLinkStatus(sharedLink);

        boolean isProtected = sharedLink.getPasswordHash() != null;

        // Increment access count
        sharedLink.setAccessCount(sharedLink.getAccessCount() + 1);
        sharedLinkRepository.save(sharedLink);

        if (isProtected) {
            return PublicSharedResourceResponse.builder()
                    .token(token)
                    .resourceType(sharedLink.getFile() != null ? "FILE" : "FOLDER")
                    .resourceName(sharedLink.getFile() != null ? sharedLink.getFile().getOriginalName() : sharedLink.getFolder().getName())
                    .permissionRole(sharedLink.getRole().name())
                    .isPasswordProtected(true)
                    .isPasswordVerified(false)
                    .build();
        }

        return buildResourceResponse(sharedLink, true);
    }

    @Override
    @Transactional
    public PublicSharedResourceResponse verifyPasswordAndAccess(String token, AccessShareLinkRequest request) {
        SharedLink sharedLink = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Share Link", "token", token));

        validateShareLinkStatus(sharedLink);

        if (sharedLink.getPasswordHash() != null) {
            if (request.getPassword() == null || !passwordEncoder.matches(request.getPassword(), sharedLink.getPasswordHash())) {
                throw new BadRequestException("Invalid password for protected share link.");
            }
        }

        return buildResourceResponse(sharedLink, true);
    }

    @Override
    public Resource downloadSharedFile(String token, String password) {
        SharedLink sharedLink = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Share Link", "token", token));

        validateShareLinkStatus(sharedLink);

        if (sharedLink.getFile() == null) {
            throw new BadRequestException("This share link does not point directly to a file.");
        }

        if (sharedLink.getPasswordHash() != null) {
            if (password == null || !passwordEncoder.matches(password, sharedLink.getPasswordHash())) {
                throw new BadRequestException("Invalid password for protected download.");
            }
        }

        return storageService.loadAsResource(sharedLink.getFile().getStoragePath());
    }

    @Override
    @Transactional
    public void revokeShareLink(String userEmail, String token) {
        User user = getUserByEmail(userEmail);
        SharedLink sharedLink = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Share Link", "token", token));

        if (!sharedLink.getCreator().getId().equals(user.getId())) {
            throw new BadRequestException("Only the share link creator can revoke this link.");
        }

        sharedLink.setIsActive(false);
        sharedLinkRepository.save(sharedLink);
    }

    private void validateShareLinkStatus(SharedLink sharedLink) {
        if (!sharedLink.getIsActive()) {
            throw new BadRequestException("This share link has been revoked.");
        }
        if (sharedLink.getExpiresAt() != null && sharedLink.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("This share link has expired.");
        }
    }

    private PublicSharedResourceResponse buildResourceResponse(SharedLink sharedLink, boolean verified) {
        if (sharedLink.getFile() != null) {
            return PublicSharedResourceResponse.builder()
                    .token(sharedLink.getToken())
                    .resourceType("FILE")
                    .resourceName(sharedLink.getFile().getOriginalName())
                    .permissionRole(sharedLink.getRole().name())
                    .isPasswordProtected(sharedLink.getPasswordHash() != null)
                    .isPasswordVerified(verified)
                    .fileDetails(mapToFileResponse(sharedLink.getFile()))
                    .build();
        } else {
            Folder folder = sharedLink.getFolder();
            List<FileItem> folderFiles = fileRepository.findByUserAndFolderIdAndIsTrashedFalse(folder.getUser(), folder.getId());
            List<Folder> folderSubfolders = folderRepository.findByUserAndParentIdAndIsTrashedFalse(folder.getUser(), folder.getId());

            return PublicSharedResourceResponse.builder()
                    .token(sharedLink.getToken())
                    .resourceType("FOLDER")
                    .resourceName(folder.getName())
                    .permissionRole(sharedLink.getRole().name())
                    .isPasswordProtected(sharedLink.getPasswordHash() != null)
                    .isPasswordVerified(verified)
                    .folderDetails(mapToFolderResponse(folder))
                    .folderFiles(folderFiles.stream().map(this::mapToFileResponse).toList())
                    .folderSubfolders(folderSubfolders.stream().map(this::mapToFolderResponse).toList())
                    .build();
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private ShareLinkResponse mapToShareLinkResponse(SharedLink link) {
        return ShareLinkResponse.builder()
                .id(link.getId())
                .token(link.getToken())
                .shareUrl("/share/" + link.getToken())
                .role(link.getRole().name())
                .isPasswordProtected(link.getPasswordHash() != null)
                .expiresAt(link.getExpiresAt())
                .accessCount(link.getAccessCount())
                .isActive(link.getIsActive())
                .createdAt(link.getCreatedAt())
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
