package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreadcrumbResponse {
    private UUID id;
    private String name;

    public static List<BreadcrumbResponse> EMPTY = List.of();
}