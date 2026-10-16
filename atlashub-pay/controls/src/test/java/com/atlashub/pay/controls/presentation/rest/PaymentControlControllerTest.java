package com.atlashub.pay.controls.presentation.rest;

import com.atlashub.pay.controls.application.commands.ResolveControlCase.ResolveControlCaseHandler;
import com.atlashub.pay.controls.application.commands.SubmitControlEvidence.SubmitControlEvidenceHandler;
import com.atlashub.pay.controls.application.queries.GetActiveControlledFundsPolicy.ControlledFundsPolicyResult;
import com.atlashub.pay.controls.application.queries.GetActiveControlledFundsPolicy.GetActiveControlledFundsPolicyHandler;
import com.atlashub.pay.controls.application.queries.GetActiveControlledFundsPolicy.GetActiveControlledFundsPolicyQuery;
import com.atlashub.pay.controls.application.queries.GetPaymentControlDecision.PaymentControlDecisionResult;
import com.atlashub.pay.controls.application.queries.ListControlCases.ControlCaseResult;
import com.atlashub.pay.controls.application.queries.ListControlCases.ListControlCasesHandler;
import com.atlashub.pay.controls.application.queries.ListControlCases.ListControlCasesQuery;
import com.atlashub.pay.controls.application.queries.ListControlDecisions.ListControlDecisionsHandler;
import com.atlashub.pay.controls.application.queries.ListControlDecisions.ListControlDecisionsQuery;
import com.atlashub.pay.controls.presentation.dto.ControlCaseResponse;
import com.atlashub.pay.controls.presentation.dto.ControlledFundsPolicyResponse;
import com.atlashub.pay.controls.presentation.dto.PaymentControlDecisionResponse;
import com.atlashub.pay.controls.presentation.dto.ResolveControlCaseRequest;
import com.atlashub.pay.controls.presentation.dto.SubmitControlEvidenceRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControlControllerTest {

    @Mock
    private GetActiveControlledFundsPolicyHandler getPolicyHandler;

    @Mock
    private ListControlDecisionsHandler listDecisionsHandler;

    @Mock
    private ListControlCasesHandler listCasesHandler;

    @Mock
    private SubmitControlEvidenceHandler submitEvidenceHandler;

    @Mock
    private ResolveControlCaseHandler resolveCaseHandler;

    private PaymentControlController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new PaymentControlController(
                getPolicyHandler,
                listDecisionsHandler,
                listCasesHandler,
                submitEvidenceHandler,
                resolveCaseHandler
        );
        principal = new AuthenticatedPrincipal(100L, 10L, "TEST", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Should get active controlled funds policy")
    void shouldGetActivePolicy() {
        ControlledFundsPolicyResult result = new ControlledFundsPolicyResult(
                1L, 100L, 10L, "LIVE", "v1.0", "PURPOSE_RESTRICTED",
                Set.of("RAW_MATERIALS"), Collections.emptySet(), "ACTIVE",
                ZonedDateTime.now(), null, "ADMIN"
        );
        when(getPolicyHandler.execute(any(GetActiveControlledFundsPolicyQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<ControlledFundsPolicyResponse>> response = controller.getActivePolicy(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().data().facilityId());
    }

    @Test
    @DisplayName("Should list decisions")
    void shouldListDecisions() {
        PaymentControlDecisionResult decisionResult = new PaymentControlDecisionResult(
                10L, 1L, 100L, 10L, "TX-01", "ATOMIC_TRANSFER", "PAYOUT",
                "ALLOW", "ALLOW", Collections.emptySet(), null, null,
                "APPLIED", ZonedDateTime.now(), ZonedDateTime.now()
        );
        PageResult<PaymentControlDecisionResult> page = new PageResult<>(
                List.of(decisionResult), 0, 20, 1, 1
        );
        when(listDecisionsHandler.execute(any(ListControlDecisionsQuery.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResult<PaymentControlDecisionResponse>>> response =
                controller.listDecisions(0, 20, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().content().size());
    }

    @Test
    @DisplayName("Should list control cases")
    void shouldListCases() {
        ControlCaseResult caseResult = new ControlCaseResult(
                5L, 10L, 10L, "OPEN", Collections.emptyList(),
                ZonedDateTime.now(), null, null, null
        );
        PageResult<ControlCaseResult> page = new PageResult<>(
                List.of(caseResult), 0, 20, 1, 1
        );
        when(listCasesHandler.execute(any(ListControlCasesQuery.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResult<ControlCaseResponse>>> response =
                controller.listCases(0, 20, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().content().size());
    }

    @Test
    @DisplayName("Should submit evidence for case")
    void shouldSubmitEvidence() {
        SubmitControlEvidenceRequest request = new SubmitControlEvidenceRequest("DOC-123", "Proof of delivery");

        ResponseEntity<ApiResponse<Void>> response = controller.submitEvidence(5L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(submitEvidenceHandler).execute(any());
    }

    @Test
    @DisplayName("Should resolve control case")
    void shouldResolveCase() {
        ResolveControlCaseRequest request = new ResolveControlCaseRequest(true, "Approved supplier invoice");

        ResponseEntity<ApiResponse<Void>> response = controller.resolveCase(5L, request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(resolveCaseHandler).execute(any());
    }
}
