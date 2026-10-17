package com.atlashub.analytics.application.query.GetTransactionVolume;

import com.atlashub.analytics.domain.entities.TransactionVolumeProjection;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class GetTransactionVolumeHandler extends Query<GetTransactionVolumeQuery, TransactionVolumeResult> {

    private final TransactionVolumeProjectionRepository repository;

    public GetTransactionVolumeHandler(TransactionVolumeProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionVolumeResult execute(GetTransactionVolumeQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        TransactionVolumeProjection projection = repository.findByOrgEnvAndMonth(
                query.organizationId(),
                env,
                query.month()
        ).orElseGet(() -> TransactionVolumeProjection.create(
                null,
                query.organizationId(),
                env,
                query.month()
        ));

        return new TransactionVolumeResult(
                projection.getMetricMonth(),
                projection.getTotalCharges(),
                projection.getTotalVolume() != null ? projection.getTotalVolume() : BigDecimal.ZERO,
                projection.getSuccessfulCharges(),
                projection.getFailedCharges(),
                projection.getTotalPayouts(),
                projection.getTotalPayoutAmount() != null ? projection.getTotalPayoutAmount() : BigDecimal.ZERO,
                projection.getPlatformFeeCollected() != null ? projection.getPlatformFeeCollected() : BigDecimal.ZERO
        );
    }
}
