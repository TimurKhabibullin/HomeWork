package com.example.taskservice.controller;

import com.example.taskservice.constants.TaskStatus;
import com.example.taskservice.dto.AssignTaskDto;
import com.example.taskservice.dto.ChangeTaskStatusDto;
import com.example.taskservice.dto.CreateTaskDto;
import com.example.taskservice.dto.PageDto;
import com.example.taskservice.dto.TaskDto;
import com.example.taskservice.entity.Task;
import com.example.taskservice.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * @author Timur Khabibullin
 */
@Tag(name = "Tasks")
@RequestMapping("/api/v1/tasks")
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService svc;

    @Operation(
            summary = "Список задач",
            description = "Список задач с пагинацией (page, size не больше 100) и фильтрами по статусу и исполнителю. "
                    + "Сортировка всегда по id, параметр sort игнорируется"
    )
    @GetMapping
    public PageDto<TaskDto> list(@RequestParam(required = false) TaskStatus status,
                                 @RequestParam(required = false) Long assigneeId,
                                 @PageableDefault(size = 20) Pageable pageable) {
        // сортировка фиксирована по id (индексирована, стабильна); клиентский sort игнорируется
        var fixedSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Task.F.ID));

        return svc.list(status, assigneeId, fixedSort);
    }

    @Operation(
            summary = "Получить задачу по id",
            description = "Получить задачу по id вместе с исполнителем"
    )
    @GetMapping("/{id}")
    public TaskDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @Operation(
            summary = "Создать задачу",
            description = "Создать задачу в статусе NEW, опционально сразу с исполнителем. Отправляет событие TASK_CREATED в Kafka"
    )
    @PostMapping
    public ResponseEntity<TaskDto> create(@Valid @RequestBody CreateTaskDto body) {
        var created = svc.create(body);

        return ResponseEntity.created(URI.create("/api/v1/tasks/" + created.getId())).body(created);
    }

    @Operation(
            summary = "Назначить исполнителя",
            description = "Назначить исполнителя задачи. Отправляет событие TASK_ASSIGNED в Kafka, если исполнитель изменился"
    )
    @PutMapping("/{id}/assignee")
    public TaskDto assign(@PathVariable Long id, @Valid @RequestBody AssignTaskDto body) {
        return svc.assign(id, body.getUserId());
    }

    @Operation(
            summary = "Сменить статус",
            description = "Сменить статус задачи. Допустимые переходы: NEW -> IN_PROGRESS -> DONE"
    )
    @PutMapping("/{id}/status")
    public TaskDto changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeTaskStatusDto body) {
        return svc.changeStatus(id, body.getStatus());
    }
}
