package com.vaultflow.service;

import com.vaultflow.dto.request.CreateFolderRequest;
import com.vaultflow.dto.request.MoveFolderRequest;
import com.vaultflow.dto.request.RenameFolderRequest;
import com.vaultflow.dto.response.FolderResponse;

import java.util.List;
import java.util.UUID;

public interface FolderService {
    FolderResponse createFolder(String userEmail, CreateFolderRequest request);
    FolderResponse getFolderById(String userEmail, UUID folderId);
    List<FolderResponse> getSubfolders(String userEmail, UUID parentId);
    FolderResponse renameFolder(String userEmail, UUID folderId, RenameFolderRequest request);
    FolderResponse moveFolder(String userEmail, UUID folderId, MoveFolderRequest request);
    void deleteFolder(String userEmail, UUID folderId);
}
