package vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private Long id;

    private String fileName;

    private String originalFileName;

    private String contentType;

    private Long size;

    private Long folderId;

    private LocalDateTime createdAt;

}