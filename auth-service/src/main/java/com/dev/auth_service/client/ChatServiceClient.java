package com.dev.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ChatServiceConfig")
public interface ChatServiceClient {

    @PatchMapping("/internal/chat/anonymize/{senderId}")
    void anonymizeSender(@PathVariable("senderId") String senderId);
}