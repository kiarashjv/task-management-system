package com.taskmanagement.userservice.task.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.userservice.security.SecurityConfigTest;
import com.taskmanagement.userservice.task.dto.TaskRequest;
import com.taskmanagement.userservice.task.model.Priority;
import com.taskmanagement.userservice.task.model.Status;
import com.taskmanagement.userservice.task.model.Task;
import com.taskmanagement.userservice.task.service.ITaskService;
import com.taskmanagement.userservice.user.model.User;
import com.taskmanagement.userservice.user.service.IUserService;

@WebMvcTest(TaskController.class)
@Import(SecurityConfigTest.class)
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ITaskService taskService;

    @MockBean
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // Create Task
    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateTask_thenReturns201() throws Exception {
        UUID userId = UUID.randomUUID();
        Date futureDate = Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        TaskRequest taskRequest = new TaskRequest("Task 1", "Description 1", Status.TODO, Priority.LOW, futureDate, userId);

        Task createdTask = new Task(UUID.randomUUID(), "Task 1", "Description 1", Status.TODO, Priority.LOW, futureDate, null, null);
        when(taskService.createTask(any(Task.class))).thenReturn(createdTask);

        // Mock the userService.getUserById() method
        when(userService.getUserById(userId)).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdTask.getId().toString()))
                .andExpect(jsonPath("$.title").value(createdTask.getTitle()))
                .andExpect(jsonPath("$.description").value(createdTask.getDescription()))
                .andExpect(jsonPath("$.status").value(createdTask.getStatus().toString()))
                .andExpect(jsonPath("$.priority").value(createdTask.getPriority().toString()))
                .andExpect(jsonPath("$.dueDate").exists())
                .andExpect(jsonPath("$.assignedUser").isEmpty())
                .andExpect(jsonPath("$.createdBy").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateTaskWithInvalidData_thenReturns400() throws Exception {
        UUID userId = UUID.randomUUID();
        Date pastDate = Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        TaskRequest taskRequest = new TaskRequest(null, null, null, null, pastDate, userId);

        when(userService.getUserById(userId)).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title is required"))
                .andExpect(jsonPath("$.description").value("Description is required"))
                .andExpect(jsonPath("$.status").value("Status is required"))
                .andExpect(jsonPath("$.priority").value("Priority is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateTaskNonExistentUser_thenReturns404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        Date futureDate = Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        TaskRequest taskRequest = new TaskRequest("Task 1", "Description 1", Status.TODO, Priority.LOW, futureDate, nonExistentUserId);

        when(userService.getUserById(nonExistentUserId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(taskService, never()).createTask(any());

    }

    @Test
    @WithMockUser(roles = "USER")
    void whenCreateTaskWithUserRole_thenReturns403() throws Exception {
        UUID userId = UUID.randomUUID();
        Date futureDate = Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        TaskRequest taskRequest = new TaskRequest("Task 1", "Description 1", Status.TODO, Priority.LOW, futureDate, userId);

        when(userService.getUserById(userId)).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isForbidden());

        verify(taskService, never()).createTask(any());
    }

    // Update task
    @Test
    @WithMockUser(roles = "ADMIN")
    void whenUpdateTask_thenReturns200() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Date futureDate = Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        TaskRequest taskRequest = new TaskRequest("Task 1", "Description 1", Status.TODO, Priority.LOW, futureDate, userId);

        User mockUser = new User();
        mockUser.setId(userId);

        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));

        Task updatedTask = new Task(taskId, "Task 1", "Description 1", Status.TODO, Priority.LOW, futureDate, mockUser, null);

        when(taskService.updateTask(eq(taskId), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Task 1"))
                .andExpect(jsonPath("$.description").value("Description 1"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.dueDate").exists())
                .andExpect(jsonPath("$.assignedUser").exists())
                .andExpect(jsonPath("$.createdBy").isEmpty());

        verify(userService).getUserById(userId);
        verify(taskService).updateTask(eq(taskId), any(Task.class));
    }

}
