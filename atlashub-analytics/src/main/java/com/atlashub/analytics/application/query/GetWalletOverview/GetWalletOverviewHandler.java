package com.atlashub.analytics.application.query.GetWalletOverview;

import com.atlashub.analytics.domain.entities.WalletBalanceSummaryProjection;
import com.atlashub.analytics.domain.repositories.WalletBalanceSummaryProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class GetWalletOverviewHandler extends Query<GetWalletOverviewQuery, WalletBalanceSummaryResult> {

    private final WalletBalanceSummaryProjectionRepository repository;

    public GetWalletOverviewHandler(WalletBalanceSummaryProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceSummaryResult execute(GetWalletOverviewQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        WalletBalanceSummaryProjection projection = repository.findByOrgEnv(
                query.organizationId(),
                env
        ).orElseGet(() -> WalletBalanceSummaryProjection.create(
                null,
                query.organizationId(),
                env
        ));

        return new WalletBalanceSummaryResult(
                projection.getOperatingBalance() != null ? projection.getOperatingBalance() : BigDecimal.ZERO,
                projection.getEscrowBalance() != null ? projection.getEscrowBalance() : BigDecimal.ZERO,
                projection.getPayrollReserveBalance() != null ? projection.getPayrollReserveBalance() : BigDecimal.ZERO,
                projection.getTaxHoldingBalance() != null ? projection.getTaxHoldingBalance() : BigDecimal.ZERO
        );
    }
}
