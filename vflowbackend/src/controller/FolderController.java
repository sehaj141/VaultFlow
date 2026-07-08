package com.vaultflow.controller;

import com.vaultflow.dto.request.CreateFolderRequest;
import com.vaultflow.dto.request.MoveFolderRequest;
import com.vaultflow.dto.request.RenameFolderRequest;
import com.vaultflow.dto.response.BreadcrumbResponse;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.security.CurrentUser;
import com.vaultflow.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderResponse> create(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateFolderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(folderService.createFolder(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<FolderResponse>> listChildren(
            @CurrentUser UUID userId,
            @RequestParam(required = false) UUID parentId) {
        return ResponseEntity.ok(folderService.listChildren(userId, parentId));
    }

    @PutMapping("/{folderId}/rename")
    public ResponseEntity<FolderResponse> rename(
            @CurrentUser UUID userId,
            @PathVariable UUID folderId,
            @Valid @RequestBody RenameFolderRequest request) {
        return ResponseEntity.ok(folderService.renameFolder(userId, folderId, request));
    }

    @PutMapping("/{folderId}/move")
    public ResponseEntity<FolderResponse> move(
            @CurrentUser UUID userId,
            @PathVariable UUID folderId,
            @Valid @RequestBody MoveFolderRequest request) {
        return ResponseEntity.ok(folderService.moveFolder(userId, folderId, request));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UUID userId,
            @PathVariable UUID folderId) {
        folderService.deleteFolder(userId, folderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{folderId}/breadcrumb")
    public ResponseEntity<List<BreadcrumbResponse>> breadcrumb(
            @CurrentUser UUID userId,
            @PathVariable UUID folderId) {
        return ResponseEntity.ok(folderService.getBreadcrumb(userId, folderId));
    }
}