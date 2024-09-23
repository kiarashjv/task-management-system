package com.taskmanagement.userservice.task.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.userservice.security.SecurityConfigTest;
import com.taskmanagement.userservice.security.WithMockJwt;
import com.taskmanagement.userservice.task.dto.TaskRequest;
import com.taskmanagement.userservice.task.exception.TaskNotFoundException;
import com.taskmanagement.userservice.task.model.Priority;
import com.taskmanagement.userservice.task.model.Status;
import com.taskmanagement.userservice.task.model.Task;
import com.taskmanagement.userservice.task.service.ITaskService;
import com.taskmanagement.userservice.user.model.Role;
import com.taskmanagement.userservice.user.model.User;
import com.taskmanagement.userservice.user.service.IUserService;

@WebMvcTest(TaskController.class)
@Import(SecurityConfigTest.class)
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "taskService")
    private ITaskService taskService;

    @MockBean
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // Create Task
    @Test
    @WithMockJwt(roles = "ADMIN")
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
    @WithMockJwt(roles = "ADMIN")
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
    @WithMockJwt(roles = "ADMIN")
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
    @WithMockJwt(roles = "USER")
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
    @WithMockJwt(roles = "ADMIN")
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

    @Test
    @WithMockJwt(username = "adminuser", roles = {"ADMIN"})
    void whenAdminUpdatesAnyTask_thenReturns200() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Date upcomingDate = Date.from(LocalDateTime.now().plusDays(1)
                .atZone(ZoneId.systemDefault()).toInstant());

        TaskRequest taskRequest = new TaskRequest("Admin Update", "Admin Description",
                Status.IN_PROGRESS, Priority.HIGH, upcomingDate, userId);

        User assignedUser = new User();
        assignedUser.setId(userId);
        assignedUser.setUsername("regularuser");
        assignedUser.setRoles(Set.of(Role.USER));

        Task updatedTask = new Task(taskId, "Admin Update", "Admin Description",
                Status.IN_PROGRESS, Priority.HIGH, upcomingDate, assignedUser, null);

        when(userService.getUserById(userId)).thenReturn(Optional.of(assignedUser));
        when(taskService.updateTask(eq(taskId), any(Task.class))).thenReturn(updatedTask);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Admin Update"))
                .andExpect(jsonPath("$.description").value("Admin Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").exists())
                .andExpect(jsonPath("$.assignedUser.username").value("regularuser"));

        verify(taskService).updateTask(eq(taskId), any(Task.class));
    }

    @Test
    @WithMockJwt(username = "regularuser", roles = {"USER"})
    void whenUserUpdatesOwnTask_thenReturns200() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String username = "regularuser";
        Date upcomingDate = Date.from(LocalDateTime.now().plusDays(1)
                .atZone(ZoneId.systemDefault()).toInstant());

        TaskRequest taskRequest = new TaskRequest("User Updated Task", "User Updated Description",
                Status.IN_PROGRESS, Priority.MEDIUM, upcomingDate, userId);

        User assignedUser = new User();
        assignedUser.setId(userId);
        assignedUser.setUsername("regularuser");
        assignedUser.setRoles(Set.of(Role.USER));

        Task updatedTask = new Task(taskId, "User Updated Task", "User Updated Description",
                Status.IN_PROGRESS, Priority.MEDIUM, upcomingDate, assignedUser, null);

        when(userService.getUserById(userId)).thenReturn(Optional.of(assignedUser));
        when(taskService.updateTask(eq(taskId), any(Task.class))).thenReturn(updatedTask);
        when(taskService.isTaskAssignedToUser(username, taskId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("User Updated Task"))
                .andExpect(jsonPath("$.description").value("User Updated Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.dueDate").exists())
                .andExpect(jsonPath("$.assignedUser").exists());

        verify(taskService).updateTask(eq(taskId), any(Task.class));
        verify(taskService).isTaskAssignedToUser(username, taskId);
    }

    @Test
    @WithMockJwt(username = "otheruser", roles = {"USER"})
    void whenUserUpdatesOthersTask_thenReturns403() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String actingUsername = "otheruser";
        Date upcomingDate = Date.from(LocalDateTime.now().plusDays(1)
                .atZone(ZoneId.systemDefault()).toInstant());

        TaskRequest taskRequest = new TaskRequest("Unauthorized Update", "Should Fail",
                Status.IN_PROGRESS, Priority.HIGH, upcomingDate, userId);

        User assignedUser = new User();
        assignedUser.setId(userId);
        assignedUser.setUsername("regularuser");
        assignedUser.setRoles(Set.of(Role.USER));

        when(taskService.isTaskAssignedToUser(actingUsername, taskId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isForbidden());

        verify(taskService, never()).updateTask(eq(taskId), any(Task.class));
        verify(taskService).isTaskAssignedToUser(actingUsername, taskId);
    }

    @Test
    void whenUnauthenticatedUserUpdatesTask_thenReturns401() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TaskRequest taskRequest = new TaskRequest("Unauthenticated Update", "Should Fail",
                Status.IN_PROGRESS, Priority.MEDIUM, new Date(), userId);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isUnauthorized());

        verify(taskService, never()).updateTask(any(UUID.class), any(Task.class));
    }

    // Delete task
    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenDeleteTaskAsAdmin_thenReturns204() throws Exception {
        UUID taskId = UUID.randomUUID();

        // Assuming the task exists, no exception is thrown
        doNothing().when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(taskId);
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenDeleteNonExistentTaskAsAdmin_thenReturns404() throws Exception {
        UUID taskId = UUID.randomUUID();

        // Simulate TaskNotFoundException when deleting
        doThrow(new TaskNotFoundException("Task not found")).when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound());

        verify(taskService).deleteTask(taskId);
    }

    @Test
    void whenDeleteTaskAsUnauthenticated_thenReturns401() throws Exception {
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isUnauthorized());

        verify(taskService, never()).deleteTask(any(UUID.class));
    }

    // Get task
    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenGetAllTasksAsAdmin_thenReturns200WithTasks() throws Exception {
        List<Task> tasks = List.of(
                new Task(UUID.randomUUID(), "Task 1", "Description 1", Status.TODO, Priority.LOW, new Date(), null, null),
                new Task(UUID.randomUUID(), "Task 2", "Description 2", Status.IN_PROGRESS, Priority.HIGH, new Date(), null, null)
        );
        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));

        verify(taskService).getAllTasks();
    }

    @Test
    @WithMockJwt(username = "assignedUser", roles = "USER")
    void whenGetAssignedTaskAsUser_thenReturns200() throws Exception {
        UUID taskId = UUID.randomUUID();
        User assignedUser = new User();
        assignedUser.setId(UUID.randomUUID());
        assignedUser.setUsername("assignedUser");

        Task task = new Task(taskId, "User Task", "User Description", Status.TODO, Priority.MEDIUM, new Date(), assignedUser, null);
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task));
        when(taskService.isTaskAssignedToUser("assignedUser", taskId)).thenReturn(true);

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("User Task"))
                .andExpect(jsonPath("$.assignedUser.username").value("assignedUser"));

        verify(taskService).getTaskById(taskId);
        verify(taskService).isTaskAssignedToUser("assignedUser", taskId);
    }

    @Test
    @WithMockJwt(roles = "USER")
    void whenGetAllTasksAsUser_thenReturns403() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());

        verify(taskService, never()).getAllTasks();
    }

    @Test
    void whenGetAllTasksAsUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        verify(taskService, never()).getAllTasks();
    }

}
