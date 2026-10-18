package com.atlashub.pay.transfers.domain.repositories;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.PageResult;

import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends Repository<Payout> {

    Optional<Payout> findByReference(String reference);

    Optional<Payout> findByIdWithPessimisticLock(Long id);

    PageResult<Payout> findByOrganizationIdAndStatus(Long organizationId, PayoutStatus status, int page, int size);

    PageResult<Payout> findByOrganizationId(Long organizationId, int page, int size);

    List<Payout> findBySourceSystemAndSourceReferenceId(SourceSystem sourceSystem, String sourceReferenceId);
}
