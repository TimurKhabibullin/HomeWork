package com.example.taskservice.controller;

import com.example.taskservice.dto.CreateUserDto;
import com.example.taskservice.dto.PageDto;
import com.example.taskservice.dto.UserDto;
import com.example.taskservice.entity.User;
import com.example.taskservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * ТОЛЬКО ДЛЯ ТЕСТИРОВАНИЯ. Онбординг пользователей вне рамок задания, поэтому эти эндпоинты нужны лишь для удобной ручной проверки.
 * Отключаются свойством {@code app.test-endpoints.enabled=false} (переменная TEST_ENDPOINTS_ENABLED).
 *
 * @author Timur Khabibullin
 */
@Tag(name = "Users (TEST ONLY)", description = "Служебные эндпоинты только для тестирования, не часть целевого API")
@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.test-endpoints.enabled", havingValue = "true", matchIfMissing = true)
public class UserController {

    private final UserService svc;

    @Operation(
            summary = "[TEST ONLY] Создать пользователя",
            description = "Только для тестирования. Почта должна быть уникальной"
    )
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserDto body) {
        var created = svc.create(body);

        return ResponseEntity.created(URI.create("/api/v1/users/" + created.getId())).body(created);
    }

    @Operation(
            summary = "[TEST ONLY] Получить пользователя по id",
            description = "Только для тестирования"
    )
    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @Operation(
            summary = "[TEST ONLY] Список пользователей",
            description = "Только для тестирования. Пагинация, сортировка всегда по id"
    )
    @GetMapping
    public PageDto<UserDto> list(@PageableDefault(size = 20) Pageable pageable) {
        return svc.list(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(User.F.ID)));
    }
}
