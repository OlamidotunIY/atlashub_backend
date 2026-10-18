package com.atlashub.billing.infrastructure.persistence.adapters;

import com.atlashub.billing.domain.entities.TransactionVolumeLedger;
import com.atlashub.billing.domain.repositories.TransactionVolumeLedgerRepository;
import com.atlashub.billing.infrastructure.persistence.entities.TransactionVolumeLedgerJpa;
import com.atlashub.billing.infrastructure.persistence.mappers.TransactionVolumeLedgerMapper;
import com.atlashub.billing.infrastructure.persistence.repositories.SpringDataTransactionVolumeLedgerRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TransactionVolumeLedgerRepositoryAdapter
        extends JpaBaseRepository<TransactionVolumeLedger, TransactionVolumeLedgerJpa>
        implements TransactionVolumeLedgerRepository {

    private final SpringDataTransactionVolumeLedgerRepository springDataRepo;

    public TransactionVolumeLedgerRepositoryAdapter(
            SpringDataTransactionVolumeLedgerRepository springDataRepo,
            TransactionVolumeLedgerMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "billing_tx_volume_ledger_seq";
    }

    @Override
    public Optional<TransactionVolumeLedger> findByOrganizationIdAndMonth(Long organizationId, String month) {
        if (organizationId == null || month == null || month.isBlank()) {
            return Optional.empty();
        }
        return springDataRepo.findByOrganizationIdAndMonth(organizationId, month).map(mapper::toDomain);
    }
}
