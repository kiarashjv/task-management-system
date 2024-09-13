package com.taskmanagement.userservice.service;

import org.springframework.stereotype.Service;

import com.taskmanagement.userservice.model.User;
import com.taskmanagement.userservice.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        // Add validation and password encoding logic here
        return userRepository.save(user);
    }

    // Add other user-related methods
}
