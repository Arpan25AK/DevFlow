package com.dev.auth_service.service;

import com.dev.auth_service.entity.RefreshToken;
import com.dev.auth_service.exception.RefreshTokenException;
import com.dev.auth_service.repo.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepo){
        this.refreshTokenRepo = refreshTokenRepo;

    }

    @Transactional
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

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token){
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepo.delete(token);
            throw new RefreshTokenException("Refresh token expired, please login again");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepo.findByToken(token);
    }

    public void deleteByToken(String token){
        refreshTokenRepo.deleteByToken(token);
    }

    public void revokeAllTokens(UUID userId){
        refreshTokenRepo.deleteByUserId(userId);
    }
}
