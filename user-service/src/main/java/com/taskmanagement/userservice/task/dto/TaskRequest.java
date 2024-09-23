package com.taskmanagement.userservice.task.dto;

import java.util.Date;
import java.util.UUID;

import com.taskmanagement.userservice.task.model.Priority;
import com.taskmanagement.userservice.task.model.Status;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Status is required")
    private Status status;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @Future(message = "Due date must be in the future")
    private Date dueDate;

    @NotNull(message = "Assigned User ID is required")
    private UUID assignedUserId;

    // Constructors
    public TaskRequest() {
    }

    public TaskRequest(String title, String description, Status status, Priority priority, Date dueDate, UUID assignedUserId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assignedUserId = assignedUserId;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // (Repeat for other fields)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public UUID getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(UUID assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    @Override
    public String toString() {
        return "TaskRequest{"
                + "title='" + title + '\''
                + ", description='" + description + '\''
                + ", status=" + status
                + ", priority=" + priority
                + ", dueDate=" + dueDate
                + ", assignedUserId=" + assignedUserId
                + '}';
    }
}
