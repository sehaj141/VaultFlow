package com.vaultflow.service;

import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.Folder;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.FolderRepository;
import com.vaultflow.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrashCleanupScheduler {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final StorageService storageService;

    // Runs every night at 3:00 AM (0 0 3 * * ?)
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void purgeOldTrashedItems() {
        log.info("Starting automated 30-day trash cleanup job...");

        Instant cutoffTime = Instant.now().minus(30, ChronoUnit.DAYS);

        List<FileItem> allTrashedFiles = fileRepository.findAll().stream()
                .filter(f -> f.getIsTrashed() && f.getUpdatedAt().isBefore(cutoffTime))
                .toList();

        int purgedFileCount = 0;
        for (FileItem file : allTrashedFiles) {
            try {
                storageService.delete(file.getStoragePath());
            } catch (Exception e) {
                log.warn("Failed to delete storage file: {}", file.getStoragePath());
            }
            fileRepository.delete(file);
            purgedFileCount++;
        }

        List<Folder> allTrashedFolders = folderRepository.findAll().stream()
                .filter(f -> f.getIsTrashed() && f.getUpdatedAt().isBefore(cutoffTime))
                .toList();

        int purgedFolderCount = 0;
        for (Folder folder : allTrashedFolders) {
            folderRepository.delete(folder);
            purgedFolderCount++;
        }

        log.info("Trash cleanup completed. Purged {} files and {} folders older than 30 days.", purgedFileCount, purgedFolderCount);
    }
}
