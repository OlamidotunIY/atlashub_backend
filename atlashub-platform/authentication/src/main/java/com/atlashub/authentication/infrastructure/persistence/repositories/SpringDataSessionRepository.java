package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.infrastructure.persistence.entities.SessionJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSessionRepository extends JpaRepository<SessionJpa, Long> {
}
