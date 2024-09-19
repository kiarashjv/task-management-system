package com.taskmanagement.userservice.task.dto;

import com.taskmanagement.userservice.task.model.Status;
import com.taskmanagement.userservice.task.model.Priority;
import com.taskmanagement.userservice.user.dto.UserResponse;

import java.util.Date;
import java.util.UUID;
import java.time.LocalDateTime;

import com.taskmanagement.userservice.task.model.Task;

public class TaskResponse {

    private final UUID id;
    private final String title;
    private final String description;
    private final Status status;
    private final Priority priority;
    private final Date dueDate;
    private final UserResponse assignedUser;
    private final UserResponse createdBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String message;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.dueDate = task.getDueDate();
        this.assignedUser = task.getAssignedUser() != null ? new UserResponse(task.getAssignedUser()) : null;
        this.createdBy = task.getCreatedBy() != null ? new UserResponse(task.getCreatedBy()) : null;
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
        this.message = null;
    }

    public TaskResponse(String message) {
        this.id = null;
        this.title = null;
        this.description = null;
        this.status = null;
        this.priority = null;
        this.dueDate = null;
        this.assignedUser = null;
        this.createdBy = null;
        this.createdAt = null;
        this.updatedAt = null;
        this.message = message;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public Priority getPriority() {
        return priority;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public UserResponse getAssignedUser() {
        return assignedUser;
    }

    public UserResponse getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getMessage() {
        return message;
    }
    // No setters to ensure immutability
}
