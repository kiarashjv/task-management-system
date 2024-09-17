package com.taskmanagement.userservice.user.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.userservice.security.SecurityConfigTest;
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
    @WithMockUser
    void whenCreateUser_thenReturns200() throws Exception {
        Set<Role> roles = new HashSet<>(Arrays.asList(Role.USER));
        User user = new User("testuser", "password", "test@example.com", roles);
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles", hasItem("USER")));
    }

    @Test
    @WithMockUser
    void whenCreateUserWithMultipleRoles_thenReturns200() throws Exception {
        Set<Role> roles= new HashSet<>(Arrays.asList(Role.USER,Role.ADMIN));
        User user = new User("adminuser", "password", "admin@example.com", roles);
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.roles", hasItem("USER")))
                .andExpect(jsonPath("$.roles", hasItem("ADMIN")));
    }

    @Test
    @WithMockUser
    void whenCreateInvalidUser_thenReturns400() throws Exception {
        User invalidUser = new User();
        // Omitting required fields to trigger validation error

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"))
                .andExpect(jsonPath("$.password").value("Password is required"))
                .andExpect(jsonPath("$.email").value("Email is required"))
                .andExpect(jsonPath("$.roles").doesNotExist());
    }
}
