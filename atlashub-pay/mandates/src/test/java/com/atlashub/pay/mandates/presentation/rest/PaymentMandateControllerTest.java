package com.atlashub.pay.mandates.presentation.rest;

import com.atlashub.pay.mandates.application.commands.CreateMandate.CreateMandateCommand;
import com.atlashub.pay.mandates.application.commands.CreateMandate.CreateMandateHandler;
import com.atlashub.pay.mandates.application.commands.CreateMandate.CreateMandateResponse;
import com.atlashub.pay.mandates.application.commands.PauseMandate.PauseMandateCommand;
import com.atlashub.pay.mandates.application.commands.PauseMandate.PauseMandateHandler;
import com.atlashub.pay.mandates.application.commands.ResumeMandate.ResumeMandateCommand;
import com.atlashub.pay.mandates.application.commands.ResumeMandate.ResumeMandateHandler;
import com.atlashub.pay.mandates.application.commands.RevokeMandate.RevokeMandateCommand;
import com.atlashub.pay.mandates.application.commands.RevokeMandate.RevokeMandateHandler;
import com.atlashub.pay.mandates.application.queries.GetMandate.GetMandateHandler;
import com.atlashub.pay.mandates.application.queries.GetMandate.GetMandateQuery;
import com.atlashub.pay.mandates.application.queries.GetMandate.MandateResult;
import com.atlashub.pay.mandates.application.queries.ListMandates.ListMandatesHandler;
import com.atlashub.pay.mandates.application.queries.ListMandates.ListMandatesQuery;
import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.pay.mandates.presentation.dto.CreateMandateRequest;
import com.atlashub.pay.mandates.presentation.dto.MandateResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMandateControllerTest {

    @Mock
    private CreateMandateHandler createMandateHandler;

    @Mock
    private PauseMandateHandler pauseMandateHandler;

    @Mock
    private ResumeMandateHandler resumeMandateHandler;

    @Mock
    private RevokeMandateHandler revokeMandateHandler;

    @Mock
    private GetMandateHandler getMandateHandler;

    @Mock
    private ListMandatesHandler listMandatesHandler;

    private PaymentMandateController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new PaymentMandateController(
                createMandateHandler,
                pauseMandateHandler,
                resumeMandateHandler,
                revokeMandateHandler,
                getMandateHandler,
                listMandatesHandler
        );
        principal = new AuthenticatedPrincipal(100L, 10L, "TEST", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("POST /api/v1/pay/mandates should invoke CreateMandateHandler")
    void shouldCreateMandate() {
        CreateMandateRequest request = new CreateMandateRequest(
                "CUST-001",
                "customer@example.com",
                new BigDecimal("15000.00"),
                "NGN",
                MandateFrequency.MONTHLY,
                "AUTH_12345",
                LocalDate.of(2026, 12, 1)
        );
        CreateMandateResponse response = new CreateMandateResponse(1L, "ACTIVE", LocalDate.of(2026, 12, 1));
        when(createMandateHandler.execute(any(CreateMandateCommand.class))).thenReturn(response);

        ResponseEntity<ApiResponse<CreateMandateResponse>> result = controller.createMandate(request, principal);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().data().mandateId());
        verify(createMandateHandler).execute(any(CreateMandateCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/pay/mandates/{id}/pause should invoke PauseMandateHandler")
    void shouldPauseMandate() {
        ResponseEntity<ApiResponse<Void>> result = controller.pauseMandate(1L, principal);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(pauseMandateHandler).execute(new PauseMandateCommand(1L, 10L));
    }

    @Test
    @DisplayName("POST /api/v1/pay/mandates/{id}/resume should invoke ResumeMandateHandler")
    void shouldResumeMandate() {
        ResponseEntity<ApiResponse<Void>> result = controller.resumeMandate(1L, principal);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(resumeMandateHandler).execute(new ResumeMandateCommand(1L, 10L));
    }

    @Test
    @DisplayName("POST /api/v1/pay/mandates/{id}/revoke should invoke RevokeMandateHandler")
    void shouldRevokeMandate() {
        ResponseEntity<ApiResponse<Void>> result = controller.revokeMandate(1L, principal);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(revokeMandateHandler).execute(new RevokeMandateCommand(1L, 10L));
    }

    @Test
    @DisplayName("GET /api/v1/pay/mandates should invoke ListMandatesHandler")
    void shouldListMandates() {
        MandateResult resultItem = new MandateResult(
                1L, 10L, "CUST-1", "c@example.com", new BigDecimal("5000.00"),
                "NGN", "MONTHLY", "ACTIVE", LocalDate.now(), ZonedDateTime.now(), null
        );
        when(listMandatesHandler.execute(any(ListMandatesQuery.class))).thenReturn(List.of(resultItem));

        ResponseEntity<ApiResponse<List<MandateResponse>>> result = controller.listMandates("CUST-1", MandateStatus.ACTIVE, principal);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().data().size());
        verify(listMandatesHandler).execute(any(ListMandatesQuery.class));
    }

    @Test
    @DisplayName("GET /api/v1/pay/mandates/{id} should invoke GetMandateHandler")
    void shouldGetMandate() {
        MandateResult resultItem = new MandateResult(
                1L, 10L, "CUST-1", "c@example.com", new BigDecimal("5000.00"),
                "NGN", "MONTHLY", "ACTIVE", LocalDate.now(), ZonedDateTime.now(), null
        );
        when(getMandateHandler.execute(any(GetMandateQuery.class))).thenReturn(resultItem);

        ResponseEntity<ApiResponse<MandateResponse>> result = controller.getMandate(1L, principal);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().data().id());
        verify(getMandateHandler).execute(any(GetMandateQuery.class));
    }
}
