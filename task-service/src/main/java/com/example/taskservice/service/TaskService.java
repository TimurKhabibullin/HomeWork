package com.example.taskservice.service;

import com.example.taskservice.constants.TaskStatus;
import com.example.taskservice.dto.CreateTaskDto;
import com.example.taskservice.dto.PageDto;
import com.example.taskservice.dto.TaskDto;
import com.example.taskservice.dto.event.TaskAssignedEvent;
import com.example.taskservice.dto.event.TaskCreatedEvent;
import com.example.taskservice.entity.Task;
import com.example.taskservice.entity.User;
import com.example.taskservice.exception.ConflictException;
import com.example.taskservice.exception.NotFoundException;
import com.example.taskservice.repository.TaskRepository;
import com.example.taskservice.repository.UserRepository;
import com.example.taskservice.transformer.TaskTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Бизнес-логика задач. События публикуются как Spring-события и уходят в Kafka после коммита транзакции.
 *
 * @author Timur Khabibullin
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repo;

    private final UserRepository userRepo;

    private final TaskTransformer t;

    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public PageDto<TaskDto> list(TaskStatus status, Long assigneeId, Pageable pageable) {
        Specification<Task> spec = Specification.unrestricted();

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get(Task.F.STATUS), status));
        }
        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get(Task.F.ASSIGNEE).get(User.F.ID), assigneeId));
        }

        return PageDto.from(repo.findAll(spec, pageable).map(t::toDto));
    }

    @Transactional(readOnly = true)
    public TaskDto get(Long id) {
        return t.toDto(findTask(id));
    }

    @Transactional
    public TaskDto create(CreateTaskDto dto) {
        var assignee = dto.getAssigneeId() == null ? null : findUser(dto.getAssigneeId());
        var task = repo.saveAndFlush(new Task()
                .setTitle(dto.getTitle())
                .setDescription(dto.getDescription())
                .setAssignee(assignee));

        publisher.publishEvent(TaskCreatedEvent.of(task.getId(), task.getTitle(), dto.getAssigneeId()));

        return t.toDto(task);
    }

    @Transactional
    public TaskDto assign(Long taskId, Long userId) {
        var task = findTask(taskId);
        var assignee = findUser(userId);
        var previousAssigneeId = task.getAssignee() == null ? null : task.getAssignee().getId();

        if (Objects.equals(previousAssigneeId, userId)) {
            // ничего не изменилось — событие не нужно
            return t.toDto(task);
        }

        repo.saveAndFlush(task.setAssignee(assignee));
        publisher.publishEvent(TaskAssignedEvent.of(task.getId(), userId, previousAssigneeId));

        return t.toDto(task);
    }

    @Transactional
    public TaskDto changeStatus(Long taskId, TaskStatus newStatus) {
        var task = findTask(taskId);

        if (task.getStatus() == newStatus) {
            return t.toDto(task);
        }
        if (!task.getStatus().canTransitionTo(newStatus)) {
            throw new ConflictException("Переход из статуса %s в %s не разрешён".formatted(task.getStatus(), newStatus));
        }

        repo.saveAndFlush(task.setStatus(newStatus));

        return t.toDto(task);
    }

    private Task findTask(Long id) {
        return repo.findWithAssigneeById(id)
                   .orElseThrow(() -> new NotFoundException("Задача %d не найдена".formatted(id)));
    }

    private User findUser(Long id) {
        return userRepo.findById(id)
                       .orElseThrow(() -> new NotFoundException("Пользователь %d не найден".formatted(id)));
    }
}
