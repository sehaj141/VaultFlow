package com.vaultflow.util;

import com.vaultflow.constant.SupportedFileTypes;
import com.vaultflow.exception.UnsupportedFileTypeException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileTypeValidator {

    private final Tika tika = new Tika();

    /**
     * Detects the REAL mime type by reading the file's magic bytes (header signature),
     * completely ignoring the client-supplied Content-Type and filename extension —
     * both of which are trivially spoofable.
     */
    public String detectAndValidateMimeType(MultipartFile file) {
        try {
            String detectedMimeType = tika.detect(file.getInputStream());

            if (!SupportedFileTypes.ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                throw new UnsupportedFileTypeException(
                        "File type not supported: " + detectedMimeType);
            }

            return detectedMimeType;
        } catch (IOException e) {
            throw new UnsupportedFileTypeException("Could not read file contents to verify type");
        }
    }
}