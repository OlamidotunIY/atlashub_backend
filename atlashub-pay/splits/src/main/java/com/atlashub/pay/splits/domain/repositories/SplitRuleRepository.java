package com.atlashub.pay.splits.domain.repositories;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface SplitRuleRepository extends Repository<SplitRule> {
    List<SplitRule> findAllByOrganizationId(Long organizationId);
    Optional<SplitRule> findByIdAndOrganizationId(Long id, Long organizationId);
}
