package com.vaultflow.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Uploads raw bytes to storage at the given path. Returns nothing — the caller
     * already knows the path since it generates it before calling this.
     */
    void upload(String storagePath, MultipartFile file) throws Exception;

    /**
     * Generates a short-lived signed URL for downloading a private object.
     */
    String generateSignedDownloadUrl(String storagePath, int expirySeconds);

    void delete(String storagePath);
}