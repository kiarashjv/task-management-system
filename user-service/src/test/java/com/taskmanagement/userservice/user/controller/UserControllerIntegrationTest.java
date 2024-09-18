package com.taskmanagement.userservice.user.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.userservice.security.SecurityConfigTest;
import com.taskmanagement.userservice.user.dto.UserRequest;
import com.taskmanagement.userservice.user.exception.UserAlreadyExistsException;
import com.taskmanagement.userservice.user.exception.UserNotFoundException;
import com.taskmanagement.userservice.user.model.Role;
import com.taskmanagement.userservice.user.model.User;
import com.taskmanagement.userservice.user.service.IUserService;

@WebMvcTest(UserController.class)
@Import(SecurityConfigTest.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateUser_thenReturns201AndPasswordIsNotReturned() throws Exception {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        UserRequest userRequest = new UserRequest("testuser", "password123", "test@example.com", roles);
        
        User createdUser = new User("testuser", "password123", "test@example.com", roles);
        when(userService.createUser(any(User.class))).thenReturn(createdUser);
    
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles", hasItem("USER")))
                .andExpect(jsonPath("$.password").doesNotExist());
    
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateUserWithMultipleRoles_thenReturns201() throws Exception {
        Set<Role> roles = new HashSet<>(Arrays.asList(Role.USER, Role.ADMIN));
        UserRequest userRequest = new UserRequest("adminuser", "password", "admin@example.com", roles);
        User createdUser = new User("adminuser", "password", "admin@example.com", roles);
        
        when(userService.createUser(any(User.class))).thenReturn(createdUser);
    
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.roles", hasItem("USER")))
                .andExpect(jsonPath("$.roles", hasItem("ADMIN")))
                .andExpect(jsonPath("$.password").doesNotExist());
    
        verify(userService).createUser(argThat(user -> 
            user.getUsername().equals("adminuser") &&
            user.getPassword().equals("password") &&
            user.getEmail().equals("admin@example.com") &&
            user.getRoles().containsAll(roles)
        ));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateInvalidUser_thenReturns400() throws Exception {
        UserRequest invalidUserRequest = new UserRequest();
        // Omitting required fields to trigger validation error
    
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"))
                .andExpect(jsonPath("$.password").value("Password is required"))
                .andExpect(jsonPath("$.email").value("Email is required"));
    
        // Verify that the userService was not called
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void whenGetUserById_thenReturns200() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User("testuser", "password", "test@example.com", new HashSet<>(Arrays.asList(Role.USER)));
        user.setId(userId);
    
        when(userService.getUserById(userId)).thenReturn(Optional.of(user));
    
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles", hasItem("USER")))
                .andExpect(jsonPath("$.password").doesNotExist());
    
        verify(userService).getUserById(userId);
    }


    @Test
    @WithMockUser(roles="ADMIN")
    void whenGetUserById_thenReturns404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
    
        when(userService.getUserById(nonExistentUserId)).thenReturn(Optional.empty());
    
        mockMvc.perform(get("/api/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound());
    
        verify(userService).getUserById(nonExistentUserId);
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void whenUpdateUser_thenReturns200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRequest userRequest = new UserRequest("updateduser", "newpassword", "updated@example.com", new HashSet<>(Arrays.asList(Role.USER)));
    
        User updatedUser = new User("updateduser", "newpassword", "updated@example.com", new HashSet<>(Arrays.asList(Role.USER)));
        updatedUser.setId(userId);
    
        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(updatedUser);
    
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.roles", hasItem("USER")))
                .andExpect(jsonPath("$.password").doesNotExist());
    
        // Verify that the userService was called with the correct arguments
        verify(userService).updateUser(eq(userId), argThat(user -> 
            user.getUsername().equals("updateduser") &&
            user.getPassword().equals("newpassword") &&
            user.getEmail().equals("updated@example.com") &&
            user.getRoles().contains(Role.USER)
        ));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void whenUpdateNonExistentUser_thenReturns404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        UserRequest userRequest = new UserRequest("updateduser", "newpassword", "updated@example.com", new HashSet<>(Arrays.asList(Role.USER)));
    
        when(userService.updateUser(eq(nonExistentUserId), any(User.class))).thenReturn(null);
    
        mockMvc.perform(put("/api/users/{id}", nonExistentUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNotFound());
    
        verify(userService).updateUser(eq(nonExistentUserId), any(User.class));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void whenUpdateUserWithInvalidData_thenReturns400() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRequest invalidUserRequest = new UserRequest("", "", "invalid-email", null);
    
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"))
                .andExpect(jsonPath("$.password").value("Password is required"))
                .andExpect(jsonPath("$.email").value("Email should be valid"));
    
        verify(userService, never()).updateUser(eq(userId), any(User.class));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void whenDeleteUser_thenReturns204() throws Exception {
        UUID userId = UUID.randomUUID();
    
        doNothing().when(userService).deleteUser(userId);
    
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
    
        // Verify that the userService was called with the correct argument
        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void whenDeleteNonExistentUser_thenReturns404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
    
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(nonExistentUserId);
    
        mockMvc.perform(delete("/api/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound());
    
        verify(userService).deleteUser(nonExistentUserId);
    }


    @Test
    @WithMockUser(roles="ADMIN")
    void whenGetAllUsers_thenReturns200() throws Exception {
        List<User> users = Arrays.asList(
            new User("testuser1", "password1", "test1@example.com", new HashSet<>(Arrays.asList(Role.USER))),
            new User("testuser2", "password2", "test2@example.com", new HashSet<>(Arrays.asList(Role.USER))),
            new User("testuser3", "password3", "test3@example.com", new HashSet<>(Arrays.asList(Role.USER)))
        );
    
        when(userService.getAllUsers()).thenReturn(users);
    
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].username").value("testuser1"))
                .andExpect(jsonPath("$[0].email").value("test1@example.com"))
                .andExpect(jsonPath("$[0].roles", hasItem("USER")))
                .andExpect(jsonPath("$[1].username").value("testuser2"))
                .andExpect(jsonPath("$[1].email").value("test2@example.com"))
                .andExpect(jsonPath("$[1].roles", hasItem("USER")))
                .andExpect(jsonPath("$[2].username").value("testuser3"))
                .andExpect(jsonPath("$[2].email").value("test3@example.com"))
                .andExpect(jsonPath("$[2].roles", hasItem("USER")))
                .andExpect(jsonPath("$[*].password").doesNotExist());
    
        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void whenGetAllUsers_thenReturns200WithEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
    
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    
        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void whenCreateUserWithExistingUsername_thenReturns400() throws Exception {
        UserRequest userRequest = new UserRequest("existinguser", "password", "existing@example.com", new HashSet<>(Arrays.asList(Role.USER)));
    
        when(userService.createUser(any(User.class))).thenThrow(new UserAlreadyExistsException("User already exists"));
    
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User already exists"));
    
        verify(userService).createUser(argThat(user -> 
            user.getUsername().equals("existinguser") &&
            user.getEmail().equals("existing@example.com") &&
            user.getRoles().contains(Role.USER)
        ));
    }

    
    // TODO: Implement test for creating a user with an existing username
    // TODO: Implement test for creating a user with an invalid email format
    // TODO: Implement test for creating a user with a too short password
    // TODO: Implement test for creating a user without any roles
    // TODO: Implement test for accessing endpoints with insufficient permissions
    // TODO: Implement test for accessing endpoints without authentication


}
