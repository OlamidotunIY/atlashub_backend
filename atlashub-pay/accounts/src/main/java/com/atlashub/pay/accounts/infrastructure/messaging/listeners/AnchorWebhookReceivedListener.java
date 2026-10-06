package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.anchor.dto.common.AnchorResourceIdentifier;
import com.atlashub.anchor.dto.common.AnchorIncludedResource;
import com.atlashub.anchor.infrastructure.messaging.events.AnchorWebhookReceivedEvent;
import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusCommand;
import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusHandler;
import com.atlashub.pay.accounts.application.commands.RecordAnchorFunding.RecordAnchorFundingCommand;
import com.atlashub.pay.accounts.application.commands.RecordAnchorFunding.RecordAnchorFundingHandler;
import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeoutException;

/** Translates the verified provider event into the pay-accounts command contract. */
@Component
public class AnchorWebhookReceivedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(AnchorWebhookReceivedListener.class);
    private static final String GROUP_ID = "pay-accounts-anchor-webhooks";
    private final ApplyAnchorAccountStatusHandler handler;
    private final RecordAnchorFundingHandler fundingHandler;

    public AnchorWebhookReceivedListener(ObjectMapper objectMapper, ApplyAnchorAccountStatusHandler handler,
                                         RecordAnchorFundingHandler fundingHandler) {
        super(objectMapper);
        this.handler = handler;
        this.fundingHandler = fundingHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(AnchorWebhookReceivedEvent.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "anchor-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, AnchorWebhookReceivedEvent.class.getSimpleName(),
                AnchorWebhookReceivedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException, this::dispatch);
    }

    private void dispatch(AnchorWebhookReceivedEvent event) {
        if (event.consumer() != AnchorWebhookConsumer.PAY_ACCOUNTS) {
            return;
        }
        if (("payin.received".equalsIgnoreCase(event.eventType())
                || "payment.received".equalsIgnoreCase(event.eventType()))
                && event.includedResources().stream().anyMatch(value -> "PayIn".equalsIgnoreCase(value.type()))) {
            dispatchFunding(event);
            return;
        }
        if (!isAccountLifecycleEvent(event.eventType())) return;
        AnchorIncludedResource resource = event.includedResources().stream()
                .filter(value -> isAccountResource(value.type()))
                .findFirst().orElse(null);
        Map<String, Object> attributes = resource == null
                ? event.attributes() : accountAttributes(resource, event);
        String resourceType = resource == null
                ? text(attributes, "resourceType", "accountType", "type") : resource.type();
        String resourceId = resource == null
                ? text(attributes, "resourceId", "accountId", "id") : resource.id();
        if (resourceId == null) {
            resourceId = relationshipId(event.relationships(), "account", "depositAccount", "subAccount", "reservedAccount");
        }
        String status = text(attributes, "status");
        if (resourceType == null || resourceId == null || status == null) {
            throw new IllegalArgumentException("Anchor account webhook is missing resource type, resource id, or status");
        }

        if (!"ACTIVE".equalsIgnoreCase(status) && !"FAILED".equalsIgnoreCase(status)) {
            return;
        }
        ConfirmedBankingDetails details = "FAILED".equalsIgnoreCase(status) ? null : new ConfirmedBankingDetails(
                text(attributes, "accountName"), text(attributes, "accountNumber"),
                text(attributes, "maskedAccountNumber"), text(attributes, "bankName"),
                text(attributes, "bankCode"));
        handler.execute(new ApplyAnchorAccountStatusCommand(
                normalizeResourceType(resourceType),
                event.environment() == AnchorEnvironment.SANDBOX ? "TEST" : "LIVE",
                resourceId, status, details, text(attributes, "failureReason", "failureMessage")));
    }

    private void dispatchFunding(AnchorWebhookReceivedEvent event) {
        AnchorIncludedResource payIn = event.includedResources().stream()
                .filter(value -> "PayIn".equalsIgnoreCase(value.type()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Anchor payin webhook must include PayIn details"));
        if (!"COMPLETED".equalsIgnoreCase(text(payIn.attributes(), "status"))) return;
        String reservedAccountId = relationshipId(payIn.relationships(), "reservedAccount");
        String depositAccountId = relationshipId(payIn.relationships(), "account", "depositAccount");
        AnchorIncludedResource charge = event.includedResources().stream()
                .filter(value -> "Charge".equalsIgnoreCase(value.type())).findFirst().orElse(null);
        Map<String, Object> transfer = nested(charge == null ? Map.of() : charge.attributes(), "transferDetails");
        fundingHandler.execute(new RecordAnchorFundingCommand(
                event.environment() == AnchorEnvironment.SANDBOX ? "TEST" : "LIVE",
                reservedAccountId, depositAccountId,
                text(payIn.attributes(), "reference", "sessionId"),
                decimal(payIn.attributes().get("amount")), text(payIn.attributes(), "currency"),
                text(transfer, "senderName"), text(transfer, "senderBank"),
                time(text(payIn.attributes(), "paidAt", "createdAt"))));
    }

    private boolean isAccountResource(String type) {
        if (type == null) return false;
        String normalized = normalizeResourceType(type);
        return normalized.equals("DEPOSIT_ACCOUNT") || normalized.equals("SUB_ACCOUNT")
                || normalized.equals("RESERVED_ACCOUNT");
    }

    private Map<String, Object> accountAttributes(
            AnchorIncludedResource account,
            AnchorWebhookReceivedEvent event
    ) {
        if (!"SUB_ACCOUNT".equals(normalizeResourceType(account.type()))) {
            return account.attributes();
        }
        Map<String, Object> merged = new HashMap<>();
        event.includedResources().stream()
                .filter(value -> value.type() != null
                        && normalizeResourceType(value.type()).equals("VIRTUAL_NUBAN"))
                .findFirst()
                .ifPresent(value -> merged.putAll(value.attributes()));
        merged.putAll(account.attributes());
        return merged;
    }

    private boolean isAccountLifecycleEvent(String eventType) {
        String normalized = eventType.toLowerCase();
        return normalized.contains("account")
                && !normalized.contains("payin")
                && !normalized.contains("transfer");
    }

    private String normalizeResourceType(String value) {
        return value.replace('-', '_').replace(' ', '_')
                .replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

    private String relationshipId(Map<String, AnchorResourceIdentifier> relationships, String... names) {
        for (String name : names) {
            AnchorResourceIdentifier identifier = relationships.get(name);
            if (identifier != null && identifier.id() != null && !identifier.id().isBlank()) {
                return identifier.id();
            }
        }
        return null;
    }

    private String text(Map<String, Object> attributes, String... names) {
        for (String name : names) {
            Object value = attributes.get(name);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nested(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private BigDecimal decimal(Object value) {
        if (value == null) throw new IllegalArgumentException("Anchor payin amount is required");
        return new BigDecimal(value.toString());
    }

    private ZonedDateTime time(String value) {
        if (value == null) return ZonedDateTime.now();
        try {
            return ZonedDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(value).atZone(ZoneOffset.UTC);
        }
    }
}
