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
public class DashboardResponse {
    private StorageUsageResponse storageUsage;
    private long totalFiles;
    private long totalFolders;
    private List<RecentItemResponse> recentUploads;
    private List<FileTypeBreakdownResponse> fileTypeBreakdown;
}