package vaultflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameRequest {

    @NotBlank(message = "Name is required")
    private String name;

}