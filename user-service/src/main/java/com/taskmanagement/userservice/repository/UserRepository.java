package com.taskmanagement.userservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskmanagement.userservice.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
}
