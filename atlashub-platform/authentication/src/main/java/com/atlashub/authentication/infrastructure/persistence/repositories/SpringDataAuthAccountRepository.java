package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.infrastructure.persistence.entities.AuthAccountJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuthAccount extends JpaRepository<AuthAccountJpa, Long> {
}
