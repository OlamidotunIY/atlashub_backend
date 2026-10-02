package com.atlashub.pay.ledger.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public final class LedgerInboundEvents {
    private LedgerInboundEvents() {
    }

    public record OrganizationBankingActivatedEvent(
            Long organizationId, Long bankingProfileId, Long businessDepositAccountId,
            Long businessSubAccountId, String currency, ZonedDateTime activatedAt) {
    }

    public record ReservedAccountActivatedEvent(
            Long reservedAccountId, Long organizationId, String ownerType,
            String ownerReferenceId, String currency) {
    }

    public record OutletCreatedEvent(
            Long outletId, Long organizationId, String outletName, String currency) {
    }

    public record ChargeSuccessfulEvent(
            Long chargeId, Long organizationId, String chargeReference, String gatewayReference,
            BigDecimal amount, String currency, String channel, String sourceSystem,
            String sourceReferenceId, Long customerId, ZonedDateTime succeededAt) {
    }

    public record ReservedAccountFundedEvent(
            Long reservedAccountId, Long organizationId, String ownerType, String ownerReferenceId,
            Long businessSubAccountId, String anchorTransferReference, BigDecimal amount,
            String currency, String senderAccountName, String senderBankCode, ZonedDateTime receivedAt) {
    }

    public record OrganizationAccountFundedEvent(
            Long organizationId, Long businessAccountId, String anchorTransferReference,
            BigDecimal amount, String currency, ZonedDateTime receivedAt) {
    }

    public record PayoutCompletedEvent(
            Long payoutId, Long organizationId, String payoutReference, BigDecimal amount,
            String currency, String sourceSystem, String sourceReferenceId,
            String recipientName, String recipientAccountNumber, ZonedDateTime completedAt) {
    }

    public record TillOpenedEvent(
            String tillSessionId, Long organizationId, Long outletId, Long cashierId,
            BigDecimal openingFloat, String currency, ZonedDateTime openedAt) {
    }

    public record TillClosedEvent(
            String tillSessionId, Long organizationId, Long outletId, Long cashierId,
            BigDecimal closingCash, BigDecimal totalSales, String currency, ZonedDateTime closedAt) {
    }

    public record OrganizationBannedEvent(
            Long organizationId, Long bannedByStaffId, String reason, ZonedDateTime bannedAt) {
    }

    public record OrganizationUnbannedEvent(
            Long organizationId, Long unbannedByStaffId, ZonedDateTime unbannedAt) {
    }

    public record OrganizationComplianceSuspendedEvent(
            Long organizationId, String reason, ZonedDateTime suspendedAt) {
    }

    public record OrganizationComplianceReinstatedEvent(
            Long organizationId, ZonedDateTime reinstatedAt) {
    }
}
