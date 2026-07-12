package vaultflow.service;

import java.util.List;

import vaultflow.dto.request.CreateFolderRequest;
import vaultflow.dto.request.MoveRequest;
import vaultflow.dto.request.RenameRequest;
import vaultflow.dto.response.FolderResponse;

public interface FolderService {

    FolderResponse create(CreateFolderRequest request);

    List<FolderResponse> getAll(Long parentId);

    FolderResponse rename(Long folderId, RenameRequest request);

    FolderResponse move(Long folderId, MoveRequest request);

    void delete(Long folderId);

}