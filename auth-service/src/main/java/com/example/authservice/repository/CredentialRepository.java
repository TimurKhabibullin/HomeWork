package com.example.authservice.repository;

import com.example.authservice.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Timur Khabibullin
 */
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    boolean existsByLogin(String login);

    Optional<Credential> findByLogin(String login);
}
