package com.atlashub.analytics.application.query.GetDeliveryPerformance;

import com.atlashub.analytics.domain.entities.DeliveryPerformanceProjection;
import com.atlashub.analytics.domain.repositories.DeliveryPerformanceProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class GetDeliveryPerformanceHandler extends Query<GetDeliveryPerformanceQuery, DeliveryPerformanceResult> {

    private final DeliveryPerformanceProjectionRepository repository;

    public GetDeliveryPerformanceHandler(DeliveryPerformanceProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryPerformanceResult execute(GetDeliveryPerformanceQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        DeliveryPerformanceProjection projection = repository.findByOrgEnvAndMonth(
                query.organizationId(),
                env,
                query.month()
        ).orElseGet(() -> DeliveryPerformanceProjection.create(
                null,
                query.organizationId(),
                env,
                query.month()
        ));

        return new DeliveryPerformanceResult(
                projection.getMetricMonth(),
                projection.getTotalShipments(),
                projection.getDelivered(),
                projection.getFailed(),
                projection.getReturned(),
                projection.getDeliverySuccessRate() != null ? projection.getDeliverySuccessRate() : BigDecimal.ZERO,
                projection.getAvgDeliveryMinutes()
        );
    }
}
