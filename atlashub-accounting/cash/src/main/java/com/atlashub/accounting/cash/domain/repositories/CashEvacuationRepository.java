package com.atlashub.accounting.cash.domain.repositories;

import com.atlashub.accounting.cash.domain.entities.CashEvacuation;
import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface CashEvacuationRepository extends Repository<CashEvacuation> {

    List<CashEvacuation> findByOrganizationId(Long organizationId);

    List<CashEvacuation> findByOrganizationIdAndStatus(Long organizationId, EvacuationStatus status);
}
