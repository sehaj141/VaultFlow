package com.vaultflow.constant;

import java.util.Map;
import java.util.Set;

public final class SupportedFileTypes {

    private SupportedFileTypes() {}

    // Maps MIME type -> allowed extensions, used for magic-byte cross-validation
    public static final Map<String, String> ALLOWED_MIME_TO_EXTENSION = Map.of(
            "application/pdf", "pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx",
            "text/plain", "txt",
            "application/zip", "zip",
            "image/png", "png",
            "image/jpeg", "jpg"
    );

    public static final Set<String> ALLOWED_MIME_TYPES = ALLOWED_MIME_TO_EXTENSION.keySet();

    public static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024; // 50 MB per file
}