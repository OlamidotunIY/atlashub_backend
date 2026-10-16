package com.atlashub.pay.controls.infrastructure.persistence.repositories;

import com.atlashub.pay.controls.infrastructure.persistence.entities.ControlCaseJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataControlCaseRepository extends JpaRepository<ControlCaseJpa, Long> {

    Optional<ControlCaseJpa> findByDecisionId(Long decisionId);

    Page<ControlCaseJpa> findAllByOrganizationId(Long organizationId, Pageable pageable);
}
