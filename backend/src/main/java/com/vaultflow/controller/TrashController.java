package com.vaultflow.controller;

import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.dto.response.TrashResponse;
import com.vaultflow.service.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trash")
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

    @GetMapping
    public ResponseEntity<TrashResponse> getTrashedItems(@AuthenticationPrincipal UserDetails userDetails) {
        TrashResponse response = trashService.getTrashedItems(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/files/{id}/restore")
    public ResponseEntity<FileResponse> restoreFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        FileResponse response = trashService.restoreFileFromTrash(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/folders/{id}/restore")
    public ResponseEntity<FolderResponse> restoreFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        FolderResponse response = trashService.restoreFolderFromTrash(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<Void> permanentlyDeleteFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        trashService.permanentlyDeleteFile(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<Void> permanentlyDeleteFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id
    ) {
        trashService.permanentlyDeleteFolder(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/empty")
    public ResponseEntity<Void> emptyTrashBin(@AuthenticationPrincipal UserDetails userDetails) {
        trashService.emptyTrashBin(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
