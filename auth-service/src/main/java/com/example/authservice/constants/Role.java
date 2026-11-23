package com.example.authservice.constants;

/**
 * Роль пользователя
 *
 * @author Timur Khabibullin
 */
public enum Role {

    /**
     * Обычный пользователь (назначается при регистрации)
     */
    USER,

    /**
     * Администратор (через регистрацию недоступна, назначается вручную в БД)
     */
    ADMIN
}
