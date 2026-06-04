package com.example.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.LocalDateTime;

@Component
public class GatewayJwtFilterFactory extends AbstractGatewayFilterFactory<GatewayJwtFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(GatewayJwtFilterFactory.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    public GatewayJwtFilterFactory() {
        super(Config.class);
    }

    public static class Config {
        // Config options can be added here if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod() != null ? request.getMethod().name() : "";

            logger.info("Gateway intercepting request: {} {}", method, path);

            // Bypass token validation for public routes: GET requests to products or categories
            if ("GET".equalsIgnoreCase(method) && 
                    (path.startsWith("/api/v1/products") || path.startsWith("/api/v1/categories"))) {
                logger.info("Bypassing JWT validation for GET request on public endpoint: {}", path);
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.warn("Missing Authorization Header for secure path: {}", path);
                return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization Header format for secure path: {}", path);
                return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                logger.info("JWT validated successfully at Gateway for user: {}", claims.getSubject());

                // Mutate request headers to pass claims down to downstream services
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", String.valueOf(claims.get("userId")))
                        .header("X-User-Role", String.valueOf(claims.get("role")))
                        .header("X-User-Email", String.valueOf(claims.get("email")))
                        .header("X-User-Name", claims.getSubject())
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                logger.error("Token verification failed: {}", e.getMessage());
                return onError(exchange, "JWT Token Validation Failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String body = String.format("{\"success\":false,\"message\":\"%s\",\"errors\":[],\"timestamp\":\"%s\"}",
                err, LocalDateTime.now());
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
