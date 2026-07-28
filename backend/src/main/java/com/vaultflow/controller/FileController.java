package com.vaultflow.controller;

import com.vaultflow.dto.request.MoveFileRequest;
import com.vaultflow.dto.request.RenameFileRequest;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) UUID folderId
    ) {
        FileResponse response = fileService.uploadFile(userDetails.getUsername(), file, folderId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileResponse>> searchFiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "extension", required = false) String extension,
            @RequestParam(value = "folderId", required = false) UUID folderId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate
    ) {
        List<FileResponse> response = fileService.searchFiles(
                userDetails.getUsername(), query, extension, folderId, startDate, endDate
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        FileResponse metadata = fileService.getFileMetadata(userDetails.getUsername(), id);
        Resource resource = fileService.downloadFile(userDetails.getUsername(), id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFileMetadata(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        FileResponse response = fileService.getFileMetadata(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getFiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "folderId", required = false) UUID folderId
    ) {
        List<FileResponse> response = fileService.getFiles(userDetails.getUsername(), folderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<FileResponse> renameFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id,
            @Valid @RequestBody RenameFileRequest request
    ) {
        FileResponse response = fileService.renameFile(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<FileResponse> moveFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id,
            @Valid @RequestBody MoveFileRequest request
    ) {
        FileResponse response = fileService.moveFile(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        fileService.deleteFile(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
