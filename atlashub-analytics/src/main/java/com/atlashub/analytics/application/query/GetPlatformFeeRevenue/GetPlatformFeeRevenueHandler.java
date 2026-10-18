package com.atlashub.analytics.application.query.GetPlatformFeeRevenue;

import com.atlashub.analytics.domain.entities.TransactionVolumeProjection;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class GetPlatformFeeRevenueHandler extends Query<GetPlatformFeeRevenueQuery, PlatformFeeResult> {

    private final TransactionVolumeProjectionRepository repository;

    public GetPlatformFeeRevenueHandler(TransactionVolumeProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public PlatformFeeResult execute(GetPlatformFeeRevenueQuery query) {
        List<TransactionVolumeProjection> list = repository.findAllByMonth(query.month());
        BigDecimal totalFees = list.stream()
                .map(TransactionVolumeProjection::getPlatformFeeCollected)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PlatformFeeResult(query.month(), totalFees);
    }
}
