package com.task.app.demo.controller;

import com.task.app.demo.dto.TaskRequest;
import com.task.app.demo.dto.TaskResponse;
import com.task.app.demo.dto.UserResponse;
import com.task.app.demo.service.TaskService;
import com.task.app.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final TaskService taskService;

    public AdminController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        if (query == null || query.trim().length() < 1) {
            return ResponseEntity.ok(List.of());
        }
        List<UserResponse> users = userService.searchUsersByUsername(query);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest request) {
        try {
            TaskResponse task = taskService.createTask(request);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PutMapping("/tasks/{id}/confirm")
    public ResponseEntity<?> confirmTask(@PathVariable Long id) {
        try {
            TaskResponse task = taskService.confirmTaskCompletion(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
