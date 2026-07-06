package com.dev.chat_service.controller;

import com.dev.chat_service.dto.ChatMessage;
import com.dev.chat_service.entity.ChatMessageDocument;
import com.dev.chat_service.repo.ChatMessageRepository;
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
    private final ChatMessageRepository chatMessageRepository;


    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessage message, StompHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        message.setSenderId(userId);
        message.setSenderUsername(username);
        message.setTimeStamp(LocalDateTime.now());

        // PERSIST
        chatMessageRepository.save(ChatMessageDocument.builder()
                .pullrequestId(message.getPullrequestId())
                .senderId(userId)
                .senderUsername(username)
                .content(message.getContent())
                .timeStamp(message.getTimeStamp())
                .build());

        messagingTemplate.convertAndSend("/topic/messages", message);
    }
}