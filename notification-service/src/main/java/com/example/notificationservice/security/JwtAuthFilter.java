package com.example.notificationservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Check for Gateway headers first
        String headerUserId = request.getHeader("X-User-Id");
        String headerUsername = request.getHeader("X-User-Name");
        String headerRole = request.getHeader("X-User-Role");
        String headerEmail = request.getHeader("X-User-Email");

        if (headerUserId != null && headerUsername != null && headerRole != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UUID userId = UUID.fromString(headerUserId);
            UserPrincipal principal = new UserPrincipal(userId, headerUsername, headerEmail, headerRole);
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + headerRole));
            
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
            // Standalone local execution or test execution fallback
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                try {
                    Claims claims = jwtService.extractAllClaims(jwt);
                    String username = claims.getSubject();
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        String userIdString = claims.get("userId", String.class);
                        UUID userId = userIdString != null ? UUID.fromString(userIdString) : UUID.randomUUID();
                        String role = claims.get("role", String.class);
                        String email = claims.get("email", String.class);

                        UserPrincipal principal = new UserPrincipal(userId, username, email, role);
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
                        
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                principal, null, authorities
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (Exception e) {
                    // Let filter chain proceed; SecurityConfig will block unauthorized requests
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
