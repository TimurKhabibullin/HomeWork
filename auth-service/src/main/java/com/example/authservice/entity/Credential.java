package com.example.authservice.entity;

import com.example.authservice.constants.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Учётные данные пользователя для входа по логину и паролю
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "credentials")
public class Credential {

    /**
     * Идентификатор (совпадает с идентификатором пользователя)
     */
    @Id
    @Column(name = "user_id")
    private Long id;

    /**
     * Пользователь
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Логин (уникален, хранится в нижнем регистре)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    /**
     * Хэш пароля (BCrypt)
     */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /**
     * Роль
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    /**
     * Вход разрешён
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Дата создания
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
