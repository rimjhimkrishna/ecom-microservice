package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = exchange.getRequest().getRemoteAddress() != null ?
                        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
            }
            // For rate limiting, if multiple IPs are forwarded, take the first one
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return Mono.just(ip);
        };
    }
}
