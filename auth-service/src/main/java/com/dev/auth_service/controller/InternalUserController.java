package com.dev.auth_service.controller;

import com.dev.auth_service.entity.User;
import com.dev.auth_service.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal, service-to-service only. Do NOT add a gateway route for this path.
 * The api-gateway only proxies /api/auth/** to this service, so as long as this
 * stays outside that prefix, the browser can never reach it directly.
 *
 * In production, auth-service's port should additionally not be reachable from
 * outside the internal docker/network - the path prefix alone is not a security
 * boundary, just a convenience for local dev where everything shares a network.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    public record ResolvedUser(String userId, String email, String username) {}

    private final UserRepository userRepository;

    public InternalUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{username}")
    public ResponseEntity<ResolvedUser> resolveByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username.toLowerCase())
                .map(u -> ResponseEntity.ok(new ResolvedUser(u.getId().toString(), u.getEmail(), u.getUsername())))
                .orElse(ResponseEntity.notFound().build());
    }
}