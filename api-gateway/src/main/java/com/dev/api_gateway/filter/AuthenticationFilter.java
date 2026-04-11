package com.dev.api_gateway.filter;

import com.dev.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilterapply(Config config) {
        return ((exchange, chain) -> {

            // 1. Check if the Authorization header exists
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

            // 2. Extract the token (Remove "Bearer ")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                return onError(exchange, "Invalid Authorization Header format", HttpStatus.UNAUTHORIZED);
            }

            try {
                // 3. Validate the token
                jwtUtil.validateToken(authHeader);

                // 4. Extract the user ID (UUID)
                String userId = jwtUtil.extractUserId(authHeader);

                // 5. Mutate the request: Add the X-User-Id header dynamically!
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("X-User-Id", userId)
                                .build())
                        .build();

                // 6. Pass the modified request down to the microservices
                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                return onError(exchange, "Unauthorized access to application", HttpStatus.FORBIDDEN);
            }
        });
    }

    // Helper to return clean 401/403 responses if the token is invalid
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Put configuration properties here if needed later
    }
}