package com.vaultflow.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SearchRequest {

    private String query;              // text to search — null means "match everything"

    private String extension;          // filter by type: "pdf", "png", etc.

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate uploadedAfter;   // filter: files uploaded on or after this date

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate uploadedBefore;  // filter: files uploaded on or before this date

    private UUID folderId;             // filter: restrict to a specific folder

    private int page = 0;             // zero-based page number
    private int size = 20;            // results per page
}