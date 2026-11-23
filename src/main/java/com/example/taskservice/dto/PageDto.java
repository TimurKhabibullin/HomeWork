package com.example.taskservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Страница результатов
 *
 * @param <T> тип элементов страницы
 * @author Timur Khabibullin
 */
@Getter
@RequiredArgsConstructor
public class PageDto<T> {

    /**
     * Элементы страницы
     */
    private final List<T> content;

    /**
     * Номер страницы (с нуля)
     */
    private final int page;

    /**
     * Размер страницы
     */
    private final int size;

    /**
     * Общее количество элементов
     */
    private final long totalElements;

    /**
     * Общее количество страниц
     */
    private final int totalPages;

    public static <T> PageDto<T> from(Page<T> page) {
        return new PageDto<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
