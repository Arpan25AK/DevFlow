package com.dev.auth_service.service;

import com.dev.auth_service.client.ChatServiceClient;
import com.dev.auth_service.client.RepositoryServiceClient;
import com.dev.auth_service.entity.RefreshToken;
import com.dev.auth_service.entity.User;
import com.dev.auth_service.exception.RefreshTokenException;
import com.dev.auth_service.exception.UserAlreadyExistsException;
import com.dev.auth_service.repo.UserRepository;
import com.dev.auth_service.security.JwtUtill;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtill jwtUtill;
    private final RefreshTokenService refreshTokenService;
    private final RepositoryServiceClient repositoryServiceClient;
    private final ChatServiceClient chatServiceClient;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtill jwtUtill, RefreshTokenService refreshTokenService,
                       RepositoryServiceClient repositoryServiceClient, ChatServiceClient chatServiceClient){
        this.jwtUtill = jwtUtill;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.repositoryServiceClient = repositoryServiceClient;
        this.chatServiceClient = chatServiceClient;
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

    public String changeUsername(String userId , String newUsername){
        User user = userRepository.findById(java.util.UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("user not found"));

        String normalized = newUsername == null ? null : newUsername.trim().toLowerCase();

        if(normalized == null || normalized.isEmpty()) throw new IllegalArgumentException("username is required");

        if(!normalized.matches("^[a-z0-9_-]{3,39}$")){
            throw new IllegalArgumentException("username must be inbetween 3- 39 chars long");
        }

        if(normalized.equals(user.getUsername())){
            return jwtUtill.generateToken(user.getId().toString(),user.getRole(),user.getEmail(),user.getUsername());
        }

        if(userRepository.existsByUsername(normalized)){
            throw new UserAlreadyExistsException("username already exists");
        }

        user.setUsername(normalized);

        try{
            userRepository.save(user);
        }catch (DataIntegrityViolationException e){
            throw new UserAlreadyExistsException("user already exists");
        }

        return jwtUtill.generateToken(user.getId().toString(), user.getRole(), user.getEmail(), user.getUsername());
    }

    public void changePassword(String userId, String currentPassword, String newPassword, Boolean logoutEverywhere){
        User user = userRepository.findById(java.util.UUID.fromString(userId)).
                orElseThrow(() -> new RuntimeException("user not found"));

        if(currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())){
            throw new IllegalArgumentException("password doesn't match");
        }

        if(newPassword == null || newPassword.length() < 8){
            throw new IllegalArgumentException("new password should at least be 8 chars long");
        }

        if(passwordEncoder.matches(newPassword, user.getPassword())){
            throw new IllegalArgumentException("new password cannot be same as curr password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        if(logoutEverywhere){
            refreshTokenService.revokeAllTokens(user.getId());
        }
    }

    public void deleteAccount(String userId, String currentPassword){
        User user = userRepository.findById(java.util.UUID.fromString(userId)).
                orElseThrow(() -> new RuntimeException("user not found!"));

        if(currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())){
            throw new IllegalArgumentException("password doesn't match");
        }

        String email = user.getEmail();
        String username = user.getUsername();

        try{
            repositoryServiceClient.purgeAllForUser(email);
        }catch (Exception e){
            log.error("failed to purge user data of : {}",email);
            throw new RuntimeException("failed to delete users repos");
        }

        try{
            chatServiceClient.anonymizeSender(userId);
        }catch (Exception e){
            log.error("failed to anonymize chat message for {} during account deletion ", username, e);
            throw new RuntimeException("failed to delete chat for the user");
        }

        refreshTokenService.revokeAllTokens(user.getId());
        userRepository.delete(user);

        log.info("account deleted successfully {} ", username);

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