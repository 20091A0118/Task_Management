package com.task.app.demo.service;

import com.task.app.demo.dto.RegisterRequest;
import com.task.app.demo.dto.UserResponse;
import com.task.app.demo.entity.Role;
import com.task.app.demo.entity.User;
import com.task.app.demo.repository.UserRepository;
import com.task.app.demo.repository.TaskRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TaskRepository taskRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersByUsername(String query) {
        // We only want to search users who have the role ROLE_USER so admin can assign them tasks
        return userRepository.findByUsernameContainingIgnoreCaseAndRole(query, Role.ROLE_USER)
                .stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
    }

}
