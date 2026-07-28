package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private UUID id;
    private String originalName;
    private String mimeType;
    private String extension;
    private Long sizeBytes;
    private String formattedSize;
    private UUID folderId;
    private Boolean isTrashed;
    private Instant createdAt;
    private Instant updatedAt;
}
