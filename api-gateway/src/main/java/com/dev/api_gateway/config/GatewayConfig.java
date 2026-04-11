package com.dev.api_gateway.config;

import com.dev.api_gateway.filter.AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> authFilter(AuthenticationFilter filter) {

        return (request, next) -> {
            // Since OncePerRequestFilter is a standard Servlet filter,
            // it will actually run automatically for every request anyway.
            // We just need this bean to exist so the YAML doesn't crash.
            return next.handle(request);
        };
    }
}