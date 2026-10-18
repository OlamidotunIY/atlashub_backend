package com.atlashub.pay.controls.domain.services;

import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.pay.controls.domain.valueobject.ProviderControlCapability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderCapabilityServiceTest {

    private ProviderCapabilityService capabilityService;

    @BeforeEach
    void setUp() {
        capabilityService = new ProviderCapabilityService();
    }

    @Test
    @DisplayName("Should maintain freeze on internal ledger balance")
    void shouldMaintainFreezeOnInternalLedger() {
        ProviderCapabilityService.DowngradeResult result = capabilityService.resolveEnforcedAction(
                ControlAction.FREEZE,
                PaymentRail.INTERNAL_LEDGER,
                Set.of(ProviderControlCapability.FREEZE_FUNDS)
        );

        assertEquals(ControlAction.FREEZE, result.enforcedAction());
        assertFalse(result.downgraded());
    }

    @Test
    @DisplayName("Should downgrade freeze to review on external accounts and never claim to freeze")
    void shouldDowngradeFreezeToReviewOnExternalAccount() {
        ProviderCapabilityService.DowngradeResult result = capabilityService.resolveEnforcedAction(
                ControlAction.FREEZE,
                PaymentRail.EXTERNAL_ACCOUNT,
                Set.of(ProviderControlCapability.FREEZE_FUNDS)
        );

        assertEquals(ControlAction.REVIEW, result.enforcedAction());
        assertTrue(result.downgraded());
        assertTrue(result.reason().contains("External bank accounts cannot be frozen"));
    }

    @Test
    @DisplayName("Should downgrade freeze to block on atomic transfer rail before submission")
    void shouldDowngradeFreezeToBlockOnTransfer() {
        ProviderCapabilityService.DowngradeResult result = capabilityService.resolveEnforcedAction(
                ControlAction.FREEZE,
                PaymentRail.ATOMIC_TRANSFER,
                Set.of()
        );

        assertEquals(ControlAction.BLOCK, result.enforcedAction());
        assertTrue(result.downgraded());
    }

    @Test
    @DisplayName("Should downgrade hold to review when provider lacks capability")
    void shouldDowngradeHoldToReviewWhenLackingCapability() {
        ProviderCapabilityService.DowngradeResult result = capabilityService.resolveEnforcedAction(
                ControlAction.HOLD,
                PaymentRail.ATOMIC_TRANSFER,
                Set.of() // empty capabilities
        );

        assertEquals(ControlAction.REVIEW, result.enforcedAction());
        assertTrue(result.downgraded());
    }
}
