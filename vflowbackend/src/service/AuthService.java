package com.vaultflow.service;

import com.vaultflow.dto.request.LoginRequest;
import com.vaultflow.dto.request.RegisterRequest;
import com.vaultflow.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshAccessToken(String refreshTokenValue);
    void logout(String refreshTokenValue);
}