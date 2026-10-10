package com.atlashub.accounting.gl.infrastructure.persistence.repositories;

import com.atlashub.accounting.gl.infrastructure.persistence.entities.AccountJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpa, Long> {

    Optional<AccountJpa> findByOrganizationIdAndCode(Long organizationId, String code);

    List<AccountJpa> findByOrganizationId(Long organizationId);

    List<AccountJpa> findByOrganizationIdAndIsActiveTrue(Long organizationId);
}
