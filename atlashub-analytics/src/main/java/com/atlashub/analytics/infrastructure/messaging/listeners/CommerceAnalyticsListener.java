package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateCustomerPerformanceProjection.UpdateCustomerPerformanceProjectionCommand;
import com.atlashub.analytics.application.command.UpdateCustomerPerformanceProjection.UpdateCustomerPerformanceProjectionHandler;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionCommand;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionHandler;
import com.atlashub.analytics.application.command.UpdateInventoryHealthProjection.UpdateInventoryHealthProjectionCommand;
import com.atlashub.analytics.application.command.UpdateInventoryHealthProjection.UpdateInventoryHealthProjectionHandler;
import com.atlashub.analytics.application.command.UpdateProductDemandProjection.UpdateProductDemandProjectionCommand;
import com.atlashub.analytics.application.command.UpdateProductDemandProjection.UpdateProductDemandProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.PosSaleCompletedPayload;
import com.atlashub.analytics.infrastructure.messaging.events.PosSaleRefundedPayload;
import com.atlashub.analytics.infrastructure.messaging.events.StockAdjustedPayload;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;

@Component
public class CommerceAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(CommerceAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-commerce-group";

    private final UpdateDailySalesProjectionHandler dailySalesHandler;
    private final UpdateProductDemandProjectionHandler productDemandHandler;
    private final UpdateCustomerPerformanceProjectionHandler customerPerformanceHandler;
    private final UpdateInventoryHealthProjectionHandler inventoryHealthHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;

    public CommerceAnalyticsListener(
            ObjectMapper objectMapper,
            UpdateDailySalesProjectionHandler dailySalesHandler,
            UpdateProductDemandProjectionHandler productDemandHandler,
            UpdateCustomerPerformanceProjectionHandler customerPerformanceHandler,
            UpdateInventoryHealthProjectionHandler inventoryHealthHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler
    ) {
        super(objectMapper);
        this.dailySalesHandler = dailySalesHandler;
        this.productDemandHandler = productDemandHandler;
        this.customerPerformanceHandler = customerPerformanceHandler;
        this.inventoryHealthHandler = inventoryHealthHandler;
        this.metricEventHandler = metricEventHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PosSaleCompletedEvent", GROUP_ID);
        registerSubscription("PosSaleRefundedEvent", GROUP_ID);
        registerSubscription("StockAdjustedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "PosSaleCompletedEvent", PosSaleCompletedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            dailySalesHandler.execute(new UpdateDailySalesProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.outletId(),
                    p.saleDate(),
                    p.grossAmount(),
                    p.discountAmount(),
                    p.taxAmount(),
                    p.netAmount(),
                    p.paymentMethod(),
                    false
            ));

            String month = String.format("%04d-%02d", p.saleDate().getYear(), p.saleDate().getMonthValue());
            if (p.productId() != null) {
                productDemandHandler.execute(new UpdateProductDemandProjectionCommand(
                        p.organizationId(),
                        p.environment(),
                        p.productId(),
                        p.outletId(),
                        month,
                        p.quantity(),
                        p.netAmount(),
                        false
                ));
            }

            if (p.customerId() != null) {
                customerPerformanceHandler.execute(new UpdateCustomerPerformanceProjectionCommand(
                        p.organizationId(),
                        p.environment(),
                        p.customerId(),
                        month,
                        p.netAmount(),
                        false
                ));
            }

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    p.eventId(),
                    p.organizationId(),
                    p.environment(),
                    "POS_SALE_COMPLETED",
                    "commerce",
                    p.netAmount(),
                    "NGN",
                    "{}",
                    ZonedDateTime.now()
            ));
        });

        processEventIfMatches(messagePayload, "PosSaleRefundedEvent", PosSaleRefundedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            dailySalesHandler.execute(new UpdateDailySalesProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.outletId(),
                    p.refundDate(),
                    p.grossAmount(),
                    p.discountAmount(),
                    p.taxAmount(),
                    p.netAmount(),
                    p.paymentMethod(),
                    true
            ));

            String month = String.format("%04d-%02d", p.refundDate().getYear(), p.refundDate().getMonthValue());
            if (p.productId() != null) {
                productDemandHandler.execute(new UpdateProductDemandProjectionCommand(
                        p.organizationId(),
                        p.environment(),
                        p.productId(),
                        p.outletId(),
                        month,
                        p.quantity(),
                        p.netAmount(),
                        true
                ));
            }

            if (p.customerId() != null) {
                customerPerformanceHandler.execute(new UpdateCustomerPerformanceProjectionCommand(
                        p.organizationId(),
                        p.environment(),
                        p.customerId(),
                        month,
                        p.netAmount(),
                        true
                ));
            }
        });

        processEventIfMatches(messagePayload, "StockAdjustedEvent", StockAdjustedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            inventoryHealthHandler.execute(new UpdateInventoryHealthProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.outletId(),
                    p.productId(),
                    p.currentStock(),
                    p.reorderPoint(),
                    p.coverageDays(),
                    p.inventoryValue(),
                    p.deadStock()
            ));
        });
    }
}
