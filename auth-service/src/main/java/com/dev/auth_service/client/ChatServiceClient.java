package com.dev.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "ChatServiceConfig")
public interface ChatServiceClient {

    // POST, not PATCH - Feign's default client can't send PATCH at all.
    @PostMapping("/internal/chat/anonymize/{senderId}")
    void anonymizeSender(@PathVariable("senderId") String senderId);
}