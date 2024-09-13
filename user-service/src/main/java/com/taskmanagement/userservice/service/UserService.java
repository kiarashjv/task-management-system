package com.taskmanagement.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.taskmanagement.userservice.model.User;
import com.taskmanagement.userservice.repository.UserRepository;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        logger.debug("Creating new user: {}", user.getUsername());
        // Add validation and password encoding logic here
        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", savedUser.getId());
        return savedUser;
    }

    // Add other user-related methods
}
