package vaultflow.service;

import vaultflow.dto.request.LoginRequest;
import vaultflow.dto.request.RegisterRequest;
import vaultflow.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String refreshToken);

}