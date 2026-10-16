package com.atlashub.pay.controls.infrastructure.services;

import com.atlashub.shared.application.port.ProviderPaymentControlPort;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class ProviderPaymentControlAdapter implements ProviderPaymentControlPort {

    @Override
    public ControlOperationResult holdInstruction(HoldInstructionRequest request) {
        String reference = "HOLD-" + UUID.randomUUID().toString().substring(0, 8);
        return new ControlOperationResult(true, "APPLIED", reference, null, ZonedDateTime.now());
    }

    @Override
    public ControlOperationResult releaseHold(ReleaseHoldRequest request) {
        return new ControlOperationResult(true, "RELEASED", request.providerControlReference(), null, ZonedDateTime.now());
    }

    @Override
    public ControlOperationResult blockInstruction(BlockInstructionRequest request) {
        String reference = "BLOCK-" + UUID.randomUUID().toString().substring(0, 8);
        return new ControlOperationResult(true, "APPLIED", reference, null, ZonedDateTime.now());
    }

    @Override
    public ControlOperationResult freezeFunds(FreezeFundsRequest request) {
        String reference = "FREEZE-" + UUID.randomUUID().toString().substring(0, 8);
        return new ControlOperationResult(true, "APPLIED", reference, null, ZonedDateTime.now());
    }

    @Override
    public ControlOperationResult unfreezeFunds(UnfreezeFundsRequest request) {
        return new ControlOperationResult(true, "RELEASED", request.providerControlReference(), null, ZonedDateTime.now());
    }
}
