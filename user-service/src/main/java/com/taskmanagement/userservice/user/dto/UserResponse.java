package com.taskmanagement.userservice.user.dto;

import com.taskmanagement.userservice.user.model.Role;
import com.taskmanagement.userservice.user.model.User;

import java.util.Set;
import java.util.UUID;

public class UserResponse {
    private final UUID id;
    private final String username;
    private final String email;
    private final Set<Role> roles;
    private final String message;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roles = user.getRoles();
        this.message = null;
    }

    public UserResponse(String message) {
        this.id = null;
        this.username = null;
        this.email = null;
        this.roles = null;
        this.message = message;
    }

    // Getters (no setters to make it immutable)
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Set<Role> getRoles() { return roles; }
    public String getMessage() { return message; }
}