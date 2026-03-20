package com.dev.auth_service.Controller;

import com.dev.auth_service.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    /*this is a dto method */
    public record AuthRequest(String email, String password){}

    private final  AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    public ResponseEntity<String> signup(@RequestBody AuthRequest request){
        String responsemessege = authService.registerUser(request.email(), request.password());
        return ResponseEntity.ok(responsemessege);
    }

    public ResponseEntity<String> login(@RequestBody AuthRequest request){
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(token);
    }
}
