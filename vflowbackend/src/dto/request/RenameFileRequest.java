package com.vaultflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameFileRequest {
    @NotBlank(message = "File name is required")
    private String name;
}