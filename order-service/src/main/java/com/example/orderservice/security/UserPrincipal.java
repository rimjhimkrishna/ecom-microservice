package com.example.orderservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final UUID id;
    private final String username;
    private final String email;
    private final String role;
}
