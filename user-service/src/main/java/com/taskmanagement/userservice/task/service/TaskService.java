package com.taskmanagement.userservice.task.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.taskmanagement.userservice.task.model.Task;
import com.taskmanagement.userservice.task.repository.TaskRepository;
import com.taskmanagement.userservice.user.service.IUserService;

@Service
public class TaskService implements ITaskService {

    private final TaskRepository taskRepository;

    private final IUserService userService;

    public TaskService(TaskRepository taskRepository, IUserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @Override
    public Task createTask(Task task) {
        if (task.getAssignedUser() != null) {
            userService.getUserById(task.getAssignedUser().getId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
        }
        return taskRepository.save(task);
    }

    @Override
    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findById(id);
    }

    @Override
    public Task updateTask(UUID id, Task task) {
        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(task.getTitle());
                    existingTask.setDescription(task.getDescription());
                    existingTask.setStatus(task.getStatus());
                    existingTask.setPriority(task.getPriority());
                    existingTask.setDueDate(task.getDueDate());
                    existingTask.setAssignedUser(task.getAssignedUser());
                    return taskRepository.save(existingTask);
                })
                .orElse(null);
    }

    @Override
    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

}
