package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicSharedResourceResponse {
    private String token;
    private String resourceType; // "FILE" or "FOLDER"
    private String resourceName;
    private String permissionRole;
    private Boolean isPasswordProtected;
    private Boolean isPasswordVerified;
    private FileResponse fileDetails;
    private FolderResponse folderDetails;
    private List<FileResponse> folderFiles;
    private List<FolderResponse> folderSubfolders;
}
