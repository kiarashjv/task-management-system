package com.taskmanagement.userservice.auth.service;

public interface IAuthService {
    String authenticateAndGetToken(String username, String password);
}
