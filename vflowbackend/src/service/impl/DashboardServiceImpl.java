package com.vaultflow.service.impl;

import com.vaultflow.dto.response.*;
import com.vaultflow.entity.FileEntity;
import com.vaultflow.entity.User;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    private static final int RECENT_UPLOADS_LIMIT = 10;

    @Override
    public DashboardResponse getDashboard(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        StorageUsageResponse storageUsage = buildStorageUsage(user);

        long totalFiles = fileRepository.countActiveFilesByUserId(userId);
        long totalFolders = folderRepository.countActiveFoldersByUserId(userId);

        List<RecentItemResponse> recentUploads = fileRepository
                .findTop10ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toRecentItem)
                .collect(Collectors.toList());

        List<FileTypeBreakdownResponse> breakdown = fileRepository.getFileTypeBreakdown(userId);

        return DashboardResponse.builder()
                .storageUsage(storageUsage)
                .totalFiles(totalFiles)
                .totalFolders(totalFolders)
                .recentUploads(recentUploads)
                .fileTypeBreakdown(breakdown)
                .build();
    }

    private StorageUsageResponse buildStorageUsage(User user) {
        long used = user.getStorageUsedBytes();
        long limit = user.getStorageLimitBytes();
        double percentage = limit == 0 ? 0 : (used * 100.0) / limit;

        return StorageUsageResponse.builder()
                .usedBytes(used)
                .limitBytes(limit)
                .percentageUsed(Math.round(percentage * 100) / 100.0) // 2 decimal places
                .build();
    }

    private RecentItemResponse toRecentItem(FileEntity file) {
        // NOTE: this method only ever receives files for now since "Recent Uploads"
        // is file-specific. Once Phase 8 (Activity Logs) lands, the dashboard's
        // "Recent Activity" feed (a DIFFERENT section from this) will read from the
        // activities table instead of inferring activity from file timestamps.
        return RecentItemResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .type("file")
                .extension(file.getExtension())
                .sizeBytes(file.getSizeBytes())
                .createdAt(file.getCreatedAt())
                .build();
    }
}