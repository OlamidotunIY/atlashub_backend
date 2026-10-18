package com.atlashub.billing.domain.repositories;

import com.atlashub.billing.domain.entities.TransactionVolumeLedger;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface TransactionVolumeLedgerRepository extends Repository<TransactionVolumeLedger> {

    Optional<TransactionVolumeLedger> findByOrganizationIdAndMonth(Long organizationId, String month);
}
