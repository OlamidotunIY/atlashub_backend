package com.atlashub.pay.controls.application.commands.ResolveControlCase;

import com.atlashub.pay.controls.domain.entities.ControlCase;
import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.repositories.ControlCaseRepository;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlCaseStatus;
import com.atlashub.pay.controls.domain.valueobject.ControlReason;
import com.atlashub.pay.controls.domain.valueobject.ControlStatus;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveControlCaseHandlerTest {

    @Mock
    private ControlCaseRepository caseRepository;

    @Mock
    private PaymentControlDecisionRepository decisionRepository;

    private ResolveControlCaseHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ResolveControlCaseHandler(caseRepository, decisionRepository);
    }

    @Test
    @DisplayName("Should approve and release control case and linked decision")
    void shouldApproveControlCase() {
        ControlCase controlCase = ControlCase.open(1L, 10L, 5L, null);
        PaymentControlDecision decision = PaymentControlDecision.evaluate(
                10L, 1L, 100L, 5L, "TX-01",
                PaymentRail.ATOMIC_TRANSFER, PaymentContext.PAYOUT,
                ControlAction.HOLD, ControlAction.HOLD,
                Set.of(ControlReason.BENEFICIARY_NOT_APPROVED), null
        );
        decision.confirmApplied("PROV-HOLD-1");

        when(caseRepository.findById(1L)).thenReturn(Optional.of(controlCase));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(decision));

        ResolveControlCaseCommand command = new ResolveControlCaseCommand(
                1L,
                999L,
                true,
                "Approved after review"
        );

        handler.execute(command);

        assertEquals(ControlCaseStatus.RELEASED, controlCase.getStatus());
        assertEquals(ControlStatus.RELEASED, decision.getStatus());
        verify(caseRepository).save(controlCase);
        verify(decisionRepository).save(decision);
    }
}
