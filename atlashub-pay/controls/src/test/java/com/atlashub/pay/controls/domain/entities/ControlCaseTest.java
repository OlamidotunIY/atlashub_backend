package com.atlashub.pay.controls.domain.entities;

import com.atlashub.pay.controls.domain.events.ControlCaseResolvedEvent;
import com.atlashub.pay.controls.domain.exceptions.InvalidControlTransitionException;
import com.atlashub.pay.controls.domain.valueobject.ControlCaseStatus;
import com.atlashub.pay.controls.domain.valueobject.EvidenceRequirement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlCaseTest {

    @Test
    @DisplayName("Should open case in EVIDENCE_REQUESTED status when requirements provided")
    void shouldOpenCaseWithEvidenceRequested() {
        List<EvidenceRequirement> reqs = List.of(new EvidenceRequirement("INVOICE", "Supplier invoice", true));
        ControlCase controlCase = ControlCase.open(1L, 10L, 5L, reqs);

        assertNotNull(controlCase);
        assertEquals(ControlCaseStatus.EVIDENCE_REQUESTED, controlCase.getStatus());
        assertEquals(1, controlCase.getRequestedEvidence().size());
    }

    @Test
    @DisplayName("Should submit evidence and transition to UNDER_REVIEW")
    void shouldSubmitEvidence() {
        ControlCase controlCase = ControlCase.open(1L, 10L, 5L, null);
        assertEquals(ControlCaseStatus.OPEN, controlCase.getStatus());

        controlCase.submitEvidence("DOC-REF-001");

        assertEquals(ControlCaseStatus.UNDER_REVIEW, controlCase.getStatus());
        assertEquals(1, controlCase.getSubmittedEvidenceRefs().size());
    }

    @Test
    @DisplayName("Should approve release and register ControlCaseResolvedEvent")
    void shouldApproveRelease() {
        ControlCase controlCase = ControlCase.open(1L, 10L, 5L, null);
        controlCase.submitEvidence("DOC-REF-001");

        controlCase.approveRelease(999L, "Valid supplier invoice verified");

        assertEquals(ControlCaseStatus.RELEASED, controlCase.getStatus());
        assertEquals(999L, controlCase.getResolvedBy());
        assertEquals(1, controlCase.peekDomainEvents().size());
        assertTrue(controlCase.peekDomainEvents().get(0) instanceof ControlCaseResolvedEvent);
    }

    @Test
    @DisplayName("Should reject release and register ControlCaseResolvedEvent")
    void shouldRejectRelease() {
        ControlCase controlCase = ControlCase.open(1L, 10L, 5L, null);

        controlCase.rejectRelease(999L, "Suspicious merchant account");

        assertEquals(ControlCaseStatus.REJECTED, controlCase.getStatus());
        assertEquals(1, controlCase.peekDomainEvents().size());
    }

    @Test
    @DisplayName("Should throw when submitting evidence on closed case")
    void shouldThrowWhenSubmittingEvidenceOnClosedCase() {
        ControlCase controlCase = ControlCase.open(1L, 10L, 5L, null);
        controlCase.close();

        assertThrows(InvalidControlTransitionException.class, () -> controlCase.submitEvidence("REF-123"));
    }
}
