package com.vaultflow.service.impl;

import com.vaultflow.dto.request.CreateFolderRequest;
import com.vaultflow.dto.request.MoveFolderRequest;
import com.vaultflow.dto.request.RenameFolderRequest;
import com.vaultflow.dto.response.BreadcrumbResponse;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.entity.Folder;
import com.vaultflow.entity.User;
import com.vaultflow.exception.BadRequestException;
import com.vaultflow.exception.ResourceNotFoundException;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FolderResponse createFolder(String userEmail, CreateFolderRequest request) {
        User user = getUserByEmail(userEmail);

        Folder parent = null;
        String pathPrefix = "";
        int depth = 0;

        if (request.getParentId() != null) {
            parent = folderRepository.findByIdAndUser(request.getParentId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Folder", "id", request.getParentId()));
            
            if (folderRepository.existsByUserAndParentAndName(user, parent, request.getName().trim())) {
                throw new BadRequestException("A folder named '" + request.getName() + "' already exists in this directory.");
            }
            pathPrefix = parent.getPath();
            depth = parent.getDepth() + 1;
        } else {
            if (folderRepository.existsByUserAndParentIsNullAndName(user, request.getName().trim())) {
                throw new BadRequestException("A root folder named '" + request.getName() + "' already exists.");
            }
        }

        UUID newFolderId = UUID.randomUUID();
        String currentPath = pathPrefix.isEmpty() ? "/" + newFolderId : pathPrefix + "/" + newFolderId;

        Folder folder = Folder.builder()
                .id(newFolderId)
                .name(request.getName().trim())
                .user(user)
                .parent(parent)
                .path(currentPath)
                .depth(depth)
                .isTrashed(false)
                .build();

        Folder savedFolder = folderRepository.save(folder);
        return mapToFolderResponse(savedFolder);
    }

    @Override
    public FolderResponse getFolderById(String userEmail, UUID folderId) {
        User user = getUserByEmail(userEmail);
        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        return mapToFolderResponse(folder);
    }

    @Override
    public List<FolderResponse> getSubfolders(String userEmail, UUID parentId) {
        User user = getUserByEmail(userEmail);
        List<Folder> subfolders;

        if (parentId == null) {
            subfolders = folderRepository.findByUserAndParentIsNullAndIsTrashedFalse(user);
        } else {
            subfolders = folderRepository.findByUserAndParentIdAndIsTrashedFalse(user, parentId);
        }

        return subfolders.stream()
                .map(this::mapToFolderResponse)
                .toList();
    }

    @Override
    @Transactional
    public FolderResponse renameFolder(String userEmail, UUID folderId, RenameFolderRequest request) {
        User user = getUserByEmail(userEmail);
        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        String newName = request.getNewName().trim();

        if (folder.getName().equalsIgnoreCase(newName)) {
            return mapToFolderResponse(folder);
        }

        if (folder.getParent() != null) {
            if (folderRepository.existsByUserAndParentAndName(user, folder.getParent(), newName)) {
                throw new BadRequestException("A folder named '" + newName + "' already exists in this directory.");
            }
        } else {
            if (folderRepository.existsByUserAndParentIsNullAndName(user, newName)) {
                throw new BadRequestException("A root folder named '" + newName + "' already exists.");
            }
        }

        folder.setName(newName);
        Folder updatedFolder = folderRepository.save(folder);
        return mapToFolderResponse(updatedFolder);
    }

    @Override
    @Transactional
    public FolderResponse moveFolder(String userEmail, UUID folderId, MoveFolderRequest request) {
        User user = getUserByEmail(userEmail);
        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        UUID targetParentId = request.getTargetParentId();

        // 1. Prevent moving folder into itself
        if (folderId.equals(targetParentId)) {
            throw new BadRequestException("Cannot move a folder into itself.");
        }

        Folder targetParent = null;
        String newBasePath = "";
        int newDepth = 0;

        if (targetParentId != null) {
            targetParent = folderRepository.findByIdAndUser(targetParentId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Target Parent Folder", "id", targetParentId));

            // 2. Prevent cycle: Cannot move folder into its own descendant
            if (targetParent.getPath().startsWith(folder.getPath())) {
                throw new BadRequestException("Cannot move a folder into one of its own subfolders.");
            }

            if (folderRepository.existsByUserAndParentAndName(user, targetParent, folder.getName())) {
                throw new BadRequestException("A folder named '" + folder.getName() + "' already exists in target destination.");
            }

            newBasePath = targetParent.getPath();
            newDepth = targetParent.getDepth() + 1;
        } else {
            if (folderRepository.existsByUserAndParentIsNullAndName(user, folder.getName())) {
                throw new BadRequestException("A root folder named '" + folder.getName() + "' already exists.");
            }
        }

        // Calculate path changes for folder and all its descendants
        String oldPathPrefix = folder.getPath();
        String newPathPrefix = newBasePath.isEmpty() ? "/" + folder.getId() : newBasePath + "/" + folder.getId();
        int depthDelta = newDepth - folder.getDepth();

        // Fetch all descendants to update paths
        List<Folder> descendants = folderRepository.findAllDescendants(user, oldPathPrefix);
        for (Folder descendant : descendants) {
            String updatedPath = descendant.getPath().replaceFirst(oldPathPrefix, newPathPrefix);
            descendant.setPath(updatedPath);
            descendant.setDepth(descendant.getDepth() + depthDelta);
        }

        folder.setParent(targetParent);
        folder.setPath(newPathPrefix);
        folder.setDepth(newDepth);

        folderRepository.saveAll(descendants);
        Folder savedFolder = folderRepository.save(folder);

        return mapToFolderResponse(savedFolder);
    }

    @Override
    @Transactional
    public void deleteFolder(String userEmail, UUID folderId) {
        User user = getUserByEmail(userEmail);
        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        // Soft delete folder and all descendants (isTrashed = true)
        List<Folder> descendants = folderRepository.findAllDescendants(user, folder.getPath());
        for (Folder descendant : descendants) {
            descendant.setIsTrashed(true);
        }
        folder.setIsTrashed(true);

        folderRepository.saveAll(descendants);
        folderRepository.save(folder);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private FolderResponse mapToFolderResponse(Folder folder) {
        List<BreadcrumbResponse> breadcrumbs = buildBreadcrumbs(folder);
        int subfolderCount = folder.getSubfolders() != null ? (int) folder.getSubfolders().stream().filter(f -> !f.getIsTrashed()).count() : 0;

        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
                .path(folder.getPath())
                .depth(folder.getDepth())
                .isTrashed(folder.getIsTrashed())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .breadcrumbs(breadcrumbs)
                .subfolderCount(subfolderCount)
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
}
