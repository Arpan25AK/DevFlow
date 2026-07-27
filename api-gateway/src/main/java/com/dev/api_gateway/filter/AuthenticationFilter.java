package com.dev.api_gateway.filter;

import com.dev.api_gateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Allow completely public GET access to avatars without authentication
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/auth/avatar")) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isPublicAuthEndpoint = path.equals("/api/auth/login") ||
                path.equals("/api/auth/signup") ||
                path.equals("/api/auth/refresh") ||
                path.equals("/api/auth/logout");

        // 2. For Avatar mutations (POST, DELETE) or other public auth endpoints / chat
        if (isPublicAuthEndpoint || path.startsWith("/api/auth/avatar") || path.startsWith("/api/chat")) {

            // If it's an avatar modification, enforce token validation
            if (path.startsWith("/api/auth/avatar")) {
                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    sendErrorResponse(response, "Missing or invalid Authorization Header", HttpStatus.UNAUTHORIZED);
                    return;
                }
                try {
                    String token = authHeader.substring(7);
                    jwtUtil.validateToken(token);
                    String userId = jwtUtil.extractUserId(token);

                    // Pass along user ID if needed via header wrapper for mutations
                    MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
                    mutableRequest.putHeader("X-User-Id", userId);
                    filterChain.doFilter(mutableRequest, response);
                    return;
                } catch (io.jsonwebtoken.JwtException e) {
                    sendErrorResponse(response, "Unauthorized access: Invalid Token", HttpStatus.FORBIDDEN);
                    return;
                }
            }

            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, "Missing or invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);
        String userId;
        String email;

        try {
            jwtUtil.validateToken(token);
            userId = jwtUtil.extractUserId(token);
            email = jwtUtil.extractEmail(token);
            System.out.println(email);
        } catch (io.jsonwebtoken.JwtException e) {
            sendErrorResponse(response, "Unauthorized access: Invalid Token", HttpStatus.FORBIDDEN);
            return;
        }

        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        mutableRequest.putHeader("X-User-Id", userId);
        mutableRequest.putHeader("X-User-Email", email);
        filterChain.doFilter(mutableRequest, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}