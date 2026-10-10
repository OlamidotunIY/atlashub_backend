package com.atlashub.pay.txquery.domain.repositories;

import com.atlashub.pay.txquery.domain.entities.TransactionRecord;
import com.atlashub.pay.txquery.domain.valueobject.TransactionFilter;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.PageResult;

import java.time.YearMonth;
import java.util.Optional;

public interface TransactionRecordRepository extends Repository<TransactionRecord> {
    Long nextEntryIdentity();
    Optional<TransactionRecord> findByOrganizationIdAndEnvironmentAndReference(
            Long organizationId, ApiEnvironment environment, String reference);
    PageResult<TransactionRecord> search(TransactionFilter filter);
    TransactionVolume calculateVolume(Long organizationId, ApiEnvironment environment, YearMonth month);

    record TransactionVolume(long chargeCount, java.math.BigDecimal chargeAmount,
                             long payoutCount, java.math.BigDecimal payoutAmount, String currency) {
    }
}
