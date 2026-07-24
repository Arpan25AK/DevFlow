package com.dev.chat_service.controller;

import com.dev.chat_service.repo.ChatMessageRepository;
import com.dev.chat_service.entity.ChatMessageDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/chat")
@RequiredArgsConstructor
@Slf4j
public class InternalChatController {

    private final ChatMessageRepository chatMessageRepository;

    private static final String DELETED_USER_LABEL = "deleted user";

    // POST, not PATCH: Feign's default HTTP client (java.net.HttpURLConnection)
    // cannot send PATCH requests at all (JDK-level restriction), so internal
    // service-to-service calls use POST here even though this is semantically
    // an update. Only matters for internal calls - never exposed externally.
    @PostMapping("/anonymize/{senderId}")
    public ResponseEntity<Void> anonymizeSender(@PathVariable String senderId) {

        List<ChatMessageDocument> messages = chatMessageRepository.findBySenderId(senderId);

        messages.forEach(m -> m.setSenderUsername(DELETED_USER_LABEL));

        chatMessageRepository.saveAll(messages);

        log.info("anonymized {} chat messages for deleted account (senderId={})", messages.size(), senderId);

        return ResponseEntity.noContent().build();
    }
}