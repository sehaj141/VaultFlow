package com.vaultflow.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String store(MultipartFile file, String destinationPath);
    Resource loadAsResource(String storagePath);
    void delete(String storagePath);
}
