package vaultflow.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import vaultflow.dto.request.MoveRequest;
import vaultflow.dto.request.RenameRequest;
import vaultflow.dto.response.FileResponse;

public interface FileService {

    FileResponse upload(Long folderId, MultipartFile file) throws IOException;

    List<FileResponse> getAll(Long folderId);

    FileResponse rename(Long fileId, RenameRequest request);

    FileResponse move(Long fileId, MoveRequest request);

    void delete(Long fileId);

}