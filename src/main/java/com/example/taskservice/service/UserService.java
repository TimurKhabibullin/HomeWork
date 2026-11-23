package com.example.taskservice.service;

import com.example.taskservice.dto.CreateUserDto;
import com.example.taskservice.dto.PageDto;
import com.example.taskservice.dto.UserDto;
import com.example.taskservice.exception.ConflictException;
import com.example.taskservice.exception.NotFoundException;
import com.example.taskservice.repository.UserRepository;
import com.example.taskservice.transformer.UserTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Пользователи. Онбординг вне рамок задания — сервис нужен только для тестовых эндпоинтов.
 *
 * @author Timur Khabibullin
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    private final UserTransformer t;

    @Transactional
    public UserDto create(CreateUserDto dto) {
        if (repo.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ConflictException("Пользователь с почтой %s уже существует".formatted(dto.getEmail()));
        }

        return t.toDto(repo.saveAndFlush(t.toEntity(dto)));
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) {
        return repo.findById(id)
                   .map(t::toDto)
                   .orElseThrow(() -> new NotFoundException("Пользователь %d не найден".formatted(id)));
    }

    @Transactional(readOnly = true)
    public PageDto<UserDto> list(Pageable pageable) {
        return PageDto.from(repo.findAll(pageable).map(t::toDto));
    }
}
