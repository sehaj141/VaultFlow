package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentItemResponse {
    private UUID id;
    private String name;
    private String type;       // "file" or "folder"
    private String extension;  // null for folders
    private Long sizeBytes;    // null for folders
    private LocalDateTime createdAt;
}