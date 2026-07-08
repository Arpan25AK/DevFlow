package com.dev.auth_service.controller;

import com.dev.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record AuthRequest(String email, String username, String password){}

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthRequest request){
        String responsemessege = authService.registerUser(request.email(), request.username(), request.password());
        return ResponseEntity.ok(responsemessege);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request, HttpServletResponse response){
        AuthService.LoginResponse result = authService.login(request.email(), request.password());

        ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), Duration.ofDays(7));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(result.accessToken());
    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge){
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(false) // dev is plain http; flip to true once this runs behind https
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }
}