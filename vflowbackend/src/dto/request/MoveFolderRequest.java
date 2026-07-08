package com.vaultflow.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class MoveFolderRequest {
    // null = move to top level
    private UUID newParentId;
}