package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateTransactionVolumeProjection.UpdateTransactionVolumeProjectionCommand;
import com.atlashub.analytics.application.command.UpdateTransactionVolumeProjection.UpdateTransactionVolumeProjectionHandler;
import com.atlashub.analytics.application.command.UpdateWalletBalanceSummaryProjection.UpdateWalletBalanceSummaryProjectionCommand;
import com.atlashub.analytics.application.command.UpdateWalletBalanceSummaryProjection.UpdateWalletBalanceSummaryProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.ChargeCompletedPayload;
import com.atlashub.analytics.infrastructure.messaging.events.LedgerTransactionPayload;
import com.atlashub.analytics.infrastructure.messaging.events.PayoutCompletedPayload;
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
public class PayAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PayAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-pay-group";

    private final UpdateTransactionVolumeProjectionHandler txVolumeHandler;
    private final UpdateWalletBalanceSummaryProjectionHandler walletBalanceHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;

    public PayAnalyticsListener(
            ObjectMapper objectMapper,
            UpdateTransactionVolumeProjectionHandler txVolumeHandler,
            UpdateWalletBalanceSummaryProjectionHandler walletBalanceHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler
    ) {
        super(objectMapper);
        this.txVolumeHandler = txVolumeHandler;
        this.walletBalanceHandler = walletBalanceHandler;
        this.metricEventHandler = metricEventHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ChargeSuccessfulEvent", GROUP_ID);
        registerSubscription("ChargeFailedEvent", GROUP_ID);
        registerSubscription("PayoutCompletedEvent", GROUP_ID);
        registerSubscription("LedgerTransactionPostedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "ChargeSuccessfulEvent", ChargeCompletedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            txVolumeHandler.execute(new UpdateTransactionVolumeProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.month(),
                    true,
                    true,
                    false,
                    p.amount(),
                    p.platformFee()
            ));

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    p.eventId(),
                    p.organizationId(),
                    p.environment(),
                    "CHARGE_SUCCESSFUL",
                    "pay",
                    p.amount(),
                    "NGN",
                    "{}",
                    ZonedDateTime.now()
            ));
        });

        processEventIfMatches(messagePayload, "ChargeFailedEvent", ChargeCompletedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            txVolumeHandler.execute(new UpdateTransactionVolumeProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.month(),
                    true,
                    false,
                    false,
                    p.amount(),
                    p.platformFee()
            ));
        });

        processEventIfMatches(messagePayload, "PayoutCompletedEvent", PayoutCompletedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            txVolumeHandler.execute(new UpdateTransactionVolumeProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.month(),
                    false,
                    true,
                    true,
                    p.amount(),
                    null
            ));
        });

        processEventIfMatches(messagePayload, "LedgerTransactionPostedEvent", LedgerTransactionPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            walletBalanceHandler.execute(new UpdateWalletBalanceSummaryProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.ledgerType(),
                    p.balance()
            ));
        });
    }
}
