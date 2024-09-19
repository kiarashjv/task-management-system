package com.taskmanagement.userservice.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskmanagement.userservice.task.model.Task;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

}
