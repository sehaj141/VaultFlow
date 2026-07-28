package com.vaultflow.service;

import com.vaultflow.entity.RefreshToken;
import com.vaultflow.entity.User;
import com.vaultflow.exception.TokenRefreshException;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(UUID userId);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
    int deleteByUserId(UUID userId);
    int deleteByToken(String token);
}
