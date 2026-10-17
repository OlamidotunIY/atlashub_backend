package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

@Getter
public class DeliveryPerformanceProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final String metricMonth;
    private int totalShipments;
    private int delivered;
    private int failed;
    private int returned;
    private BigDecimal deliverySuccessRate;
    private int avgDeliveryMinutes;
    private ZonedDateTime updatedAt;

    public DeliveryPerformanceProjection(
            Long id,
            Long organizationId,
            String environment,
            String metricMonth,
            int totalShipments,
            int delivered,
            int failed,
            int returned,
            BigDecimal deliverySuccessRate,
            int avgDeliveryMinutes,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.metricMonth = metricMonth;
        this.totalShipments = totalShipments;
        this.delivered = delivered;
        this.failed = failed;
        this.returned = returned;
        this.deliverySuccessRate = deliverySuccessRate;
        this.avgDeliveryMinutes = avgDeliveryMinutes;
        this.updatedAt = updatedAt;
    }

    public static DeliveryPerformanceProjection create(
            Long id,
            Long organizationId,
            String environment,
            String metricMonth
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (metricMonth == null || metricMonth.isBlank()) {
            throw new AnalyticsInvariantException("Metric month must not be blank");
        }
        return new DeliveryPerformanceProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                metricMonth,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO,
                0,
                ZonedDateTime.now()
        );
    }

    public void recordShipment(boolean isDelivered, boolean isReturned, Integer deliveryMinutes) {
        this.totalShipments += 1;
        if (isDelivered) {
            this.delivered += 1;
            if (deliveryMinutes != null && deliveryMinutes > 0) {
                if (this.avgDeliveryMinutes == 0) {
                    this.avgDeliveryMinutes = deliveryMinutes;
                } else {
                    this.avgDeliveryMinutes = (this.avgDeliveryMinutes + deliveryMinutes) / 2;
                }
            }
        } else if (isReturned) {
            this.returned += 1;
        } else {
            this.failed += 1;
        }

        if (this.totalShipments > 0) {
            this.deliverySuccessRate = BigDecimal.valueOf(this.delivered)
                    .divide(BigDecimal.valueOf(this.totalShipments), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
