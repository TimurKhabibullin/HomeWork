package com.example.taskservice.repository;

import com.example.taskservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Timur Khabibullin
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
