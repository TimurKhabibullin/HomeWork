package com.example.taskservice.repository;

import com.example.taskservice.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * @author Timur Khabibullin
 */
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Исполнитель подгружается одним запросом (без N+1)
     */
    @Override
    @EntityGraph(attributePaths = Task.F.ASSIGNEE)
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);

    @EntityGraph(attributePaths = Task.F.ASSIGNEE)
    Optional<Task> findWithAssigneeById(Long id);
}
