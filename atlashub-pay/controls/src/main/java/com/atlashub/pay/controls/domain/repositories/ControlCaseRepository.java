package com.atlashub.pay.controls.domain.repositories;

import com.atlashub.pay.controls.domain.entities.ControlCase;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface ControlCaseRepository extends Repository<ControlCase> {

    Optional<ControlCase> findByDecisionId(Long decisionId);

    PageResult<ControlCase> findAllByOrganizationId(Long organizationId, int pageNumber, int pageSize);
}
