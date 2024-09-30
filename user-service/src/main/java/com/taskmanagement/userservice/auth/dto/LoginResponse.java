package com.taskmanagement.userservice.auth.dto;

import com.taskmanagement.userservice.user.dto.UserResponse;

public class LoginResponse {

    private final String token;
    private final UserResponse user;

    public LoginResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserResponse getUser() {
        return user;
    }

}
