package com.vaultflow.service;

import com.vaultflow.dto.request.AccessShareLinkRequest;
import com.vaultflow.dto.request.CreateShareLinkRequest;
import com.vaultflow.dto.response.PublicSharedResourceResponse;
import com.vaultflow.dto.response.ShareLinkResponse;
import org.springframework.core.io.Resource;

public interface ShareService {
    ShareLinkResponse createShareLink(String userEmail, CreateShareLinkRequest request);
    PublicSharedResourceResponse getPublicSharedResource(String token);
    PublicSharedResourceResponse verifyPasswordAndAccess(String token, AccessShareLinkRequest request);
    Resource downloadSharedFile(String token, String password);
    void revokeShareLink(String userEmail, String token);
}
