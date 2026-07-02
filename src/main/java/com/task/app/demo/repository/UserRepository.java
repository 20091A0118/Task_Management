package com.task.app.demo.repository;

import com.task.app.demo.entity.User;
import com.task.app.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // Search users by matching username prefix/part and role USER (to assign tasks to normal users)
    List<User> findByUsernameContainingIgnoreCaseAndRole(String username, Role role);

    List<User> findByCreatedBy(User admin);

    List<User> findByUsernameContainingIgnoreCaseAndRoleAndCreatedBy(String username, Role role, User admin);

    List<User> findByRole(Role role);

    boolean existsByUsername(String username);
}
