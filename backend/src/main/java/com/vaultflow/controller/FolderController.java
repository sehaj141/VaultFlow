package com.vaultflow.controller;

import com.vaultflow.dto.request.CreateFolderRequest;
import com.vaultflow.dto.request.MoveFolderRequest;
import com.vaultflow.dto.request.RenameFolderRequest;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateFolderRequest request
    ) {
        FolderResponse response = folderService.createFolder(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolderResponse> getFolderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        FolderResponse response = folderService.getFolderById(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FolderResponse>> getSubfolders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "parentId", required = false) UUID parentId
    ) {
        List<FolderResponse> response = folderService.getSubfolders(userDetails.getUsername(), parentId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<FolderResponse> renameFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id,
            @Valid @RequestBody RenameFolderRequest request
    ) {
        FolderResponse response = folderService.renameFolder(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<FolderResponse> moveFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id,
            @Valid @RequestBody MoveFolderRequest request
    ) {
        FolderResponse response = folderService.moveFolder(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        folderService.deleteFolder(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
