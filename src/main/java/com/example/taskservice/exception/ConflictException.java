package com.example.taskservice.exception;

/**
 * Операция конфликтует с текущим состоянием ресурса (HTTP 409)
 *
 * @author Timur Khabibullin
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
