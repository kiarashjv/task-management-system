package com.taskmanagement.userservice.task.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanagement.userservice.task.dto.TaskRequest;
import com.taskmanagement.userservice.task.dto.TaskResponse;
import com.taskmanagement.userservice.task.exception.TaskNotFoundException;
import com.taskmanagement.userservice.task.model.Task;
import com.taskmanagement.userservice.task.service.ITaskService;
import com.taskmanagement.userservice.user.exception.UserNotFoundException;
import com.taskmanagement.userservice.user.service.IUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final ITaskService taskService;
    private final IUserService userService;

    public TaskController(ITaskService taskService, IUserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        logger.info("Received request to create task: {}", taskRequest.getTitle());
        Task task = convertToTask(taskRequest);
        Task createdTask = taskService.createTask(task);
        logger.info("Task created successfully with ID: {}", createdTask.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TaskResponse(createdTask));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @taskService.isTaskAssignedToUser(authentication.name, #id))")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID id, @Valid @RequestBody TaskRequest taskRequest) {
        logger.info("Received request to update task with ID: {}", id);
        Task task = convertToTask(taskRequest);
        Task updatedTask = taskService.updateTask(id, task);
        if (updatedTask == null) {
            logger.warn("Task with ID: {} not found", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Task updated successfully with ID: {}", id);
        return ResponseEntity.ok(new TaskResponse(updatedTask));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        logger.info("Received request to delete task with ID: {}", id);
        try {
            taskService.deleteTask(id);
            logger.info("Task deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (TaskNotFoundException e) {
            logger.warn("Failed to delete task: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @taskService.isTaskAssignedToUser(authentication.name, #id))")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID id) {
        logger.info("Received request to get task with ID: {}", id);
        return taskService.getTaskById(id)
                .map(task -> ResponseEntity.ok(new TaskResponse(task)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        logger.info("Received request to get all tasks");
        List<Task> tasks = taskService.getAllTasks();
        List<TaskResponse> taskResponses = tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
        logger.info("Retrieved {} tasks", taskResponses.size());
        return ResponseEntity.ok(taskResponses);
    }

    private Task convertToTask(TaskRequest taskRequest) {
        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setStatus(taskRequest.getStatus());
        task.setPriority(taskRequest.getPriority());
        task.setDueDate(taskRequest.getDueDate());
        task.setAssignedUser(userService.getUserById(taskRequest.getAssignedUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found")));
        return task;
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<TaskResponse> handleTaskNotFoundException(TaskNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new TaskResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<TaskResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new TaskResponse(ex.getMessage()));
    }
}
