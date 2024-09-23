package com.taskmanagement.userservice.user.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.userservice.security.SecurityConfigTest;
import com.taskmanagement.userservice.security.WithMockJwt;
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
    @WithMockJwt(roles = "ADMIN")
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
    @WithMockJwt(roles = "ADMIN")
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

        verify(userService).createUser(argThat(user
                -> user.getUsername().equals("adminuser")
                && user.getPassword().equals("password")
                && user.getEmail().equals("admin@example.com")
                && user.getRoles().containsAll(roles)
        ));
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
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
    @WithMockJwt(roles = "ADMIN")
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
    @WithMockJwt(roles = "ADMIN")
    void whenGetUserById_thenReturns404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        when(userService.getUserById(nonExistentUserId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(nonExistentUserId);
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
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
        verify(userService).updateUser(eq(userId), argThat(user
                -> user.getUsername().equals("updateduser")
                && user.getPassword().equals("newpassword")
                && user.getEmail().equals("updated@example.com")
                && user.getRoles().contains(Role.USER)
        ));
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
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
    @WithMockJwt(roles = "ADMIN")
    void whenUpdateUserWithInvalidData_thenReturns400() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRequest invalidUserRequest = new UserRequest("", "", "invalid-email", null);

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"))
                .andExpect(jsonPath("$.password").value(containsInAnyOrder(
                        "Password is required",
                        "Password must be at least 8 characters long"
                )))
                .andExpect(jsonPath("$.email").value("Email should be valid"))
                .andExpect(jsonPath("$.roles").value("User must have at least one role"));

        verify(userService, never()).updateUser(eq(userId), any(User.class));
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenDeleteUser_thenReturns204() throws Exception {
        UUID userId = UUID.randomUUID();

        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        // Verify that the userService was called with the correct argument
        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenDeleteNonExistentUser_thenReturns404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(nonExistentUserId);

        mockMvc.perform(delete("/api/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(nonExistentUserId);
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
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
    @WithMockJwt(roles = "ADMIN")
    void whenGetAllUsers_thenReturns200WithEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenCreateUserWithExistingUsername_thenReturns400() throws Exception {
        UserRequest userRequest = new UserRequest("existinguser", "password", "existing@example.com", new HashSet<>(Arrays.asList(Role.USER)));

        when(userService.createUser(any(User.class))).thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User already exists"));

        verify(userService).createUser(argThat(user
                -> user.getUsername().equals("existinguser")
                && user.getEmail().equals("existing@example.com")
                && user.getRoles().contains(Role.USER)
        ));
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenCreateUserWithInvalidEmail_thenReturns400() throws Exception {
        UserRequest userRequest = new UserRequest("testuser", "password", "invalid-email", new HashSet<>(Arrays.asList(Role.USER)));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email should be valid"));

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenCreateUserWithTooShortPassword_thenReturns400() throws Exception {
        UserRequest userRequest = new UserRequest("testuser", "short", "test@example.com", new HashSet<>(Arrays.asList(Role.USER)));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password must be at least 8 characters long"));

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockJwt(roles = "ADMIN")
    void whenCreateUserWithoutAnyRoles_thenReturns400() throws Exception {
        UserRequest userRequest = new UserRequest("testuser", "password", "test@example.com", new HashSet<>());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.roles").value("User must have at least one role"));

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockJwt(roles = "USER")
    void whenGetAllUsers_thenReturns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockJwt(roles = "USER")
    void whenCreateUser_thenReturns403() throws Exception {
        UserRequest userRequest = new UserRequest("testuser", "password", "test@example.com", new HashSet<>(Arrays.asList(Role.USER)));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockJwt(username = "testuser", roles = "USER")
    void whenUpdateUser_thenReturns403() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRequest userRequest = new UserRequest("updateduser", "newpassword", "updated@example.com", new HashSet<>(Arrays.asList(Role.USER)));

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockJwt(roles = "USER")
    void whenDeleteUser_thenReturns403() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenAccessEndpointWithoutAuthentication_thenReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserRequest("testuser", "password", "test@example.com", new HashSet<>(Arrays.asList(Role.USER))))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserRequest("updateduser", "newpassword", "updated@example.com", new HashSet<>(Arrays.asList(Role.USER))))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
