package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTypeCategoryStats {
    private String categoryName; // e.g. "PDF Documents", "Images", "Code & Docs", "Archives"
    private Long fileCount;
    private Long sizeBytes;
    private String formattedSize;
    private Double percentageOfTotalStorage;
}
