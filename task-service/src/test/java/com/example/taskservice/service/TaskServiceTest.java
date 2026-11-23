package com.example.taskservice.service;

import com.example.taskservice.constants.TaskStatus;
import com.example.taskservice.dto.CreateTaskDto;
import com.example.taskservice.dto.event.TaskAssignedEvent;
import com.example.taskservice.dto.event.TaskCreatedEvent;
import com.example.taskservice.entity.Task;
import com.example.taskservice.entity.User;
import com.example.taskservice.exception.ConflictException;
import com.example.taskservice.exception.NotFoundException;
import com.example.taskservice.repository.TaskRepository;
import com.example.taskservice.repository.UserRepository;
import com.example.taskservice.transformer.TaskTransformerImpl;
import com.example.taskservice.transformer.UserTransformerImpl;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Timur Khabibullin
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private final EasyRandom gen = new EasyRandom();

    @Mock
    private TaskRepository repo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ApplicationEventPublisher publisher;

    private TaskService service;

    @BeforeEach
    void setUp() {
        var transformer = new TaskTransformerImpl(new UserTransformerImpl());

        service = new TaskService(repo, userRepo, transformer, publisher);
    }

    @Test
    void shouldCreateTaskAndPublishCreatedEvent() {
        var dto = new CreateTaskDto().setTitle("Write docs").setDescription("desc");
        when(repo.saveAndFlush(any(Task.class))).thenAnswer(inv -> inv.<Task>getArgument(0).setId(42L));

        var result = service.create(dto);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(result.getAssignee()).isNull();

        var captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOfSatisfying(TaskCreatedEvent.class, event -> {
            assertThat(event.taskId()).isEqualTo(42L);
            assertThat(event.title()).isEqualTo("Write docs");
        });
    }

    @Test
    void shouldThrowNotFoundOnCreateWithUnknownAssignee() {
        var dto = gen.nextObject(CreateTaskDto.class);
        when(userRepo.findById(dto.getAssigneeId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(NotFoundException.class);

        verify(publisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void shouldPublishAssignedEventWithPreviousAssignee() {
        var previous = gen.nextObject(User.class);
        var next = gen.nextObject(User.class);
        var task = gen.nextObject(Task.class).setAssignee(previous);
        when(repo.findWithAssigneeById(task.getId())).thenReturn(Optional.of(task));
        when(userRepo.findById(next.getId())).thenReturn(Optional.of(next));

        var result = service.assign(task.getId(), next.getId());

        assertThat(result.getAssignee().getId()).isEqualTo(next.getId());

        var captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOfSatisfying(TaskAssignedEvent.class, event -> {
            assertThat(event.assigneeId()).isEqualTo(next.getId());
            assertThat(event.previousAssigneeId()).isEqualTo(previous.getId());
        });
    }

    @Test
    void shouldNotPublishEventOnAssignSameUser() {
        var assignee = gen.nextObject(User.class);
        var task = gen.nextObject(Task.class).setAssignee(assignee);
        when(repo.findWithAssigneeById(task.getId())).thenReturn(Optional.of(task));
        when(userRepo.findById(assignee.getId())).thenReturn(Optional.of(assignee));

        service.assign(task.getId(), assignee.getId());

        verify(publisher, never()).publishEvent(any(Object.class));
        verify(repo, never()).saveAndFlush(any(Task.class));
    }

    @Test
    void shouldChangeStatusAlongAllowedTransitions() {
        var task = gen.nextObject(Task.class).setStatus(TaskStatus.NEW);
        when(repo.findWithAssigneeById(task.getId())).thenReturn(Optional.of(task));

        assertThat(service.changeStatus(task.getId(), TaskStatus.IN_PROGRESS).getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(service.changeStatus(task.getId(), TaskStatus.DONE).getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void shouldThrowConflictOnInvalidStatusTransition() {
        var task = gen.nextObject(Task.class).setStatus(TaskStatus.NEW);
        when(repo.findWithAssigneeById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.changeStatus(task.getId(), TaskStatus.DONE)).isInstanceOf(ConflictException.class);

        verify(repo, never()).saveAndFlush(any(Task.class));
    }

    @Test
    void shouldThrowNotFoundOnGetUnknownTask() {
        when(repo.findWithAssigneeById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }
}
