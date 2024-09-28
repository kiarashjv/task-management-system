package com.taskmanagement.userservice.config;

import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.taskmanagement.userservice.user.model.Role;
import com.taskmanagement.userservice.user.model.User;
import com.taskmanagement.userservice.user.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    @ConditionalOnProperty(name = "app.init-db", havingValue = "true")
    public CommandLineRunner initializeData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin"));
                adminUser.setEmail("admin@example.com");
                adminUser.setRoles(Collections.singleton(Role.ADMIN));

                userRepository.save(adminUser);

                System.out.println("Default admin user created.");
            }
        };
    }
}
