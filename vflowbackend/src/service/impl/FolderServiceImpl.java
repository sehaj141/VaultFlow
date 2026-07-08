package com.vaultflow.service.impl;

import com.vaultflow.dto.request.CreateFolderRequest;
import com.vaultflow.dto.request.MoveFolderRequest;
import com.vaultflow.dto.request.RenameFolderRequest;
import com.vaultflow.dto.response.BreadcrumbResponse;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.entity.Folder;
import com.vaultflow.exception.DuplicateFolderNameException;
import com.vaultflow.exception.FolderNotFoundException;
import com.vaultflow.exception.InvalidFolderOperationException;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;

    @Override
    @Transactional
    public FolderResponse createFolder(UUID userId, CreateFolderRequest request) {
        UUID parentId = request.getParentId();

        if (parentId != null) {
            // Ensure the parent exists AND belongs to this user — prevents creating
            // a folder under someone else's tree by guessing a UUID.
            folderRepository.findByIdAndUserId(parentId, userId)
                    .orElseThrow(() -> new FolderNotFoundException("Parent folder not found"));
        }

        boolean nameTaken = parentId == null
                ? folderRepository.existsByUserIdAndParentIdIsNullAndNameIgnoreCase(userId, request.getName())
                : folderRepository.existsByUserIdAndParentIdAndNameIgnoreCase(userId, parentId, request.getName());

        if (nameTaken) {
            throw new DuplicateFolderNameException(
                    "A folder named '" + request.getName() + "' already exists here");
        }

        Folder folder = Folder.builder()
                .name(request.getName())
                .userId(userId)
                .parentId(parentId)
                .build();

        folder = folderRepository.save(folder);
        return toResponse(folder);
    }

    @Override
    public List<FolderResponse> listChildren(UUID userId, UUID parentId) {
        List<Folder> children = (parentId == null)
                ? folderRepository.findByUserIdAndParentIdIsNullAndDeletedAtIsNull(userId)
                : folderRepository.findByUserIdAndParentIdAndDeletedAtIsNull(userId, parentId);

        return children.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FolderResponse renameFolder(UUID userId, UUID folderId, RenameFolderRequest request) {
        Folder folder = getOwnedFolderOrThrow(userId, folderId);

        boolean nameTaken = folder.getParentId() == null
                ? folderRepository.existsByUserIdAndParentIdIsNullAndNameIgnoreCase(userId, request.getName())
                : folderRepository.existsByUserIdAndParentIdAndNameIgnoreCase(userId, folder.getParentId(), request.getName());

        if (nameTaken && !folder.getName().equalsIgnoreCase(request.getName())) {
            throw new DuplicateFolderNameException(
                    "A folder named '" + request.getName() + "' already exists here");
        }

        folder.setName(request.getName());
        folder = folderRepository.save(folder);
        return toResponse(folder);
    }

    @Override
    @Transactional
    public FolderResponse moveFolder(UUID userId, UUID folderId, MoveFolderRequest request) {
        Folder folder = getOwnedFolderOrThrow(userId, folderId);
        UUID newParentId = request.getNewParentId();

        if (newParentId != null) {
            if (newParentId.equals(folderId)) {
                throw new InvalidFolderOperationException("A folder cannot be moved into itself");
            }

            // Ownership check
            folderRepository.findByIdAndUserId(newParentId, userId)
                    .orElseThrow(() -> new FolderNotFoundException("Destination folder not found"));

            // Cycle check: is the destination actually a descendant of the folder being moved?
            // (i.e. is `folderId` an ancestor of `newParentId`?)
            boolean wouldCreateCycle = folderRepository.isSameOrAncestor(newParentId, folderId);
            if (wouldCreateCycle) {
                throw new InvalidFolderOperationException(
                        "Cannot move a folder into one of its own subfolders");
            }
        }

        boolean nameTaken = newParentId == null
                ? folderRepository.existsByUserIdAndParentIdIsNullAndNameIgnoreCase(userId, folder.getName())
                : folderRepository.existsByUserIdAndParentIdAndNameIgnoreCase(userId, newParentId, folder.getName());

        if (nameTaken) {
            throw new DuplicateFolderNameException(
                    "A folder named '" + folder.getName() + "' already exists in the destination");
        }

        folder.setParentId(newParentId);
        folder = folderRepository.save(folder);
        return toResponse(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(UUID userId, UUID folderId) {
        Folder folder = getOwnedFolderOrThrow(userId, folderId);
        // ON DELETE CASCADE at the DB level removes the entire subtree.
        // NOTE: this is a HARD delete for now — Phase 9 replaces this with soft-delete (deleted_at)
        // and moves the folder + subtree into the Trash lifecycle instead.
        folderRepository.delete(folder);
    }

    @Override
    public List<BreadcrumbResponse> getBreadcrumb(UUID userId, UUID folderId) {
        getOwnedFolderOrThrow(userId, folderId); // ownership check
        return folderRepository.findBreadcrumbTrail(folderId).stream()
                .map(row -> BreadcrumbResponse.builder()
                        .id((UUID) row[0])
                        .name((String) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    private Folder getOwnedFolderOrThrow(UUID userId, UUID folderId) {
        return folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));
    }

    private FolderResponse toResponse(Folder folder) {
        boolean hasChildren = !folderRepository
                .findByUserIdAndParentIdAndDeletedAtIsNull(folder.getUserId(), folder.getId())
                .isEmpty();

        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParentId())
                .hasChildren(hasChildren)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}