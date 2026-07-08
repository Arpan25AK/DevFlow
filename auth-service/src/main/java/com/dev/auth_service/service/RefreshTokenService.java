package com.dev.auth_service.service;

import com.dev.auth_service.entity.RefreshToken;
import com.dev.auth_service.repo.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepo){
        this.refreshTokenRepo = refreshTokenRepo;

    }

    public RefreshToken createRefreshToken(UUID userId){
        refreshTokenRepo.deleteByUserId(userId);
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .userId(userId)
                .expiryDate(Instant.now().plusMillis( refreshExpiration))
                .build();

        return refreshTokenRepo.save(token);
    }
}
