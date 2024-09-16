package com.taskmanagement.userservice.auth.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.Jwt;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock 
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenAuthenticateAndGetToken_thenReturnsToken() {
        // Arrange
        String username = "testuser";
        String password = "password";
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", username)
                .build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Act
        String token = authService.authenticateAndGetToken(username, password);

        // Assert
        assertNotNull(token);
        assertEquals("token", token);
    }

}
