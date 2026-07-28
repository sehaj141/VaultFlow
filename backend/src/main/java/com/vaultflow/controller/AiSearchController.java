package com.vaultflow.controller;

import com.vaultflow.dto.request.AiSearchRequest;
import com.vaultflow.dto.response.AiSearchResponse;
import com.vaultflow.service.AiSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiSearchController {

    private final AiSearchService aiSearchService;

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponse> processAiSearch(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AiSearchRequest request
    ) {
        AiSearchResponse response = aiSearchService.processAiSearch(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
