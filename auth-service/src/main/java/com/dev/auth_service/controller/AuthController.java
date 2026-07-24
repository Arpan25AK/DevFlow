package com.dev.auth_service.controller;

import com.dev.auth_service.service.AuthService;
import com.dev.auth_service.service.MinioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record AuthRequest(String email, String username, String password){}

    private final AuthService authService;
    private final MinioService minioService;

    public AuthController(AuthService authService, MinioService minioService){
        this.authService = authService;
        this.minioService = minioService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthRequest request){
        String responsemessege = authService.registerUser(request.email(), request.username(), request.password());
        return ResponseEntity.ok(responsemessege);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request, HttpServletResponse response){
        AuthService.LoginResponse result = authService.login(request.email(), request.password());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken(), Duration.ofDays(7)).toString());
        return ResponseEntity.ok(result.accessToken());
    }

    public record ChangeUsernameRequest(String username){}

    @PatchMapping("/username")
    public ResponseEntity<?> changeUsername(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody ChangeUsernameRequest request){

        String newAccessToken = authService.changeUsername(userId, request.username());
        return ResponseEntity.ok(newAccessToken);
    }

    public record ChangePasswordRequest(String currentPassword, String newPassword, boolean logoutEverywhere){}

    @PatchMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody ChangePasswordRequest request){

        authService.changePassword(userId, request.currentPassword(), request.newPassword(), request.logoutEverywhere());
        return ResponseEntity.ok("Password changed successfully");
    }

    public record DeleteAccountRequest(String currentPassword){}

    @DeleteMapping
    public ResponseEntity<String> deleteAccount(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody DeleteAccountRequest request,
            HttpServletResponse response){

        authService.deleteAccount(userId, request.currentPassword());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie("", Duration.ZERO).toString());
        return ResponseEntity.ok("Account deleted");
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file){

        String avatarUrl = authService.uploadProfilePicture(userId, file);
        return ResponseEntity.ok(avatarUrl);
    }

    @GetMapping("/avatar/{userId}")
    public ResponseEntity<InputStreamResource> getAvatar(@PathVariable String userId){
        String objectName = authService.getAvatarObject(userId);
        if(objectName == null){
            return ResponseEntity.notFound().build();
        }

        MinioService.AvatarObject avatar = minioService.getAvatar(objectName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(new InputStreamResource(avatar.stream()));
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<String> deleteAvatar(@RequestHeader("X-User-Id") String userId){
        authService.deleteProfilePicture(userId);
        return ResponseEntity.ok("Profile picture removed");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response){

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token present");
        }

        AuthService.LoginResponse result = authService.refreshAccessToken(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken(), Duration.ofDays(7)).toString());
        return ResponseEntity.ok(result.accessToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response){

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie("", Duration.ZERO).toString());
        return ResponseEntity.ok("logged out");
    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge){
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }
}