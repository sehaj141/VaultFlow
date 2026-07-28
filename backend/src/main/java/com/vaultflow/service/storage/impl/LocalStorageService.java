package com.vaultflow.service.storage.impl;

import com.vaultflow.exception.BadRequestException;
import com.vaultflow.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    public LocalStorageService(@Value("${vaultflow.storage.local-dir:vaultflow_data/uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String destinationPath) {
        try {
            if (file.isEmpty()) {
                throw new BadRequestException("Failed to store empty file.");
            }

            Path destination = this.rootLocation.resolve(destinationPath).normalize();
            if (!destination.getParent().equals(this.rootLocation)) {
                // Ensure target is inside upload root
                Files.createDirectories(destination.getParent());
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + destinationPath, e);
        }
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            Path file = rootLocation.resolve(storagePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("Could not read file: " + storagePath);
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("Could not read file: " + storagePath);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path file = rootLocation.resolve(storagePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Ignore error if file doesn't exist
        }
    }
}
