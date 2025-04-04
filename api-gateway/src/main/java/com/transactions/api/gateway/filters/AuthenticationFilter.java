package com.transactions.api.gateway.filters;


import com.transactions.api.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private final JwtUtil jwtUtil;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);


    public AuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Skip authentication for login request
        String requestPath = request.getURI().getPath();
        HttpMethod requestMethod = request.getMethod();

        if (requestMethod == HttpMethod.POST && (requestPath.equals("/auth/login") || requestPath.equals("/users/api/v1/"))) {
            return chain.filter(exchange);
        }

        if (requestMethod == HttpMethod.GET && requestPath.equals("/contactSupport")) {
            return chain.filter(exchange);
        }

        // Extract JWT from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange);
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                return unauthorizedResponse(exchange);
            }
        }
        catch (Exception e) {
            return unauthorizedResponse(exchange);
        }

        // Extract User ID from JWT and append to header
        String userId = jwtUtil.extractUserId(token);
        log.info("User ID: {} access the resource - {}", userId, requestPath);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Execute early in the filter chain
    }
}
