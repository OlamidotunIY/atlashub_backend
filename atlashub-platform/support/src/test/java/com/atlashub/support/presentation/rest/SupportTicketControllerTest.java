package com.atlashub.support.presentation.rest;

import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.support.application.command.AddTicketMessage.AddTicketMessageCommand;
import com.atlashub.support.application.command.AddTicketMessage.AddTicketMessageHandler;
import com.atlashub.support.application.command.AddTicketMessage.AddTicketMessageResult;
import com.atlashub.support.application.command.AssignTicket.AssignTicketCommand;
import com.atlashub.support.application.command.AssignTicket.AssignTicketHandler;
import com.atlashub.support.application.command.AssignTicket.AssignTicketResult;
import com.atlashub.support.application.command.CloseTicket.CloseTicketCommand;
import com.atlashub.support.application.command.CloseTicket.CloseTicketHandler;
import com.atlashub.support.application.command.CloseTicket.CloseTicketResult;
import com.atlashub.support.application.command.EscalateTicket.EscalateTicketCommand;
import com.atlashub.support.application.command.EscalateTicket.EscalateTicketHandler;
import com.atlashub.support.application.command.EscalateTicket.EscalateTicketResult;
import com.atlashub.support.application.command.OpenTicket.OpenTicketCommand;
import com.atlashub.support.application.command.OpenTicket.OpenTicketHandler;
import com.atlashub.support.application.command.OpenTicket.OpenTicketResult;
import com.atlashub.support.application.command.ReopenTicket.ReopenTicketCommand;
import com.atlashub.support.application.command.ReopenTicket.ReopenTicketHandler;
import com.atlashub.support.application.command.ReopenTicket.ReopenTicketResult;
import com.atlashub.support.application.command.ResolveTicket.ResolveTicketCommand;
import com.atlashub.support.application.command.ResolveTicket.ResolveTicketHandler;
import com.atlashub.support.application.command.ResolveTicket.ResolveTicketResult;
import com.atlashub.support.application.command.StartTicketProgress.StartTicketProgressCommand;
import com.atlashub.support.application.command.StartTicketProgress.StartTicketProgressHandler;
import com.atlashub.support.application.command.StartTicketProgress.StartTicketProgressResult;
import com.atlashub.support.application.query.GetSlaMetrics.GetSlaMetricsHandler;
import com.atlashub.support.application.query.GetSlaMetrics.GetSlaMetricsQuery;
import com.atlashub.support.application.query.GetSlaMetrics.SlaMetricsResult;
import com.atlashub.support.application.query.GetTicketDetails.GetTicketDetailsHandler;
import com.atlashub.support.application.query.GetTicketDetails.GetTicketDetailsQuery;
import com.atlashub.support.application.query.GetTicketDetails.TicketDetailsResult;
import com.atlashub.support.application.query.ListAgentTickets.ListAgentTicketsHandler;
import com.atlashub.support.application.query.ListAgentTickets.ListAgentTicketsQuery;
import com.atlashub.support.application.query.ListAllTickets.ListAllTicketsHandler;
import com.atlashub.support.application.query.ListAllTickets.ListAllTicketsQuery;
import com.atlashub.support.application.query.ListMyTickets.ListMyTicketsHandler;
import com.atlashub.support.application.query.ListMyTickets.ListMyTicketsQuery;
import com.atlashub.support.application.query.ListMyTickets.TicketSummaryResult;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import com.atlashub.support.presentation.dto.AddTicketMessageRequest;
import com.atlashub.support.presentation.dto.AssignTicketRequest;
import com.atlashub.support.presentation.dto.EscalateTicketRequest;
import com.atlashub.support.presentation.dto.OpenTicketRequest;
import com.atlashub.support.presentation.dto.OpenTicketResponse;
import com.atlashub.support.presentation.dto.ReopenTicketRequest;
import com.atlashub.support.presentation.dto.ResolveTicketRequest;
import com.atlashub.support.presentation.dto.SlaMetricsResponse;
import com.atlashub.support.presentation.dto.TicketDetailsResponse;
import com.atlashub.support.presentation.dto.TicketSummaryResponse;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportTicketControllerTest {

    @Mock
    private OpenTicketHandler openTicketHandler;
    @Mock
    private AddTicketMessageHandler addTicketMessageHandler;
    @Mock
    private AssignTicketHandler assignTicketHandler;
    @Mock
    private StartTicketProgressHandler startTicketProgressHandler;
    @Mock
    private ResolveTicketHandler resolveTicketHandler;
    @Mock
    private CloseTicketHandler closeTicketHandler;
    @Mock
    private ReopenTicketHandler reopenTicketHandler;
    @Mock
    private EscalateTicketHandler escalateTicketHandler;
    @Mock
    private ListMyTicketsHandler listMyTicketsHandler;
    @Mock
    private GetTicketDetailsHandler getTicketDetailsHandler;
    @Mock
    private ListAgentTicketsHandler listAgentTicketsHandler;
    @Mock
    private ListAllTicketsHandler listAllTicketsHandler;
    @Mock
    private GetSlaMetricsHandler getSlaMetricsHandler;

    private SupportTicketController controller;

    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            50L, 100L, "LIVE", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1)
    );

    @BeforeEach
    void setUp() {
        controller = new SupportTicketController(
                openTicketHandler,
                addTicketMessageHandler,
                assignTicketHandler,
                startTicketProgressHandler,
                resolveTicketHandler,
                closeTicketHandler,
                reopenTicketHandler,
                escalateTicketHandler,
                listMyTicketsHandler,
                getTicketDetailsHandler,
                listAgentTicketsHandler,
                listAllTicketsHandler,
                getSlaMetricsHandler
        );
    }

    @Test
    @DisplayName("Should open ticket and return CREATED response")
    void shouldOpenTicket() {
        OpenTicketRequest request = new OpenTicketRequest(
                "Payment issue", "Charged twice", TicketCategory.PAYMENT_DISPUTE,
                TicketPriority.HIGH, "Charge", "chg_1"
        );
        OpenTicketResult result = new OpenTicketResult(1L, "TKT-2026-00001");
        when(openTicketHandler.execute(any(OpenTicketCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<OpenTicketResponse>> response = controller.openTicket(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("TKT-2026-00001", response.getBody().data().ticketNumber());
    }

    @Test
    @DisplayName("Should add message to ticket thread")
    void shouldAddMessage() {
        AddTicketMessageRequest request = new AddTicketMessageRequest("Here is info", false, null);
        AddTicketMessageResult result = new AddTicketMessageResult(201L, 1L);
        when(addTicketMessageHandler.execute(any(AddTicketMessageCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<AddTicketMessageResult>> response = controller.addMessage(principal, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(201L, response.getBody().data().messageId());
    }

    @Test
    @DisplayName("Should assign ticket to agent")
    void shouldAssignTicket() {
        AssignTicketRequest request = new AssignTicketRequest(900L);
        AssignTicketResult result = new AssignTicketResult(1L, 900L, "ASSIGNED");
        when(assignTicketHandler.execute(any(AssignTicketCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<AssignTicketResult>> response = controller.assignTicket(principal, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(900L, response.getBody().data().agentId());
    }

    @Test
    @DisplayName("Should start progress on ticket")
    void shouldStartProgress() {
        StartTicketProgressResult result = new StartTicketProgressResult(1L, "IN_PROGRESS");
        when(startTicketProgressHandler.execute(any(StartTicketProgressCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<StartTicketProgressResult>> response = controller.startProgress(principal, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("IN_PROGRESS", response.getBody().data().status());
    }

    @Test
    @DisplayName("Should resolve ticket")
    void shouldResolveTicket() {
        ResolveTicketRequest request = new ResolveTicketRequest("Issue resolved");
        ResolveTicketResult result = new ResolveTicketResult(1L, "RESOLVED", "Issue resolved", ZonedDateTime.now());
        when(resolveTicketHandler.execute(any(ResolveTicketCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<ResolveTicketResult>> response = controller.resolveTicket(principal, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("RESOLVED", response.getBody().data().status());
    }

    @Test
    @DisplayName("Should close ticket")
    void shouldCloseTicket() {
        CloseTicketResult result = new CloseTicketResult(1L, "CLOSED", ZonedDateTime.now(), 50L);
        when(closeTicketHandler.execute(any(CloseTicketCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<CloseTicketResult>> response = controller.closeTicket(principal, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("CLOSED", response.getBody().data().status());
    }

    @Test
    @DisplayName("Should reopen ticket")
    void shouldReopenTicket() {
        ReopenTicketRequest request = new ReopenTicketRequest("Further help needed");
        ReopenTicketResult result = new ReopenTicketResult(1L, "OPEN");
        when(reopenTicketHandler.execute(any(ReopenTicketCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<ReopenTicketResult>> response = controller.reopenTicket(principal, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("OPEN", response.getBody().data().status());
    }

    @Test
    @DisplayName("Should escalate ticket")
    void shouldEscalateTicket() {
        EscalateTicketRequest request = new EscalateTicketRequest(999L, "Escalating to lead");
        EscalateTicketResult result = new EscalateTicketResult(1L, 999L);
        when(escalateTicketHandler.execute(any(EscalateTicketCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<EscalateTicketResult>> response = controller.escalateTicket(principal, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(999L, response.getBody().data().assignedTo());
    }

    @Test
    @DisplayName("Should list organization tickets")
    void shouldListMyTickets() {
        TicketSummaryResult summary = new TicketSummaryResult(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.GENERAL, TicketPriority.LOW,
                "Inquiry", TicketStatus.OPEN, null, false,
                ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(3),
                ZonedDateTime.now(), ZonedDateTime.now()
        );
        when(listMyTicketsHandler.execute(any(ListMyTicketsQuery.class))).thenReturn(List.of(summary));

        ResponseEntity<ApiResponse<List<TicketSummaryResponse>>> response = controller.listMyTickets(principal, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1, response.getBody().data().size());
    }

    @Test
    @DisplayName("Should get ticket details")
    void shouldGetTicketDetails() {
        TicketDetailsResult details = new TicketDetailsResult(
                1L, "TKT-2026-00001", 100L, 50L,
                TicketCategory.GENERAL, TicketPriority.LOW,
                "Inquiry", TicketStatus.OPEN, null, false,
                null, null,
                ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(3),
                null, null, null, null, null,
                ZonedDateTime.now(), ZonedDateTime.now(),
                List.of()
        );
        when(getTicketDetailsHandler.execute(any(GetTicketDetailsQuery.class))).thenReturn(details);

        ResponseEntity<ApiResponse<TicketDetailsResponse>> response = controller.getTicketDetails(principal, 1L, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("TKT-2026-00001", response.getBody().data().ticketNumber());
    }

    @Test
    @DisplayName("Should get SLA metrics")
    void shouldGetSlaMetrics() {
        SlaMetricsResult metrics = new SlaMetricsResult(
                10, 90.0, 80.0, 15.0, 120.0, 1, List.of()
        );
        when(getSlaMetricsHandler.execute(any(GetSlaMetricsQuery.class))).thenReturn(metrics);

        ResponseEntity<ApiResponse<SlaMetricsResponse>> response = controller.getSlaMetrics(
                principal, ZonedDateTime.now().minusDays(30), ZonedDateTime.now());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(10, response.getBody().data().totalTickets());
    }
}
