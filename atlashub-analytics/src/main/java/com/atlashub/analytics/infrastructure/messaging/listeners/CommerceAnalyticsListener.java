package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionCommand;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionHandler;
import com.atlashub.analytics.application.command.UpdateCustomerPerformanceProjection.UpdateCustomerPerformanceProjectionCommand;
import com.atlashub.analytics.application.command.UpdateCustomerPerformanceProjection.UpdateCustomerPerformanceProjectionHandler;
import com.atlashub.analytics.application.command.UpdateInventoryHealthProjection.UpdateInventoryHealthProjectionCommand;
import com.atlashub.analytics.application.command.UpdateInventoryHealthProjection.UpdateInventoryHealthProjectionHandler;
import com.atlashub.analytics.application.command.UpdateOutletPerformanceProjection.UpdateOutletPerformanceProjectionCommand;
import com.atlashub.analytics.application.command.UpdateOutletPerformanceProjection.UpdateOutletPerformanceProjectionHandler;
import com.atlashub.analytics.application.command.UpdateProductDemandProjection.UpdateProductDemandProjectionCommand;
import com.atlashub.analytics.application.command.UpdateProductDemandProjection.UpdateProductDemandProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.PosSaleCompletedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.PosSaleRefundedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.StockAdjustedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeoutException;

@Component
public class CommerceAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(CommerceAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-commerce-group";

    private final UpdateDailySalesProjectionHandler dailySalesHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;
    private final UpdateProductDemandProjectionHandler productDemandHandler;
    private final UpdateCustomerPerformanceProjectionHandler customerPerformanceHandler;
    private final UpdateOutletPerformanceProjectionHandler outletPerformanceHandler;
    private final UpdateInventoryHealthProjectionHandler inventoryHealthHandler;

    public CommerceAnalyticsListener(
            ObjectMapper objectMapper,
            UpdateDailySalesProjectionHandler dailySalesHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler,
            UpdateProductDemandProjectionHandler productDemandHandler,
            UpdateCustomerPerformanceProjectionHandler customerPerformanceHandler,
            UpdateOutletPerformanceProjectionHandler outletPerformanceHandler,
            UpdateInventoryHealthProjectionHandler inventoryHealthHandler
    ) {
        super(objectMapper);
        this.dailySalesHandler = dailySalesHandler;
        this.metricEventHandler = metricEventHandler;
        this.productDemandHandler = productDemandHandler;
        this.customerPerformanceHandler = customerPerformanceHandler;
        this.outletPerformanceHandler = outletPerformanceHandler;
        this.inventoryHealthHandler = inventoryHealthHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PosSaleCompletedEvent", GROUP_ID);
        registerSubscription("PosSaleRefundedEvent", GROUP_ID);
        registerSubscription("StockAdjustedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "PosSaleCompletedEvent", PosSaleCompletedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            PosSaleCompletedEvent.Payload payload = event.payload();
            java.time.ZonedDateTime saleTimestamp = payload.completedAt() != null ? payload.completedAt() : event.occurredAt();
            dailySalesHandler.execute(new UpdateDailySalesProjectionCommand(
                    payload.organizationId(),
                    "live",
                    payload.outletId(),
                    saleTimestamp.toLocalDate(),
                    valueOr(payload.totalGross(), payload.totalNet()),
                    valueOr(payload.totalDiscount(), BigDecimal.ZERO),
                    valueOr(payload.totalTax(), BigDecimal.ZERO),
                    payload.totalNet(),
                    payload.paymentMethod(),
                    false
            ));

            outletPerformanceHandler.execute(new UpdateOutletPerformanceProjectionCommand(
                    payload.organizationId(), "live", payload.outletId(), metricMonth(saleTimestamp),
                    payload.totalNet(), valueOr(payload.totalDiscount(), BigDecimal.ZERO), 1, BigDecimal.ZERO));

            if (payload.customerId() != null) {
                customerPerformanceHandler.execute(new UpdateCustomerPerformanceProjectionCommand(
                        payload.organizationId(), "live", payload.customerId(), metricMonth(saleTimestamp),
                        payload.totalNet(), false));
            }

            if (payload.items() != null) {
                payload.items().forEach(item -> productDemandHandler.execute(new UpdateProductDemandProjectionCommand(
                        payload.organizationId(), "live", item.productId(), payload.outletId(),
                        metricMonth(saleTimestamp), item.quantity(), item.totalAmount(), false)));
            }

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    "live",
                    "POS_SALE_COMPLETED",
                    "commerce",
                    payload.totalNet(),
                    payload.currency(),
                    "{}",
                    saleTimestamp
            ));
        });

        processEventIfMatches(messagePayload, "PosSaleRefundedEvent", PosSaleRefundedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            PosSaleRefundedEvent.Payload payload = event.payload();
            java.time.ZonedDateTime refundTimestamp = payload.refundedAt() != null ? payload.refundedAt() : event.occurredAt();
            if (payload.outletId() != null) {
                dailySalesHandler.execute(new UpdateDailySalesProjectionCommand(
                        payload.organizationId(), "live", payload.outletId(), refundTimestamp.toLocalDate(),
                        valueOr(payload.totalGross(), payload.refundAmount()),
                        valueOr(payload.totalDiscount(), BigDecimal.ZERO),
                        valueOr(payload.totalTax(), BigDecimal.ZERO), payload.refundAmount(), payload.paymentMethod(), true));
                outletPerformanceHandler.execute(new UpdateOutletPerformanceProjectionCommand(
                        payload.organizationId(), "live", payload.outletId(), metricMonth(refundTimestamp),
                        payload.refundAmount().negate(), BigDecimal.ZERO, 0, BigDecimal.ZERO));
            }
            if (payload.customerId() != null) {
                customerPerformanceHandler.execute(new UpdateCustomerPerformanceProjectionCommand(
                        payload.organizationId(), "live", payload.customerId(), metricMonth(refundTimestamp),
                        payload.refundAmount(), true));
            }
            if (payload.items() != null && payload.outletId() != null) {
                payload.items().forEach(item -> productDemandHandler.execute(new UpdateProductDemandProjectionCommand(
                        payload.organizationId(), "live", item.productId(), payload.outletId(),
                        metricMonth(refundTimestamp), item.quantity(), item.totalAmount(), true)));
            }
            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    "live",
                    "POS_SALE_REFUNDED",
                    "commerce",
                    payload.refundAmount(),
                    payload.currency(),
                    "{}",
                    refundTimestamp
            ));
        });

        processEventIfMatches(messagePayload, "StockAdjustedEvent", StockAdjustedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            StockAdjustedEvent.Payload payload = event.payload();
            if (payload.reorderLevel() != null) {
                inventoryHealthHandler.execute(new UpdateInventoryHealthProjectionCommand(
                        payload.organizationId(), "live", payload.outletId(), payload.productId(), payload.newQty(),
                        payload.reorderLevel(), 0, BigDecimal.ZERO, false));
            }
            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    "live",
                    "STOCK_ADJUSTED",
                    "commerce",
                    null,
                    null,
                    "{}",
                    payload.adjustedAt() != null ? payload.adjustedAt() : event.occurredAt()
            ));
        });
    }

    private String metricMonth(java.time.ZonedDateTime timestamp) {
        return String.format("%04d-%02d", timestamp.getYear(), timestamp.getMonthValue());
    }

    private BigDecimal valueOr(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }
}
