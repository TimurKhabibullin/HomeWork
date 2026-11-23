package com.example.taskservice.transformer;

import com.example.taskservice.dto.CreateUserDto;
import com.example.taskservice.dto.UserDto;
import com.example.taskservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * @author Timur Khabibullin
 */
@Mapper(componentModel = SPRING)
public interface UserTransformer {

    UserDto toDto(User entity);

    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserDto dto);
}
