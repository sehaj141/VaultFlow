package com.vaultflow.controller;

import com.vaultflow.dto.request.MoveFileRequest;
import com.vaultflow.dto.request.RenameFileRequest;
import com.vaultflow.dto.response.DownloadUrlResponse;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.security.CurrentUser;
import com.vaultflow.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileResponse> upload(
            @CurrentUser UUID userId,
            @RequestParam(required = false) UUID folderId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.uploadFile(userId, folderId, file));
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> list(
            @CurrentUser UUID userId,
            @RequestParam(required = false) UUID folderId) {
        return ResponseEntity.ok(fileService.listFiles(userId, folderId));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<DownloadUrlResponse> getDownloadUrl(
            @CurrentUser UUID userId,
            @PathVariable UUID fileId) {
        return ResponseEntity.ok(fileService.getDownloadUrl(userId, fileId));
    }

    @PutMapping("/{fileId}/rename")
    public ResponseEntity<FileResponse> rename(
            @CurrentUser UUID userId,
            @PathVariable UUID fileId,
            @Valid @RequestBody RenameFileRequest request) {
        return ResponseEntity.ok(fileService.renameFile(userId, fileId, request));
    }

    @PutMapping("/{fileId}/move")
    public ResponseEntity<FileResponse> move(
            @CurrentUser UUID userId,
            @PathVariable UUID fileId,
            @Valid @RequestBody MoveFileRequest request) {
        return ResponseEntity.ok(fileService.moveFile(userId, fileId, request));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UUID userId,
            @PathVariable UUID fileId) {
        fileService.deleteFile(userId, fileId);
        return ResponseEntity.noContent().build();
    }
}