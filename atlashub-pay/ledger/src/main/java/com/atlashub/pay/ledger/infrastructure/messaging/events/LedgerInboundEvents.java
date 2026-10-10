package com.atlashub.pay.ledger.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import com.atlashub.shared.domain.valueobject.Money;

public final class LedgerInboundEvents {
    private LedgerInboundEvents() {
    }

    public record OrganizationBankingActivatedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, Long bankingProfileId, Long businessDepositAccountId,
                              Long businessSubAccountId, String environment, String currency,
                              ZonedDateTime activatedAt) {}
    }

    public record ReservedAccountActivatedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long reservedAccountId, Long organizationId, String ownerType,
                              String ownerReferenceId, Long businessSubAccountId,
                              String anchorReservedAccountId, String accountName,
                              String maskedAccountNumber, String bankName, String environment,
                              String currency, ZonedDateTime activatedAt) {}
    }

    public record OutletCreatedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long outletId, Long organizationId, String name, String address,
                              String city, String state, String country, String currency) {}
    }

    public record ChargeSuccessfulEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, String environment, String chargeReference,
                              String gatewayReference, Money amount, Money providerFee, String channel,
                              String provider, String sourceSystem, String sourceReferenceId,
                              String customerReferenceId, ZonedDateTime succeededAt) {}
    }

    public record ChargeRefundedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, String environment, String chargeReference,
                              String providerRefundReference, Money amount, String sourceSystem,
                              String sourceReferenceId, String customerReferenceId, ZonedDateTime refundedAt) {}
    }

    public record ProviderSettlementReceivedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, String environment, String provider,
                              String settlementReference, Money amount, ZonedDateTime settledAt) {}
    }

    public record ReservedAccountFundedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long reservedAccountId, Long organizationId, String environment,
                              String ownerType, String ownerReferenceId, Long businessSubAccountId,
                              String anchorTransferReference, BigDecimal amount, String currency,
                              String senderAccountName, String senderBankCode, ZonedDateTime receivedAt) {}
    }

    public record OrganizationAccountFundedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, String environment, Long businessAccountId,
                              String anchorTransferReference, BigDecimal amount, String currency,
                              ZonedDateTime receivedAt) {}
    }

    public record PayoutCompletedEvent(
            Long payoutId, Long organizationId, String environment, String payoutReference, BigDecimal amount,
            String currency, String sourceSystem, String sourceReferenceId,
            String recipientName, String recipientAccountNumber, ZonedDateTime completedAt) {
    }

    public record TillOpenedEvent(
            String tillSessionId, Long organizationId, String environment, Long outletId, Long cashierId,
            BigDecimal openingFloat, String currency, ZonedDateTime openedAt) {
    }

    public record TillClosedEvent(
            String tillSessionId, Long organizationId, String environment, Long outletId, Long cashierId,
            BigDecimal closingCash, BigDecimal totalSales, String currency, ZonedDateTime closedAt) {
    }

    public record OrganizationBannedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, String reason, ZonedDateTime bannedAt) {}
    }

    public record OrganizationUnbannedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, ZonedDateTime unbannedAt) {}
    }

    public record OrganizationComplianceSuspendedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, String anchorBusinessCustomerId,
                              String reason, ZonedDateTime suspendedAt) {}
    }

    public record OrganizationComplianceReinstatedEvent(
            String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload) {
        public record Payload(Long organizationId, String anchorBusinessCustomerId,
                              String reason, ZonedDateTime reinstatedAt) {}
    }
}
