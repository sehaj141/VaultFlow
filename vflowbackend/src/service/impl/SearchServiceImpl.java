package com.vaultflow.service.impl;

import com.vaultflow.dto.request.SearchRequest;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.SearchResponse;
import com.vaultflow.entity.FileEntity;
import com.vaultflow.repository.SearchRepository;
import com.vaultflow.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchRepository searchRepository;

    private static final int MAX_PAGE_SIZE = 50;

    @Override
    public SearchResponse search(UUID userId, SearchRequest request) {
        // Clamp page size — never let a client request 10,000 results in one shot
        int size = Math.min(request.getSize(), MAX_PAGE_SIZE);
        int page = Math.max(request.getPage(), 0);
        int offset = page * size;

        List<FileEntity> results = searchRepository.search(
                userId,
                request.getQuery(),
                request.getExtension(),
                request.getUploadedAfter(),
                request.getUploadedBefore(),
                request.getFolderId(),
                offset,
                size
        );

        long totalResults = searchRepository.count(
                userId,
                request.getQuery(),
                request.getExtension(),
                request.getUploadedAfter(),
                request.getUploadedBefore(),
                request.getFolderId()
        );

        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalResults / size);

        List<FileResponse> mapped = results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return SearchResponse.builder()
                .results(mapped)
                .totalResults(totalResults)
                .page(page)
                .totalPages(totalPages)
                .queryEchoed(request.getQuery())
                .build();
    }

    private FileResponse toResponse(FileEntity file) {
        return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .extension(file.getExtension())
                .mimeType(file.getMimeType())
                .sizeBytes(file.getSizeBytes())
                .folderId(file.getFolderId())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
}