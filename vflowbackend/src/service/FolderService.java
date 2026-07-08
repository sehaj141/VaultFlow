package com.vaultflow.service;

import com.vaultflow.dto.request.CreateFolderRequest;
import com.vaultflow.dto.request.MoveFolderRequest;
import com.vaultflow.dto.request.RenameFolderRequest;
import com.vaultflow.dto.response.BreadcrumbResponse;
import com.vaultflow.dto.response.FolderResponse;

import java.util.List;
import java.util.UUID;

public interface FolderService {
    FolderResponse createFolder(UUID userId, CreateFolderRequest request);
    List<FolderResponse> listChildren(UUID userId, UUID parentId);
    FolderResponse renameFolder(UUID userId, UUID folderId, RenameFolderRequest request);
    FolderResponse moveFolder(UUID userId, UUID folderId, MoveFolderRequest request);
    void deleteFolder(UUID userId, UUID folderId);
    List<BreadcrumbResponse> getBreadcrumb(UUID userId, UUID folderId);
}