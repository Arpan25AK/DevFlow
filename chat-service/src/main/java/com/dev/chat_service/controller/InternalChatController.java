package com.dev.chat_service.controller;

import com.dev.chat_service.entity.ChatMessageDocument;
import com.dev.chat_service.repo.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal, service-to-service only. Do NOT add a gateway route for this path.
 * As long as this stays outside the /api/** prefix the gateway proxies, the
 * browser can never reach it directly.
 *
 * In production, this service's port should additionally not be reachable from
 * outside the internal docker/network - the path prefix alone is not a security
 * boundary, just a convenience for local dev where everything shares a network.
 */
@RestController
@RequestMapping("/internal/chat")
@RequiredArgsConstructor
@Slf4j
public class InternalChatController {

    private final ChatMessageRepository chatMessageRepository;

    private static final String DELETED_USER_LABEL = "deleted user";

    // Keyed by senderId, not senderUsername - senderId is the stable userId,
    // so this stays correct even if the user renamed themselves before deleting.
    @PatchMapping("/anonymize/{senderId}")
    public ResponseEntity<Void> anonymizeSender(@PathVariable String senderId) {

        List<ChatMessageDocument> messages = chatMessageRepository.findBySenderId(senderId);

        messages.forEach(m -> m.setSenderUsername(DELETED_USER_LABEL));

        chatMessageRepository.saveAll(messages);

        log.info("anonymized {} chat messages for deleted account (senderId={})", messages.size(), senderId);

        return ResponseEntity.noContent().build();
    }
}