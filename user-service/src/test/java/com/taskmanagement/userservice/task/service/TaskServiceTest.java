package com.taskmanagement.userservice.task.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.taskmanagement.userservice.task.model.Priority;
import com.taskmanagement.userservice.task.model.Status;
import com.taskmanagement.userservice.task.model.Task;
import com.taskmanagement.userservice.task.repository.TaskRepository;
import com.taskmanagement.userservice.user.model.User;
import com.taskmanagement.userservice.user.service.IUserService;

class TaskServiceTest {

    private static final String TEST_TITLE = "Test Task";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final Status TEST_STATUS = Status.TODO;
    private static final Priority TEST_PRIORITY = Priority.LOW;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private IUserService userService;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenCreateTask_thenTaskIsSaved() {
        // Arrange
        User assignedUser = new User();
        assignedUser.setId(UUID.randomUUID());

        Task task = new Task();
        task.setTitle(TEST_TITLE);
        task.setDescription(TEST_DESCRIPTION);
        task.setStatus(TEST_STATUS);
        task.setPriority(TEST_PRIORITY);
        task.setDueDate(new Date(LocalDate.now().plusDays(1).toEpochDay()));
        task.setAssignedUser(assignedUser);

        when(userService.getUserById(assignedUser.getId())).thenReturn(Optional.of(assignedUser));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task savedTask = taskService.createTask(task);

        // Assert
        assertNotNull(savedTask);
        assertEquals(TEST_TITLE, savedTask.getTitle());
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription());
        assertEquals(TEST_STATUS, savedTask.getStatus());
        assertEquals(TEST_PRIORITY, savedTask.getPriority());
        assertEquals(new Date(LocalDate.now().plusDays(1).toEpochDay()), savedTask.getDueDate());
        assertEquals(assignedUser, savedTask.getAssignedUser());
        verify(userService).getUserById(assignedUser.getId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void whenCreateTask_thenAssignedUserNotFound() {
        // Arrange
        User nonExistentUser = new User();
        nonExistentUser.setId(UUID.randomUUID());

        Task task = new Task();
        task.setTitle(TEST_TITLE);
        task.setDescription(TEST_DESCRIPTION);
        task.setStatus(TEST_STATUS);
        task.setPriority(TEST_PRIORITY);
        task.setDueDate(new Date(LocalDate.now().plusDays(1).toEpochDay()));
        task.setAssignedUser(nonExistentUser);

        // Act
        when(userService.getUserById(any(UUID.class))).thenReturn(Optional.empty());

        // Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.createTask(task));
        assertEquals("Assigned user not found", exception.getMessage());
        verify(userService).getUserById(nonExistentUser.getId());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void whenGetTaskById_thenTaskIsReturned() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setTitle(TEST_TITLE);
        task.setDescription(TEST_DESCRIPTION);
        task.setStatus(TEST_STATUS);
        task.setPriority(TEST_PRIORITY);
        Date dueDate = new Date(LocalDate.now().plusDays(1).toEpochDay());
        task.setDueDate(dueDate);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        Optional<Task> retrievedTask = taskService.getTaskById(taskId);

        // Assert
        assertTrue(retrievedTask.isPresent());
        Task actualTask = retrievedTask.get();
        assertEquals(taskId, actualTask.getId());
        assertEquals(TEST_TITLE, actualTask.getTitle());
        assertEquals(TEST_DESCRIPTION, actualTask.getDescription());
        assertEquals(TEST_STATUS, actualTask.getStatus());
        assertEquals(TEST_PRIORITY, actualTask.getPriority());
        assertEquals(dueDate, actualTask.getDueDate());
        verify(taskRepository).findById(taskId);
    }

    @Test
    void whenGetTaskById_thenTaskNotFound() {
        // Arrange
        UUID nonExistentTaskId = UUID.randomUUID();

        // Act
        when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

        // Assert
        Optional<Task> retrievedTask = taskService.getTaskById(nonExistentTaskId);
        assertFalse(retrievedTask.isPresent());
        verify(taskRepository).findById(nonExistentTaskId);
    }

    @Test
    void whenUpdateTask_thenTaskIsUpdated() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setStatus(Status.TODO);
        existingTask.setPriority(Priority.LOW);
        existingTask.setDueDate(new Date(LocalDate.now().toEpochDay()));

        Task updatedTaskDetails = new Task();
        updatedTaskDetails.setTitle(TEST_TITLE);
        updatedTaskDetails.setDescription(TEST_DESCRIPTION);
        updatedTaskDetails.setStatus(TEST_STATUS);
        updatedTaskDetails.setPriority(TEST_PRIORITY);
        Date newDueDate = new Date(LocalDate.now().plusDays(1).toEpochDay());
        updatedTaskDetails.setDueDate(newDueDate);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task updatedTask = taskService.updateTask(taskId, updatedTaskDetails);

        // Assert
        assertNotNull(updatedTask);
        assertEquals(taskId, updatedTask.getId());
        assertEquals(TEST_TITLE, updatedTask.getTitle());
        assertEquals(TEST_DESCRIPTION, updatedTask.getDescription());
        assertEquals(TEST_STATUS, updatedTask.getStatus());
        assertEquals(TEST_PRIORITY, updatedTask.getPriority());
        assertEquals(newDueDate, updatedTask.getDueDate());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }
    

}
