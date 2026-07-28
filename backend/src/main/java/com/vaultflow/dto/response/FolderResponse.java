package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderResponse {
    private UUID id;
    private String name;
    private UUID parentId;
    private String path;
    private Integer depth;
    private Boolean isTrashed;
    private Instant createdAt;
    private Instant updatedAt;
    private List<BreadcrumbResponse> breadcrumbs;
    private Integer subfolderCount;
}
