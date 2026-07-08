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
public class FileResponse {
    private UUID id;
    private String name;
    private String extension;
    private String mimeType;
    private Long sizeBytes;
    private UUID folderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}