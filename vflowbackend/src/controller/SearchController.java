package com.vaultflow.controller;

import com.vaultflow.dto.request.SearchRequest;
import com.vaultflow.dto.response.SearchResponse;
import com.vaultflow.security.CurrentUser;
import com.vaultflow.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @CurrentUser UUID userId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String extension,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadedAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadedBefore,
            @RequestParam(required = false) UUID folderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setExtension(extension);
        request.setUploadedAfter(uploadedAfter);
        request.setUploadedBefore(uploadedBefore);
        request.setFolderId(folderId);
        request.setPage(page);
        request.setSize(size);

        return ResponseEntity.ok(searchService.search(userId, request));
    }
}