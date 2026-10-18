package com.atlashub.analytics.application.query.GetPlatformTransactionVolume;

import com.atlashub.analytics.domain.entities.TransactionVolumeProjection;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class GetPlatformTransactionVolumeHandler extends Query<GetPlatformTransactionVolumeQuery, PlatformVolumeResult> {

    private final TransactionVolumeProjectionRepository repository;

    public GetPlatformTransactionVolumeHandler(TransactionVolumeProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public PlatformVolumeResult execute(GetPlatformTransactionVolumeQuery query) {
        List<TransactionVolumeProjection> list = repository.findAllByMonth(query.month());

        long totalCharges = list.stream().mapToLong(TransactionVolumeProjection::getTotalCharges).sum();
        long successfulCharges = list.stream().mapToLong(TransactionVolumeProjection::getSuccessfulCharges).sum();
        long failedCharges = list.stream().mapToLong(TransactionVolumeProjection::getFailedCharges).sum();
        long totalPayouts = list.stream().mapToLong(TransactionVolumeProjection::getTotalPayouts).sum();

        BigDecimal totalVolume = list.stream()
                .map(TransactionVolumeProjection::getTotalVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPayoutAmount = list.stream()
                .map(TransactionVolumeProjection::getTotalPayoutAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFees = list.stream()
                .map(TransactionVolumeProjection::getPlatformFeeCollected)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PlatformVolumeResult(
                query.month(),
                totalCharges,
                totalVolume,
                successfulCharges,
                failedCharges,
                totalPayouts,
                totalPayoutAmount,
                totalFees
        );
    }
}
