package com.example.authservice.exception;

/**
 * Запрошенный ресурс не найден (HTTP 404)
 *
 * @author Timur Khabibullin
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
