package com.vaultflow.service;

import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.dto.response.TrashResponse;

import java.util.UUID;

public interface TrashService {
    TrashResponse getTrashedItems(String userEmail);
    FileResponse restoreFileFromTrash(String userEmail, UUID fileId);
    FolderResponse restoreFolderFromTrash(String userEmail, UUID folderId);
    void permanentlyDeleteFile(String userEmail, UUID fileId);
    void permanentlyDeleteFolder(String userEmail, UUID folderId);
    void emptyTrashBin(String userEmail);
}
