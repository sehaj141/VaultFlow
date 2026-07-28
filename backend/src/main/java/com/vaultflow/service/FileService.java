package com.vaultflow.service;

import com.vaultflow.dto.request.MoveFileRequest;
import com.vaultflow.dto.request.RenameFileRequest;
import com.vaultflow.dto.response.FileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FileService {
    FileResponse uploadFile(String userEmail, MultipartFile file, UUID folderId);
    Resource downloadFile(String userEmail, UUID fileId);
    FileResponse getFileMetadata(String userEmail, UUID fileId);
    List<FileResponse> getFiles(String userEmail, UUID folderId);
    List<FileResponse> searchFiles(String userEmail, String query, String extension, UUID folderId, Instant startDate, Instant endDate);
    FileResponse renameFile(String userEmail, UUID fileId, RenameFileRequest request);
    FileResponse moveFile(String userEmail, UUID fileId, MoveFileRequest request);
    void deleteFile(String userEmail, UUID fileId);
}
