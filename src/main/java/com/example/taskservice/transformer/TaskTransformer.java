package com.example.taskservice.transformer;

import com.example.taskservice.dto.TaskDto;
import com.example.taskservice.entity.Task;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * @author Timur Khabibullin
 */
@Mapper(componentModel = SPRING, uses = UserTransformer.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TaskTransformer {

    TaskDto toDto(Task entity);
}
