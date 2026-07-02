package com.task.app.demo.service;

import com.task.app.demo.dto.TaskRequest;
import com.task.app.demo.dto.TaskResponse;
import com.task.app.demo.entity.Task;
import com.task.app.demo.entity.TaskStatus;
import com.task.app.demo.entity.User;
import com.task.app.demo.repository.TaskRepository;
import com.task.app.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, User admin) {
        User assignedUser = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));

        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                TaskStatus.PENDING,
                assignedUser,
                request.getDueDate()
        );
        task.setCreatedBy(admin);

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAdmin(User admin) {
        return taskRepository.findByCreatedByOrderByCreatedAtDesc(admin)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return taskRepository.findByAssignedUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request, User admin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getCreatedBy() == null || !task.getCreatedBy().getId().equals(admin.getId())) {
            throw new SecurityException("Unauthorized to modify this task");
        }

        User assignedUser = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedUser(assignedUser);
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long taskId, User admin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getCreatedBy() == null || !task.getCreatedBy().getId().equals(admin.getId())) {
            throw new SecurityException("Unauthorized to delete this task");
        }

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, String statusStr) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Standard user can only update tasks assigned to them. Admin can update any task they created.
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");
        boolean isAssignedUser = task.getAssignedUser().getId().equals(currentUser.getId());

        if (isAdmin) {
            if (task.getCreatedBy() == null || !task.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new SecurityException("Unauthorized to modify this task");
            }
        } else if (!isAssignedUser) {
            throw new SecurityException("Unauthorized to modify this task");
        }

        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid task status: " + statusStr);
        }

        // Only ADMIN can mark task as CONFIRMED
        if (newStatus == TaskStatus.CONFIRMED && !isAdmin) {
            throw new SecurityException("Only admin can confirm task completion");
        }

        // If task is already CONFIRMED, standard user cannot modify it anymore
        if (task.getStatus() == TaskStatus.CONFIRMED && !isAdmin) {
            throw new IllegalStateException("Confirmed tasks cannot be modified");
        }

        task.setStatus(newStatus);
        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public TaskResponse confirmTaskCompletion(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (task.getCreatedBy() == null || !task.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized to confirm this task");
        }

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("Only completed tasks can be confirmed");
        }

        task.setStatus(TaskStatus.CONFIRMED);
        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getAssignedUser().getId(),
                task.getAssignedUser().getUsername(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getDueDate(),
                task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "System"
        );
    }
}
