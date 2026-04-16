package com.dev.chat_service.controller;

import com.dev.chat_service.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {
    @MessageMapping("/chat/{pullRequestId}")
    // Server broadcasts to: /topic/reviews/{pullRequestId}
    @SendTo("/topic/reviews/{pullRequestId}")
    public ChatMessage sendMessage(@DestinationVariable String pullRequestId, @Payload ChatMessage chatMessage) {

        // Ensure the timestamp is set right as the server receives it
        chatMessage.setTimeStamp(LocalDateTime.now());
        chatMessage.setPullrequestId(pullRequestId);

        return chatMessage; // Automatically broadcasted to subscribers!
    }
}
