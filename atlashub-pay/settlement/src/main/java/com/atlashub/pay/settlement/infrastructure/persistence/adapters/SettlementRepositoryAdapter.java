package com.atlashub.pay.settlement.infrastructure.persistence.adapters;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementJpa;
import com.atlashub.pay.settlement.infrastructure.persistence.mappers.SettlementMapper;
import com.atlashub.pay.settlement.infrastructure.persistence.repositories.SpringDataSettlementRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;

@Component
public class SettlementRepositoryAdapter extends JpaBaseRepository<Settlement, SettlementJpa> implements
        SettlementRepository {

    private final SpringDataSettlementRepository springDataRepo;

    public SettlementRepositoryAdapter(SpringDataSettlementRepository springDataRepo, SettlementMapper mapper,
                                       DomainSequenceGenerator sequenceGenerator, DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "pay_settlement_seq";
    }

    @Override
    public Optional<Settlement> findByProviderAndEnvironmentAndProviderSettlementId(
            PaymentProvider provider, ApiEnvironment environment, String providerSettlementId) {
        return springDataRepo.findByProviderAndEnvironmentAndProviderSettlementId(provider, environment,
                providerSettlementId).map(mapper::toDomain);
    }

    @Override
    public List<Settlement> findAwaitingAnchorCredit(Long accountId, ApiEnvironment environment, Money amount) {
        return springDataRepo.findByAnchorDepositAccountIdAndEnvironmentAndStatusAndNetAmountAndCurrency(
                accountId, environment, SettlementStatus.AWAITING_ANCHOR_CREDIT, amount.amount(), amount.currency())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Settlement> findAwaitingAnchorCreditCreatedBefore(ZonedDateTime cutoff) {
        return springDataRepo.findByStatusAndCreatedAtBefore(SettlementStatus.AWAITING_ANCHOR_CREDIT, cutoff)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageResult<Settlement> findByOrganizationId(Long organizationId, ApiEnvironment environment, SettlementStatus status,
                                                       ZonedDateTime dateFrom, ZonedDateTime dateTo, int page,
                                                       int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SettlementJpa> jpaPage =
                springDataRepo.searchSettlements(organizationId, environment, status, dateFrom, dateTo, pageable);
        List<Settlement> content = jpaPage.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(content, jpaPage.getNumber(), jpaPage.getSize(), jpaPage.getTotalElements(),
                jpaPage.getTotalPages());
    }
}
