package com.dev.repository_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AuthServiceConfig")
public interface AuthServiceClient {

    record ResolvedUser(String userId, String email, String username) {}

    @GetMapping("/internal/users/{username}")
    ResolvedUser resolveByUsername(@PathVariable("username") String username);
}