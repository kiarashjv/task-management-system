package com.taskmanagement.userservice.security;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

@Component
public class WithMockJwtSecurityContextFactory implements WithSecurityContextFactory<WithMockJwt> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwt annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Convert roles to GrantedAuthority with 'ROLE_' prefix
        List<GrantedAuthority> authorities = List.of(annotation.roles())
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // Build JWT token
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none") // No signing algorithm for testing
                .claim("sub", annotation.username())
                .claim("roles", List.of(annotation.roles()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Create Authentication Token
        org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken authentication
                = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt, authorities);

        context.setAuthentication(authentication);
        return context;
    }
}
