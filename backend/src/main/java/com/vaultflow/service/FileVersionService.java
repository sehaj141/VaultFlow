package com.vaultflow.service;

import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.FileVersionResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileVersionService {
    FileResponse uploadNewVersion(String userEmail, UUID fileId, MultipartFile file);
    List<FileVersionResponse> getVersionTimeline(String userEmail, UUID fileId);
    FileResponse restoreVersion(String userEmail, UUID fileId, UUID versionId);
    Resource downloadVersion(String userEmail, UUID fileId, UUID versionId);
}
