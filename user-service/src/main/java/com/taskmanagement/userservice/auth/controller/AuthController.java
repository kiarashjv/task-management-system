package com.taskmanagement.userservice.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanagement.userservice.auth.dto.LoginRequest;
import com.taskmanagement.userservice.auth.dto.LoginResponse;
import com.taskmanagement.userservice.auth.service.IAuthService;
import com.taskmanagement.userservice.user.dto.UserResponse;
import com.taskmanagement.userservice.user.model.User;
import com.taskmanagement.userservice.user.service.IUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;
    private final IUserService userService;

    public AuthController(IAuthService authService, IUserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.authenticateAndGetToken(loginRequest.getUsername(), loginRequest.getPassword());
        User user = userService.getUserByUsername(loginRequest.getUsername());
        UserResponse userResponse = new UserResponse(user);
        return ResponseEntity.ok(new LoginResponse(token, userResponse));
    }

}
