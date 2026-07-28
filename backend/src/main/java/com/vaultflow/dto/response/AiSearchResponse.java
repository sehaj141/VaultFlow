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
public class AiSearchResponse {
    private String originalPrompt;
    private ParsedSearchFilterDto parsedFilter;
    private List<FileResponse> matchingFiles;
}
