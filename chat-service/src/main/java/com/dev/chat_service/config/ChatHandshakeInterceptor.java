package com.dev.chat_service.config;

import com.dev.chat_service.service.SessionRegistry;
import com.dev.chat_service.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final SessionRegistry sessionRegistry;

    public ChatHandshakeInterceptor(JwtUtil jwtUtil, SessionRegistry sessionRegistry) {
        this.jwtUtil = jwtUtil;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // Extract token from query parameter
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            return false;
        }

        String token = query.split("token=")[1].split("&")[0];

        try {
            // Validate token
            jwtUtil.validateToken(token);
            
            // Extract userId
            String userId = jwtUtil.extractUserId(token);
            
            // Store in session attributes
            attributes.put("userId", userId);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}

