package com.atlashub.pay.settlement.infrastructure.persistence.adapters;

import com.atlashub.pay.settlement.domain.entities.SettlementCreditEvidence;
import com.atlashub.pay.settlement.domain.repositories.SettlementCreditEvidenceRepository;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementCreditEvidenceJpa;
import com.atlashub.pay.settlement.infrastructure.persistence.mappers.SettlementCreditEvidenceMapper;
import com.atlashub.pay.settlement.infrastructure.persistence.repositories.SpringDataSettlementCreditEvidenceRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SettlementCreditEvidenceRepositoryAdapter
        extends JpaBaseRepository<SettlementCreditEvidence, SettlementCreditEvidenceJpa>
        implements SettlementCreditEvidenceRepository {
    private final SpringDataSettlementCreditEvidenceRepository repository;
    public SettlementCreditEvidenceRepositoryAdapter(SpringDataSettlementCreditEvidenceRepository repository,
            SettlementCreditEvidenceMapper mapper, DomainSequenceGenerator sequence, DomainEventPublisher publisher) {
        super(repository, mapper, sequence, publisher); this.repository = repository;
    }
    @Override protected String getSequenceName() { return "pay_settlement_credit_evidence_seq"; }
    @Override public Optional<SettlementCreditEvidence> findByEnvironmentAndAnchorTransferReference(
            ApiEnvironment environment, String reference) {
        return repository.findByEnvironmentAndAnchorTransferReference(environment, reference).map(mapper::toDomain);
    }
    @Override public List<SettlementCreditEvidence> findUnmatched(Long accountId, ApiEnvironment environment, Money amount) {
        return repository.findByAnchorDepositAccountIdAndEnvironmentAndAmountAndCurrencyAndMatchedSettlementIdIsNull(
                accountId, environment, amount.amount(), amount.currency()).stream().map(mapper::toDomain).toList();
    }
}
