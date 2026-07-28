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
public class FileVersionResponse {
    private UUID id;
    private UUID fileId;
    private Integer versionNumber;
    private Long sizeBytes;
    private String formattedSize;
    private String mimeType;
    private UserSummaryResponse uploadedBy;
    private Instant createdAt;
}
