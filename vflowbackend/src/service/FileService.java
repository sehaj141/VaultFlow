package com.vaultflow.service;

import com.vaultflow.dto.request.MoveFileRequest;
import com.vaultflow.dto.request.RenameFileRequest;
import com.vaultflow.dto.response.DownloadUrlResponse;
import com.vaultflow.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileService {
    FileResponse uploadFile(UUID userId, UUID folderId, MultipartFile file);
    List<FileResponse> listFiles(UUID userId, UUID folderId);
    DownloadUrlResponse getDownloadUrl(UUID userId, UUID fileId);
    FileResponse renameFile(UUID userId, UUID fileId, RenameFileRequest request);
    FileResponse moveFile(UUID userId, UUID fileId, MoveFileRequest request);
    void deleteFile(UUID userId, UUID fileId);
}