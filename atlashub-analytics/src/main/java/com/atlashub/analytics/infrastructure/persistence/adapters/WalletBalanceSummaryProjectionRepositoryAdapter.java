package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.WalletBalanceSummaryProjection;
import com.atlashub.analytics.domain.repositories.WalletBalanceSummaryProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.WalletBalanceSummaryProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.WalletBalanceSummaryProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataWalletBalanceSummaryProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WalletBalanceSummaryProjectionRepositoryAdapter
        extends JpaBaseRepository<WalletBalanceSummaryProjection, WalletBalanceSummaryProjectionJpa>
        implements WalletBalanceSummaryProjectionRepository {

    private final SpringDataWalletBalanceSummaryProjectionRepository springDataRepo;

    public WalletBalanceSummaryProjectionRepositoryAdapter(
            SpringDataWalletBalanceSummaryProjectionRepository springDataRepo,
            WalletBalanceSummaryProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "wallet_balance_summary_seq";
    }

    @Override
    public Optional<WalletBalanceSummaryProjection> findByOrgEnv(Long organizationId, String environment) {
        return springDataRepo.findByOrganizationIdAndEnvironment(organizationId, environment)
                .map(mapper::toDomain);
    }
}
