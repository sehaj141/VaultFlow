package com.vaultflow.service.impl;

import com.vaultflow.dto.response.BreadcrumbResponse;
import com.vaultflow.dto.response.DashboardAnalyticsResponse;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.FileTypeCategoryStats;
import com.vaultflow.dto.response.FolderResponse;
import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.Folder;
import com.vaultflow.entity.User;
import com.vaultflow.exception.ResourceNotFoundException;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    private static final long MAX_FREE_TIER_STORAGE_BYTES = 5L * 1024 * 1024 * 1024; // 5 GB Limit

    @Override
    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getDashboardAnalytics(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Long usedStorageBytes = fileRepository.getTotalStorageUsedByUser(user);
        if (usedStorageBytes == null) {
            usedStorageBytes = 0L;
        }

        double usagePercentage = (double) usedStorageBytes / MAX_FREE_TIER_STORAGE_BYTES * 100.0;
        DecimalFormat df = new DecimalFormat("#,##0.0");

        long totalFilesCount = fileRepository.countByUserAndIsTrashedFalse(user);
        long totalFoldersCount = folderRepository.countByUserAndIsTrashedFalse(user);

        // Fetch recent uploads and folders
        List<FileItem> recentUploadsEntities = fileRepository.findTop5ByUserAndIsTrashedFalseOrderByCreatedAtDesc(user);
        List<Folder> recentFoldersEntities = folderRepository.findTop5ByUserAndIsTrashedFalseOrderByCreatedAtDesc(user);

        List<FileResponse> recentUploads = recentUploadsEntities.stream()
                .map(this::mapToFileResponse)
                .toList();

        List<FolderResponse> recentFolders = recentFoldersEntities.stream()
                .map(this::mapToFolderResponse)
                .toList();

        // Calculate file type breakdown
        List<FileItem> allUserFiles = fileRepository.findByUserAndIsTrashedFalse(user);
        List<FileTypeCategoryStats> categoryBreakdown = computeCategoryBreakdown(allUserFiles, usedStorageBytes);

        return DashboardAnalyticsResponse.builder()
                .usedStorageBytes(usedStorageBytes)
                .maxStorageBytes(MAX_FREE_TIER_STORAGE_BYTES)
                .usagePercentage(Double.parseDouble(df.format(usagePercentage)))
                .formattedUsedStorage(formatFileSize(usedStorageBytes))
                .formattedMaxStorage("5.0 GB")
                .totalFilesCount(totalFilesCount)
                .totalFoldersCount(totalFoldersCount)
                .categoryBreakdown(categoryBreakdown)
                .recentUploads(recentUploads)
                .recentFolders(recentFolders)
                .build();
    }

    private List<FileTypeCategoryStats> computeCategoryBreakdown(List<FileItem> files, long totalUsedBytes) {
        Map<String, List<FileItem>> categorizedFiles = new HashMap<>();
        categorizedFiles.put("PDF Documents", new ArrayList<>());
        categorizedFiles.put("Images", new ArrayList<>());
        categorizedFiles.put("Text & Docs", new ArrayList<>());
        categorizedFiles.put("Archives", new ArrayList<>());
        categorizedFiles.put("Other Files", new ArrayList<>());

        for (FileItem file : files) {
            String ext = file.getExtension().toLowerCase();
            if (ext.equals("pdf")) {
                categorizedFiles.get("PDF Documents").add(file);
            } else if (Set.of("png", "jpeg", "jpg").contains(ext)) {
                categorizedFiles.get("Images").add(file);
            } else if (Set.of("docx", "txt").contains(ext)) {
                categorizedFiles.get("Text & Docs").add(file);
            } else if (ext.equals("zip")) {
                categorizedFiles.get("Archives").add(file);
            } else {
                categorizedFiles.get("Other Files").add(file);
            }
        }

        DecimalFormat df = new DecimalFormat("#,##0.0");
        List<FileTypeCategoryStats> statsList = new ArrayList<>();

        for (Map.Entry<String, List<FileItem>> entry : categorizedFiles.entrySet()) {
            long count = entry.getValue().size();
            long categoryBytes = entry.getValue().stream().mapToLong(FileItem::getSizeBytes).sum();
            double pct = totalUsedBytes > 0 ? ((double) categoryBytes / totalUsedBytes) * 100.0 : 0.0;

            if (count > 0 || categoryBytes > 0) {
                statsList.add(FileTypeCategoryStats.builder()
                        .categoryName(entry.getKey())
                        .fileCount(count)
                        .sizeBytes(categoryBytes)
                        .formattedSize(formatFileSize(categoryBytes))
                        .percentageOfTotalStorage(Double.parseDouble(df.format(pct)))
                        .build());
            }
        }

        return statsList;
    }

    private FileResponse mapToFileResponse(FileItem fileItem) {
        return FileResponse.builder()
                .id(fileItem.getId())
                .originalName(fileItem.getOriginalName())
                .mimeType(fileItem.getMimeType())
                .extension(fileItem.getExtension())
                .sizeBytes(fileItem.getSizeBytes())
                .formattedSize(formatFileSize(fileItem.getSizeBytes()))
                .folderId(fileItem.getFolder() != null ? fileItem.getFolder().getId() : null)
                .isTrashed(fileItem.getIsTrashed())
                .createdAt(fileItem.getCreatedAt())
                .updatedAt(fileItem.getUpdatedAt())
                .build();
    }

    private FolderResponse mapToFolderResponse(Folder folder) {
        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
                .path(folder.getPath())
                .depth(folder.getDepth())
                .isTrashed(folder.getIsTrashed())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .breadcrumbs(buildBreadcrumbs(folder))
                .subfolderCount(folder.getSubfolders() != null ? (int) folder.getSubfolders().stream().filter(f -> !f.getIsTrashed()).count() : 0)
                .build();
    }

    private List<BreadcrumbResponse> buildBreadcrumbs(Folder folder) {
        List<BreadcrumbResponse> breadcrumbs = new ArrayList<>();
        Folder current = folder;
        while (current != null) {
            breadcrumbs.add(0, BreadcrumbResponse.builder()
                    .id(current.getId())
                    .name(current.getName())
                    .build());
            current = current.getParent();
        }
        return breadcrumbs;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
