package com.vaultflow.service;

import com.vaultflow.dto.request.AiSearchRequest;
import com.vaultflow.dto.response.AiSearchResponse;

public interface AiSearchService {
    AiSearchResponse processAiSearch(String userEmail, AiSearchRequest request);
}
