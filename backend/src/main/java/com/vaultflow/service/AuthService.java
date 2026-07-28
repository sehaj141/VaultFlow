package com.vaultflow.service;

import com.vaultflow.dto.request.LoginRequest;
import com.vaultflow.dto.request.RefreshTokenRequest;
import com.vaultflow.dto.request.RegisterRequest;
import com.vaultflow.dto.response.AuthResponse;
import com.vaultflow.dto.response.UserSummaryResponse;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    UserSummaryResponse getCurrentUser(String email);
    void logout(UUID userId);
}
