package com.atlashub.pay.charges.infrastructure.messaging.listeners;

import com.atlashub.pay.charges.application.commands.ApplyChargeProviderStatus.ApplyChargeProviderStatusCommand;
import com.atlashub.pay.charges.application.commands.ApplyChargeProviderStatus.ApplyChargeProviderStatusHandler;
import com.atlashub.pay.charges.application.commands.ApplyRefundProviderStatus.ApplyRefundProviderStatusCommand;
import com.atlashub.pay.charges.application.commands.ApplyRefundProviderStatus.ApplyRefundProviderStatusHandler;
import com.atlashub.pay.charges.application.commands.ApplyChargeDispute.ApplyChargeDisputeCommand;
import com.atlashub.pay.charges.application.commands.ApplyChargeDispute.ApplyChargeDisputeHandler;
import com.atlashub.pay.charges.infrastructure.messaging.events.PaystackWebhookReceivedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class PaystackChargeWebhookListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaystackChargeWebhookListener.class);
    private static final String GROUP_ID = "pay-charges-paystack-webhooks";
    private static final String TOPIC = "paystack-events";
    private static final String EVENT_TYPE = "PaystackWebhookReceivedEvent";

    private final ApplyChargeProviderStatusHandler handler;
    private final ApplyRefundProviderStatusHandler refundHandler;
    private final ApplyChargeDisputeHandler disputeHandler;

    public PaystackChargeWebhookListener(ObjectMapper objectMapper, ApplyChargeProviderStatusHandler handler,
                                         ApplyRefundProviderStatusHandler refundHandler,
                                         ApplyChargeDisputeHandler disputeHandler) {
        super(objectMapper);
        this.handler = handler;
        this.refundHandler = refundHandler;
        this.disputeHandler = disputeHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(EVENT_TYPE, GROUP_ID);
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, EVENT_TYPE, PaystackWebhookReceivedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException, this::dispatch);
    }

    private void dispatch(PaystackWebhookReceivedEvent event) {
        if (event == null) {
            return;
        }

        String eventType = event.resolveEventType();
        if (eventType != null && eventType.toLowerCase().startsWith("refund.")) {
            dispatchRefund(event, eventType);
            return;
        }
        if (eventType != null && eventType.toLowerCase().startsWith("charge.dispute.")) {
            dispatchDispute(event, eventType);
            return;
        }
        boolean isSuccess = "charge.success".equalsIgnoreCase(eventType);
        boolean isFailed = "charge.failed".equalsIgnoreCase(eventType);

        if (!isSuccess && !isFailed) {
            log.debug("Ignoring non-charge Paystack webhook event type: {}", eventType);
            return;
        }

        String envStr = event.resolveEnvironment();
        ApiEnvironment environment = "LIVE".equalsIgnoreCase(envStr) ? ApiEnvironment.LIVE : ApiEnvironment.TEST;

        Map<String, Object> data = event.resolveData();
        String reference = data.get("reference") != null ? String.valueOf(data.get("reference")) : null;
        Object idObj = data.get("id");
        String gatewayReference = idObj != null && !String.valueOf(idObj).isBlank() ? String.valueOf(idObj) : reference;

        long amountInKobo = 0L;
        Object amountObj = data.get("amount");
        if (amountObj instanceof Number number) {
            amountInKobo = number.longValue();
        } else if (amountObj != null) {
            try {
                amountInKobo = Long.parseLong(String.valueOf(amountObj));
            } catch (NumberFormatException ignored) {
            }
        }
        BigDecimal amountInNaira = BigDecimal.valueOf(amountInKobo, 2);

        String currencyStr = data.get("currency") != null ? String.valueOf(data.get("currency")) : "NGN";
        CurrencyCode currency = CurrencyCode.valueOf(currencyStr);

        String failureReason = null;
        if (isFailed) {
            Object gatewayResponse = data.get("gateway_response");
            if (gatewayResponse != null && !String.valueOf(gatewayResponse).isBlank()) {
                failureReason = String.valueOf(gatewayResponse);
            } else if (data.get("message") != null && !String.valueOf(data.get("message")).isBlank()) {
                failureReason = String.valueOf(data.get("message"));
            } else {
                failureReason = "Payment failed at provider";
            }
        }

        ApplyChargeProviderStatusCommand command =
                new ApplyChargeProviderStatusCommand(environment, reference, gatewayReference,
                        Money.of(amountInNaira, currency), currency, isSuccess, failureReason);

        handler.execute(command);
    }

    private void dispatchRefund(PaystackWebhookReceivedEvent event, String eventType) {
        Map<String, Object> data = event.resolveData();
        String transactionReference = text(data, "transaction_reference", "transactionReference");
        if (transactionReference == null) {
            throw new IllegalArgumentException("Paystack refund event is missing transaction reference");
        }
        String environmentText = event.resolveEnvironment();
        ApiEnvironment environment = "LIVE".equalsIgnoreCase(environmentText)
                ? ApiEnvironment.LIVE : ApiEnvironment.TEST;
        String currencyText = text(data, "currency");
        CurrencyCode currency = CurrencyCode.valueOf(currencyText == null ? "NGN" : currencyText);
        Object amountValue = data.get("amount");
        if (amountValue == null) throw new IllegalArgumentException("Paystack refund amount is required");
        BigDecimal amount = new BigDecimal(amountValue.toString()).movePointLeft(2);
        String status = eventType.substring("refund.".length());
        refundHandler.execute(new ApplyRefundProviderStatusCommand(environment, transactionReference,
                text(data, "refund_reference", "refundReference"), status, Money.of(amount, currency),
                text(data, "reason", "message")));
    }

    private String text(Map<String, Object> values, String... names) {
        for (String name : names) {
            Object value = values.get(name);
            if (value != null && !value.toString().isBlank()) return value.toString();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void dispatchDispute(PaystackWebhookReceivedEvent event, String eventType) {
        Map<String,Object> data=event.resolveData();
        Map<String,Object> transaction=data.get("transaction") instanceof Map<?,?> value
                ? (Map<String,Object>)value:Map.of();
        String transactionReference=text(data,"transaction_reference","merchant_transaction_reference");
        if(transactionReference==null)transactionReference=text(transaction,"reference");
        String disputeReference=text(data,"id","dispute_id");
        if(transactionReference==null||disputeReference==null)
            throw new IllegalArgumentException("Paystack dispute event is missing transaction or dispute reference");
        ApiEnvironment environment="LIVE".equalsIgnoreCase(event.resolveEnvironment())
                ?ApiEnvironment.LIVE:ApiEnvironment.TEST;
        disputeHandler.execute(new ApplyChargeDisputeCommand(environment,transactionReference,disputeReference,
                eventType,text(data,"resolution","reason","note","category")));
    }
}
