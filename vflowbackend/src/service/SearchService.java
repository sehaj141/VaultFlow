package com.vaultflow.service;

import com.vaultflow.dto.request.SearchRequest;
import com.vaultflow.dto.response.SearchResponse;

import java.util.UUID;

public interface SearchService {
    SearchResponse search(UUID userId, SearchRequest request);
}