package com.task.app.demo.repository;

import com.task.app.demo.entity.Task;
import com.task.app.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedUserOrderByCreatedAtDesc(User user);
    List<Task> findAllByOrderByCreatedAtDesc();
    List<Task> findByCreatedByOrderByCreatedAtDesc(User admin);
    void deleteByAssignedUser(User user);
}
