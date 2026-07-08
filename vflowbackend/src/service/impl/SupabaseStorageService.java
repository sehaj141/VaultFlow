package com.vaultflow.service.impl;

import com.vaultflow.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService implements StorageService {

    private final WebClient supabaseWebClient;

    @Value("${supabase.bucket-name}")
    private String bucketName;

    @Override
    public void upload(String storagePath, MultipartFile file) throws Exception {
        supabaseWebClient.post()
                .uri("/storage/v1/object/{bucket}/{path}", bucketName, storagePath)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .bodyValue(file.getBytes())
                .retrieve()
                .toBodilessEntity()
                .block(); // synchronous for now — backend-mediated upload is inherently blocking per request
    }

    @Override
    public String generateSignedDownloadUrl(String storagePath, int expirySeconds) {
        Map response = supabaseWebClient.post()
                .uri("/storage/v1/object/sign/{bucket}/{path}", bucketName, storagePath)
                .bodyValue(Map.of("expiresIn", expirySeconds))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String signedPath = (String) response.get("signedURL");
        return supabaseBaseUrl() + signedPath;
    }

    @Override
    public void delete(String storagePath) {
        supabaseWebClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/storage/v1/object/{bucket}/{path}", bucketName, storagePath)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Value("${supabase.url}")
    private String supabaseBaseUrl;

    private String supabaseBaseUrl() {
        return supabaseBaseUrl;
    }
}