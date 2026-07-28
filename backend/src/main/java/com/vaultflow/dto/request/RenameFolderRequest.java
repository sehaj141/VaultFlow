package com.vaultflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenameFolderRequest {

    @NotBlank(message = "New folder name is required")
    @Size(min = 1, max = 255, message = "Folder name must be between 1 and 255 characters")
    @Pattern(regexp = "^[^/\\\\:*?\"<>|]+$", message = "Folder name contains invalid characters")
    private String newName;
}
