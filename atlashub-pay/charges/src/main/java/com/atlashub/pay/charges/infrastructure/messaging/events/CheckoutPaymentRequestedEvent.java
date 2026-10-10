package com.atlashub.pay.charges.infrastructure.messaging.events;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheckoutPaymentRequestedEvent(String eventId, String eventType, Long organizationId,
                                            ApiEnvironment environment, String reference, BigDecimal amount,
                                            CurrencyCode currency, ChargeChannel channel, String email,
                                            String sourceSystem, String sourceReferenceId, String customerReferenceId,
                                            Long terminalAssignmentId, Map<String, Object> metadata, Payload payload) {
    public CheckoutPaymentRequestedEvent {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(Long organizationId, ApiEnvironment environment, String reference, BigDecimal amount,
                          CurrencyCode currency, ChargeChannel channel, String email, String sourceSystem,
                          String sourceReferenceId, String customerReferenceId, Long terminalAssignmentId,
                          Map<String, Object> metadata) {
        public Payload {
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    public Long resolveOrganizationId() {
        return payload != null && payload.organizationId() != null ? payload.organizationId() : organizationId;
    }

    public ApiEnvironment resolveEnvironment() {
        if (payload != null && payload.environment() != null) {
            return payload.environment();
        }
        return environment != null ? environment : ApiEnvironment.TEST;
    }

    public String resolveReference() {
        return payload != null && payload.reference() != null ? payload.reference() : reference;
    }

    public BigDecimal resolveAmount() {
        return payload != null && payload.amount() != null ? payload.amount() : amount;
    }

    public CurrencyCode resolveCurrency() {
        if (payload != null && payload.currency() != null) {
            return payload.currency();
        }
        return currency != null ? currency : CurrencyCode.NGN;
    }

    public ChargeChannel resolveChannel() {
        if (payload != null && payload.channel() != null) {
            return payload.channel();
        }
        return channel != null ? channel : ChargeChannel.CARD;
    }

    public String resolveEmail() {
        return payload != null && payload.email() != null ? payload.email() : email;
    }

    public String resolveSourceSystem() {
        String src = payload != null && payload.sourceSystem() != null ? payload.sourceSystem() : sourceSystem;
        return src != null && !src.isBlank() ? src : "COMMERCE";
    }

    public String resolveSourceReferenceId() {
        return payload != null && payload.sourceReferenceId() != null ? payload.sourceReferenceId() : sourceReferenceId;
    }

    public String resolveCustomerReferenceId() {
        return payload != null ? payload.customerReferenceId() : customerReferenceId;
    }

    public Long resolveTerminalAssignmentId() {
        return payload != null ? payload.terminalAssignmentId() : terminalAssignmentId;
    }

    public Map<String, Object> resolveMetadata() {
        return payload != null && payload.metadata() != null ? payload.metadata() : metadata;
    }
}
