package com.dev.chat_service.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class SessionRegistry {
    private final Map<String, String> sessionToUserId = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, String userId) {
        sessionToUserId.put(sessionId, userId);
    }

    public void unregisterSession(String sessionId) {
        sessionToUserId.remove(sessionId);
    }

    public String getUserId(String sessionId) {
        return sessionToUserId.get(sessionId);
    }

    public Set<String> getActiveSessions() {
        return sessionToUserId.keySet();
    }

    public Set<String> getActiveUsers() {
        return Set.copyOf(sessionToUserId.values());
    }
}

