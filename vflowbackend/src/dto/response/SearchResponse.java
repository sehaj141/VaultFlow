package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<FileResponse> results;
    private long totalResults;
    private int page;
    private int totalPages;
    private String queryEchoed; // echo back the query for the frontend to highlight matches
}