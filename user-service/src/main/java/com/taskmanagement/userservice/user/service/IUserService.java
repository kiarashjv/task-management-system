package com.taskmanagement.userservice.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.taskmanagement.userservice.user.model.User;
public interface IUserService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    User createUser(User user);
    Optional<User> getUserById(UUID id);
    User updateUser(UUID id, User user);
    void deleteUser(UUID id);
    List<User> getAllUsers();

}
