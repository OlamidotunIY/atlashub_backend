package com.atlashub.billing.application.query.GetTransactionVolume;

import com.atlashub.billing.domain.entities.TransactionVolumeLedger;
import com.atlashub.billing.domain.repositories.TransactionVolumeLedgerRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

@Component("billingGetTransactionVolumeHandler")
public class GetTransactionVolumeHandler extends Query<GetTransactionVolumeQuery, TransactionVolumeResult> {

    private final TransactionVolumeLedgerRepository ledgerRepository;

    public GetTransactionVolumeHandler(TransactionVolumeLedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public TransactionVolumeResult execute(GetTransactionVolumeQuery query) {
        Optional<TransactionVolumeLedger> ledgerOpt = ledgerRepository.findByOrganizationIdAndMonth(
                query.organizationId(), query.month()
        );

        if (ledgerOpt.isPresent()) {
            TransactionVolumeLedger ledger = ledgerOpt.get();
            return new TransactionVolumeResult(
                    ledger.getOrganizationId(),
                    ledger.getMonth(),
                    ledger.getTotalVolume(),
                    ledger.getTotalTransactionCount(),
                    ledger.getCurrency(),
                    ledger.getLastUpdatedAt()
            );
        }

        return new TransactionVolumeResult(
                query.organizationId(),
                query.month(),
                BigDecimal.ZERO,
                0L,
                "NGN",
                ZonedDateTime.now()
        );
    }
}
