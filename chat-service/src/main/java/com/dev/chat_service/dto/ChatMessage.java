package com.dev.chat_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatMessage {
    private String pullrequestId;
    private String senderId;
    private String content;
    private LocalDateTime timeStamp;
}
