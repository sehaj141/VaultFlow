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
public class ShareLinkResponse {
    private UUID id;
    private String token;
    private String shareUrl;
    private String role;
    private Boolean isPasswordProtected;
    private Instant expiresAt;
    private Long accessCount;
    private Boolean isActive;
    private Instant createdAt;
}
