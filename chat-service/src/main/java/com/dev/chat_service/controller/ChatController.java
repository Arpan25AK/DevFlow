package com.dev.chat_service.controller;

import com.dev.chat_service.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {
    @MessageMapping("/chat/{pullRequestId}")
    @SendTo("/topic/reviews/{pullRequestId}")
    public ChatMessage sendMessage(@DestinationVariable String pullRequestId,
                                   @Payload ChatMessage chatMessage,
                                   StompHeaderAccessor headerAccessor) {

        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        chatMessage.setSenderId(userId);
        chatMessage.setSenderUsername(username);
        chatMessage.setTimeStamp(LocalDateTime.now());
        chatMessage.setPullrequestId(pullRequestId);

        return chatMessage;
    }
}