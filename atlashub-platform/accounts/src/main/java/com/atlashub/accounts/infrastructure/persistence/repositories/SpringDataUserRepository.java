package com.atlashub.accounts.infrastructure.persistence.repositories;

import com.atlashub.accounts.infrastructure.persistence.entities.UserJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserJPA, Long> {
    Optional<UserJPA> findByEmail(String email);
}
