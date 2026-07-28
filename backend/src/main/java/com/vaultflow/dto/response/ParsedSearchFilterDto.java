package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedSearchFilterDto {
    private String query;
    private String extension;
    private Long minSizeBytes;
    private Long maxSizeBytes;
    private Integer daysAgo;
    private String interpretationSummary;
}
