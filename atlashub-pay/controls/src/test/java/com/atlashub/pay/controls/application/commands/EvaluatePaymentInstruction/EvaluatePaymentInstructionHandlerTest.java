package com.atlashub.pay.controls.application.commands.EvaluatePaymentInstruction;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.pay.controls.domain.services.FundUsageEvaluationService;
import com.atlashub.pay.controls.domain.services.ProviderCapabilityService;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluatePaymentInstructionHandlerTest {

    @Mock
    private PaymentControlDecisionRepository decisionRepository;

    @Mock
    private ControlledFundsPolicyRepository policyRepository;

    private FundUsageEvaluationService evaluationService;
    private ProviderCapabilityService capabilityService;
    private EvaluatePaymentInstructionHandler handler;

    @BeforeEach
    void setUp() {
        evaluationService = new FundUsageEvaluationService();
        capabilityService = new ProviderCapabilityService();
        handler = new EvaluatePaymentInstructionHandler(
                decisionRepository,
                policyRepository,
                evaluationService,
                capabilityService
        );
    }

    @Test
    @DisplayName("Should evaluate instruction and record decision")
    void shouldEvaluateInstruction() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.UNRESTRICTED, null, null, null, null, null, null, null, null
        );
        policy.activate("ADMIN");

        when(decisionRepository.findByPaymentInstructionReference("TX-001")).thenReturn(Optional.empty());
        when(policyRepository.findActiveByFacilityId(100L)).thenReturn(Optional.of(policy));
        when(decisionRepository.nextIdentity()).thenReturn(50L);

        EvaluatePaymentInstructionCommand command = new EvaluatePaymentInstructionCommand(
                100L,
                10L,
                "TX-001",
                PaymentRail.ATOMIC_TRANSFER,
                PaymentContext.PAYOUT,
                "SUPPLIES",
                "ACC-123",
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                null
        );

        PaymentControlDecisionResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(50L, result.decisionId());
        assertEquals("ALLOW", result.evaluatedAction());
        assertEquals("ALLOW", result.enforcedAction());
        verify(decisionRepository).save(any(PaymentControlDecision.class));
    }
}
