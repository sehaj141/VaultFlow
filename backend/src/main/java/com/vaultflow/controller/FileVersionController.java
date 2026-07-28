package com.vaultflow.controller;

import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.FileVersionResponse;
import com.vaultflow.service.FileVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files/{fileId}/versions")
@RequiredArgsConstructor
public class FileVersionController {

    private final FileVersionService versionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadNewVersion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("fileId") UUID fileId,
            @RequestParam("file") MultipartFile file
    ) {
        FileResponse response = versionService.uploadNewVersion(userDetails.getUsername(), fileId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FileVersionResponse>> getVersionTimeline(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("fileId") UUID fileId
    ) {
        List<FileVersionResponse> response = versionService.getVersionTimeline(userDetails.getUsername(), fileId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{versionId}/restore")
    public ResponseEntity<FileResponse> restoreVersion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("fileId") UUID fileId,
            @PathVariable("versionId") UUID versionId
    ) {
        FileResponse response = versionService.restoreVersion(userDetails.getUsername(), fileId, versionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{versionId}/download")
    public ResponseEntity<Resource> downloadVersion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("fileId") UUID fileId,
            @PathVariable("versionId") UUID versionId
    ) {
        Resource resource = versionService.downloadVersion(userDetails.getUsername(), fileId, versionId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"version-" + versionId + "\"")
                .body(resource);
    }
}
