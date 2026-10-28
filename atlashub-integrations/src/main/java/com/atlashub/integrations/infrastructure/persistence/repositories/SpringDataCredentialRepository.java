package com.atlashub.integrations.infrastructure.persistence.repositories;

import com.atlashub.integrations.infrastructure.persistence.entities.CredentialJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCredentialRepository extends JpaRepository<CredentialJpa, String> {}
