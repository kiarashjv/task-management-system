package com.taskmanagement.userservice.task.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.taskmanagement.userservice.task.model.Task;

public interface ITaskService {

    Task createTask(Task task);

    Optional<Task> getTaskById(UUID id);

    Task updateTask(UUID id, Task task);

    void deleteTask(UUID id);

    List<Task> getAllTasks();

    boolean isTaskAssignedToUser(String username, UUID taskId);

}
