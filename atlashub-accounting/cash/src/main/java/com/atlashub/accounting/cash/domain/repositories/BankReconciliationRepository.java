package com.atlashub.accounting.cash.domain.repositories;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface BankReconciliationRepository extends Repository<BankReconciliation> {

    List<BankReconciliation> findByOrganizationId(Long organizationId);

    List<BankReconciliation> findByOrganizationIdAndStatus(Long organizationId, ReconciliationStatus status);
}
