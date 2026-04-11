package com.dev.api_gateway.filter;

import com.dev.api_gateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Bypass authentication for open endpoints (like login/register in auth-service)
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Check for Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, "Missing or invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 3. Validate Token and Extract User ID
            jwtUtil.validateToken(token);
            String userId = jwtUtil.extractUserId(token);

            // 4. Mutate the request to inject the X-User-Id header for downstream microservices
            HttpServletRequest mutatedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) return userId;
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return Collections.enumeration(Collections.singletonList(userId));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = Collections.list(super.getHeaderNames());
                    names.add("X-User-Id");
                    return Collections.enumeration(names);
                }
            };

            // 5. Pass the newly mutated request down the chain
            filterChain.doFilter(mutatedRequest, response);

        } catch (Exception e) {
            sendErrorResponse(response, "Unauthorized access: Invalid Token", HttpStatus.FORBIDDEN);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}