package com.atlashub.pay.txquery.infrastructure.messaging.events;

import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public final class TransactionInboundEvents {
    private TransactionInboundEvents() {
    }

    public record ChargeInitializedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                         String correlationId, ChargePayload payload) {
    }

    public record ChargeSuccessfulEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                        String correlationId, SuccessfulChargePayload payload) {
    }

    public record ChargeFailedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId,
                                    FailedChargePayload payload) {
    }

    public record ChargeRefundInitiatedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                             String correlationId, ChargeStatePayload payload) {
    }

    public record ChargeRefundedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId,
                                      ChargeStatePayload payload) {
    }

    public record ChargeDisputedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId,
                                      ChargeStatePayload payload) {
    }

    public record ChargeDisputeResolvedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                             String correlationId, ChargeStatePayload payload) {
    }

    public record ChargePayload(Long organizationId, String environment, String chargeReference, Money amount,
                                String channel, String provider, String sourceSystem, String sourceReferenceId,
                                String customerReferenceId, ZonedDateTime initializedAt) {
    }

    public record SuccessfulChargePayload(Long organizationId, String environment, String chargeReference,
                                          String gatewayReference, Money amount, Money providerFee, String channel,
                                          String provider, String sourceSystem, String sourceReferenceId,
                                          String customerReferenceId, ZonedDateTime succeededAt) {
    }

    public record FailedChargePayload(Long organizationId, String environment, String chargeReference, Money amount,
                                      String channel, String provider, String sourceSystem, String sourceReferenceId,
                                      String customerReferenceId, String reason, ZonedDateTime failedAt) {
    }

    public record ChargeStatePayload(Long organizationId, String environment, String chargeReference,
                                     @JsonAlias({"providerRefundReference", "disputeReference"}) String providerReference,
                                     Money amount, String sourceSystem, String sourceReferenceId,
                                     String customerReferenceId, @JsonAlias("resolution") String reason,
                                     @JsonAlias({"initiatedAt", "refundedAt", "disputedAt", "resolvedAt"}) ZonedDateTime occurredAt) {
    }

    public record LedgerTransactionPostedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                               String correlationId, LedgerPayload payload) {
    }

    public record LedgerPayload(Long transactionId, Long organizationId, String environment, String reference,
                                String sourceSystem, String sourceReferenceId, String description, String currency,
                                ZonedDateTime postedAt, List<LedgerEntryPayload> entries) {
    }

    public record LedgerEntryPayload(Long accountId, String entryType, BigDecimal amount) {
    }

    public record OrganizationAccountFundedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                                 String correlationId, OrganizationFundingPayload payload) {
    }

    public record OrganizationFundingPayload(Long organizationId, String environment, Long businessAccountId,
                                             String anchorTransferReference, BigDecimal amount, String currency,
                                             ZonedDateTime receivedAt) {
    }

    public record ReservedAccountFundedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                             String correlationId, ReservedFundingPayload payload) {
    }

    public record ReservedFundingPayload(Long reservedAccountId, Long organizationId, String environment,
                                         String ownerType, String ownerReferenceId, Long businessSubAccountId,
                                         String anchorTransferReference, BigDecimal amount, String currency,
                                         String senderAccountName, String senderBankCode, ZonedDateTime receivedAt) {
    }

    public record ProviderSettlementReceivedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                                  String correlationId, SettlementPayload payload) {
    }

    public record SettlementPayload(Long organizationId, String environment, String provider,
                                    String settlementReference, Money amount, ZonedDateTime settledAt) {
    }

    public record PayoutCompletedEvent(Long payoutId, Long organizationId, String environment, String payoutReference,
                                       BigDecimal amount, String currency, String sourceSystem,
                                       String sourceReferenceId, String recipientName, String recipientAccountNumber,
                                       ZonedDateTime completedAt) {
    }
}
