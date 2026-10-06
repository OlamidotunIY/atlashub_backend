package com.atlashub.pay.splits.infrastructure.persistence.repositories;

import com.atlashub.pay.splits.infrastructure.persistence.entities.SplitRuleJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataSplitRuleRepository extends JpaRepository<SplitRuleJpa, Long> {

    List<SplitRuleJpa> findAllByOrganizationId(Long organizationId);

    Optional<SplitRuleJpa> findByIdAndOrganizationId(Long id, Long organizationId);
}
