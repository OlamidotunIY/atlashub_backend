package com.atlashub.pay.controls.domain.services;

import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.pay.controls.domain.valueobject.ProviderControlCapability;

import java.util.Set;

public class ProviderCapabilityService {

    public record DowngradeResult(
            ControlAction enforcedAction,
            boolean downgraded,
            String reason
    ) {}

    public DowngradeResult resolveEnforcedAction(ControlAction requestedAction,
                                                PaymentRail paymentRail,
                                                Set<ProviderControlCapability> capabilities) {
        if (requestedAction == ControlAction.ALLOW) {
            return new DowngradeResult(ControlAction.ALLOW, false, null);
        }
        if (requestedAction == ControlAction.REVIEW) {
            return new DowngradeResult(ControlAction.REVIEW, false, null);
        }

        // Internal ledger supports holds, blocks, and balance freezes directly
        if (paymentRail == PaymentRail.INTERNAL_LEDGER) {
            return new DowngradeResult(requestedAction, false, null);
        }

        // External bank accounts outside AtlasHub control can NEVER be frozen
        if (paymentRail == PaymentRail.EXTERNAL_ACCOUNT) {
            if (requestedAction == ControlAction.FREEZE) {
                return new DowngradeResult(
                        ControlAction.REVIEW,
                        true,
                        "External bank accounts cannot be frozen; downgraded from FREEZE to REVIEW."
                );
            }
            if (requestedAction == ControlAction.HOLD) {
                return new DowngradeResult(
                        ControlAction.REVIEW,
                        true,
                        "External account rails do not support in-flight hold; downgraded from HOLD to REVIEW."
                );
            }
            if (requestedAction == ControlAction.BLOCK) {
                return new DowngradeResult(
                        ControlAction.REVIEW,
                        true,
                        "External account rails do not support post-submission block; downgraded from BLOCK to REVIEW."
                );
            }
        }

        // Atomic transfer rails (transfers before provider submission)
        if (paymentRail == PaymentRail.ATOMIC_TRANSFER) {
            if (requestedAction == ControlAction.FREEZE) {
                return new DowngradeResult(
                        ControlAction.BLOCK,
                        true,
                        "Transfer rail cannot freeze external funds; downgraded from FREEZE to BLOCK of transfer instruction."
                );
            }
            if (requestedAction == ControlAction.HOLD) {
                if (capabilities != null && capabilities.contains(ProviderControlCapability.HOLD_INSTRUCTION)) {
                    return new DowngradeResult(ControlAction.HOLD, false, null);
                }
                return new DowngradeResult(
                        ControlAction.REVIEW,
                        true,
                        "Provider lacks hold capability; downgraded from HOLD to REVIEW."
                );
            }
            if (requestedAction == ControlAction.BLOCK) {
                return new DowngradeResult(ControlAction.BLOCK, false, null);
            }
        }

        // Virtual account rail
        if (paymentRail == PaymentRail.VIRTUAL_ACCOUNT) {
            if (requestedAction == ControlAction.FREEZE) {
                if (capabilities != null && capabilities.contains(ProviderControlCapability.FREEZE_FUNDS)) {
                    return new DowngradeResult(ControlAction.FREEZE, false, null);
                }
                return new DowngradeResult(
                        ControlAction.HOLD,
                        true,
                        "Provider does not support virtual account freeze; downgraded from FREEZE to HOLD."
                );
            }
            if (requestedAction == ControlAction.HOLD) {
                if (capabilities != null && capabilities.contains(ProviderControlCapability.HOLD_INSTRUCTION)) {
                    return new DowngradeResult(ControlAction.HOLD, false, null);
                }
                return new DowngradeResult(
                        ControlAction.REVIEW,
                        true,
                        "Provider does not support virtual account hold; downgraded from HOLD to REVIEW."
                );
            }
            if (requestedAction == ControlAction.BLOCK) {
                if (capabilities != null && capabilities.contains(ProviderControlCapability.BLOCK_INSTRUCTION)) {
                    return new DowngradeResult(ControlAction.BLOCK, false, null);
                }
                return new DowngradeResult(
                        ControlAction.REVIEW,
                        true,
                        "Provider does not support virtual account block; downgraded from BLOCK to REVIEW."
                );
            }
        }

        return new DowngradeResult(requestedAction, false, null);
    }
}
