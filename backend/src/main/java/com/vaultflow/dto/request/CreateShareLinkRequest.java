package com.vaultflow.dto.request;

import com.vaultflow.entity.PermissionRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShareLinkRequest {
    private UUID fileId;
    private UUID folderId;
    @Builder.Default
    private PermissionRole role = PermissionRole.VIEWER;
    private String password; // optional password protection
    private Integer expirationHours; // e.g. 24, 168 (7 days), null for Never
}
