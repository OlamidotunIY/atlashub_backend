package com.atlashub.shared.application.port;

import java.time.ZonedDateTime;

public interface ProviderPaymentControlPort {

    record HoldInstructionRequest(
            String paymentInstructionReference,
            Long facilityId,
            Long organizationId,
            String rail,
            String reason
    ) {}

    record ReleaseHoldRequest(
            String paymentInstructionReference,
            Long facilityId,
            Long organizationId,
            String providerControlReference,
            String reason
    ) {}

    record BlockInstructionRequest(
            String paymentInstructionReference,
            Long facilityId,
            Long organizationId,
            String rail,
            String reason
    ) {}

    record FreezeFundsRequest(
            String accountReference,
            Long facilityId,
            Long organizationId,
            String rail,
            String reason
    ) {}

    record UnfreezeFundsRequest(
            String accountReference,
            Long facilityId,
            Long organizationId,
            String providerControlReference,
            String reason
    ) {}

    record ControlOperationResult(
            boolean successful,
            String status,
            String providerControlReference,
            String errorMessage,
            ZonedDateTime timestamp
    ) {
        public static ControlOperationResult success(String status, String providerControlReference) {
            return new ControlOperationResult(true, status, providerControlReference, null, ZonedDateTime.now());
        }

        public static ControlOperationResult pending(String providerControlReference) {
            return new ControlOperationResult(true, "PENDING_PROVIDER", providerControlReference, null, ZonedDateTime.now());
        }

        public static ControlOperationResult failure(String errorMessage) {
            return new ControlOperationResult(false, "FAILED", null, errorMessage, ZonedDateTime.now());
        }
    }

    ControlOperationResult holdInstruction(HoldInstructionRequest request);

    ControlOperationResult releaseHold(ReleaseHoldRequest request);

    ControlOperationResult blockInstruction(BlockInstructionRequest request);

    ControlOperationResult freezeFunds(FreezeFundsRequest request);

    ControlOperationResult unfreezeFunds(UnfreezeFundsRequest request);
}
