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
public class DashboardAnalyticsResponse {
    private Long usedStorageBytes;
    private Long maxStorageBytes; // 5GB limit (5368709120 bytes)
    private Double usagePercentage;
    private String formattedUsedStorage;
    private String formattedMaxStorage;
    private Long totalFilesCount;
    private Long totalFoldersCount;
    private List<FileTypeCategoryStats> categoryBreakdown;
    private List<FileResponse> recentUploads;
    private List<FolderResponse> recentFolders;
}
