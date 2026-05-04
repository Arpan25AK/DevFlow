package com.dev.chat_service.controller;

import com.dev.chat_service.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatMessageHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessage message, StompHeaderAccessor headerAccessor) {
        
        // Extract userId from session attributes (set by HandshakeInterceptor)
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        
        // Set sender info and timestamp
        message.setSenderId(userId);
        message.setTimeStamp(LocalDateTime.now());
        
        // Broadcast to all subscribers on /topic/messages
        messagingTemplate.convertAndSend("/topic/messages", message);
    }
}

