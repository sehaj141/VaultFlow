package com.vaultflow.controller;

import com.vaultflow.dto.request.AccessShareLinkRequest;
import com.vaultflow.dto.request.CreateShareLinkRequest;
import com.vaultflow.dto.response.PublicSharedResourceResponse;
import com.vaultflow.dto.response.ShareLinkResponse;
import com.vaultflow.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shares")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping
    public ResponseEntity<ShareLinkResponse> createShareLink(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateShareLinkRequest request
    ) {
        ShareLinkResponse response = shareService.createShareLink(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/public/{token}")
    public ResponseEntity<PublicSharedResourceResponse> getPublicSharedResource(@PathVariable("token") String token) {
        PublicSharedResourceResponse response = shareService.getPublicSharedResource(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/{token}/access")
    public ResponseEntity<PublicSharedResourceResponse> verifyPasswordAndAccess(
            @PathVariable("token") String token,
            @RequestBody AccessShareLinkRequest request
    ) {
        PublicSharedResourceResponse response = shareService.verifyPasswordAndAccess(token, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/{token}/download")
    public ResponseEntity<Resource> downloadSharedFile(
            @PathVariable("token") String token,
            @RequestParam(value = "password", required = false) String password
    ) {
        PublicSharedResourceResponse resourceDetails = shareService.getPublicSharedResource(token);
        Resource resource = shareService.downloadSharedFile(token, password);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceDetails.getResourceName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> revokeShareLink(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("token") String token
    ) {
        shareService.revokeShareLink(userDetails.getUsername(), token);
        return ResponseEntity.noContent().build();
    }
}
