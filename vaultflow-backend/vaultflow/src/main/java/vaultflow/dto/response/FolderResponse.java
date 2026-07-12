package vaultflow.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderResponse {

    private String id;

    private String name;

    private String parentId;

    private String createdAt;
}