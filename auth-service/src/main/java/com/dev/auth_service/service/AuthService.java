package com.dev.auth_service.service;

import com.dev.auth_service.entity.RefreshToken;
import com.dev.auth_service.entity.User;
import com.dev.auth_service.exception.RefreshTokenException;
import com.dev.auth_service.exception.UserAlreadyExistsException;
import com.dev.auth_service.repo.UserRepository;
import com.dev.auth_service.security.JwtUtill;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtill jwtUtill;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtill jwtUtill, RefreshTokenService refreshTokenService){
        this.jwtUtill = jwtUtill;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    public record LoginResponse(String accessToken, String refreshToken){}

    public String registerUser(String email, String username, String rawpassword){

        if(userRepository.existsByEmail(email)) throw new UserAlreadyExistsException("Email already in use");

        String normalizedUsername = username == null ? null : username.trim().toLowerCase();
        if(normalizedUsername == null || normalizedUsername.isEmpty()){
            throw new IllegalArgumentException("Username is required");
        }
        if(!normalizedUsername.matches("^[a-z0-9_-]{3,39}$")){
            throw new IllegalArgumentException("Username must be 3-39 characters: letters, numbers, - or _");
        }
        if(userRepository.existsByUsername(normalizedUsername)) throw new UserAlreadyExistsException("Username already taken");

        String hashedPassword = passwordEncoder.encode(rawpassword);

        User newUser = User.builder()
                .email(email)
                .username(normalizedUsername)
                .password(hashedPassword)
                .role("ROLE-DEVELOPER")
                .build();

        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            // Backstop for the race between existsBy... and save() above.
            throw new UserAlreadyExistsException("Email or username already taken");
        }

        return "new User registered";
    }

    public LoginResponse login(String email, String rawpassord){

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user not found"));

        if(!passwordEncoder.matches(rawpassord, user.getPassword())){
            throw new RuntimeException("incorrect password");
        }

        String accessToken =  jwtUtill.generateToken(user.getId().toString(), user.getRole(), user.getEmail(), user.getUsername());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    public LoginResponse refreshAccessToken(String refreshTokenValue){
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RefreshTokenException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RefreshTokenException("User no longer exists"));

        String newAccessToken = jwtUtill.generateToken(user.getId().toString(), user.getRole(), user.getEmail(), user.getUsername());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponse(newAccessToken, newRefreshToken.getToken());
    }

    public void logout(String refreshTokenValue){
        refreshTokenService.deleteByToken(refreshTokenValue);
    }
}