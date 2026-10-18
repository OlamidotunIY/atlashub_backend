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
import com.atlashub.support.domain.valueobject.AuthorType;
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
import com.atlashub.support.presentation.dto.SlaCategoryBreakdownResponse;
import com.atlashub.support.presentation.dto.SlaMetricsResponse;
import com.atlashub.support.presentation.dto.TicketDetailsResponse;
import com.atlashub.support.presentation.dto.TicketMessageResponse;
import com.atlashub.support.presentation.dto.TicketSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/support")
@Tag(name = "Support", description = "Customer support ticketing and SLA tracking")
public class SupportTicketController {

    private final OpenTicketHandler openTicketHandler;
    private final AddTicketMessageHandler addTicketMessageHandler;
    private final AssignTicketHandler assignTicketHandler;
    private final StartTicketProgressHandler startTicketProgressHandler;
    private final ResolveTicketHandler resolveTicketHandler;
    private final CloseTicketHandler closeTicketHandler;
    private final ReopenTicketHandler reopenTicketHandler;
    private final EscalateTicketHandler escalateTicketHandler;
    private final ListMyTicketsHandler listMyTicketsHandler;
    private final GetTicketDetailsHandler getTicketDetailsHandler;
    private final ListAgentTicketsHandler listAgentTicketsHandler;
    private final ListAllTicketsHandler listAllTicketsHandler;
    private final GetSlaMetricsHandler getSlaMetricsHandler;

    public SupportTicketController(
            OpenTicketHandler openTicketHandler,
            AddTicketMessageHandler addTicketMessageHandler,
            AssignTicketHandler assignTicketHandler,
            StartTicketProgressHandler startTicketProgressHandler,
            ResolveTicketHandler resolveTicketHandler,
            CloseTicketHandler closeTicketHandler,
            ReopenTicketHandler reopenTicketHandler,
            EscalateTicketHandler escalateTicketHandler,
            ListMyTicketsHandler listMyTicketsHandler,
            GetTicketDetailsHandler getTicketDetailsHandler,
            ListAgentTicketsHandler listAgentTicketsHandler,
            ListAllTicketsHandler listAllTicketsHandler,
            GetSlaMetricsHandler getSlaMetricsHandler
    ) {
        this.openTicketHandler = openTicketHandler;
        this.addTicketMessageHandler = addTicketMessageHandler;
        this.assignTicketHandler = assignTicketHandler;
        this.startTicketProgressHandler = startTicketProgressHandler;
        this.resolveTicketHandler = resolveTicketHandler;
        this.closeTicketHandler = closeTicketHandler;
        this.reopenTicketHandler = reopenTicketHandler;
        this.escalateTicketHandler = escalateTicketHandler;
        this.listMyTicketsHandler = listMyTicketsHandler;
        this.getTicketDetailsHandler = getTicketDetailsHandler;
        this.listAgentTicketsHandler = listAgentTicketsHandler;
        this.listAllTicketsHandler = listAllTicketsHandler;
        this.getSlaMetricsHandler = getSlaMetricsHandler;
    }

    @PostMapping("/tickets")
    @Operation(summary = "Open a new support ticket", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OpenTicketResponse>> openTicket(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody OpenTicketRequest request
    ) {
        TicketPriority priority = request.priority() != null ? request.priority() : TicketPriority.LOW;
        OpenTicketCommand command = new OpenTicketCommand(
                principal.activeOrganizationId(),
                principal.userId(),
                request.category(),
                priority,
                request.subject(),
                request.description(),
                request.relatedEntityType(),
                request.relatedEntityId(),
                UUID.randomUUID().toString()
        );

        OpenTicketResult result = openTicketHandler.execute(command);
        OpenTicketResponse response = new OpenTicketResponse(result.ticketId(), result.ticketNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Support ticket opened successfully", response, null));
    }

    @PostMapping("/tickets/{ticketId}/messages")
    @Operation(summary = "Add a message to a support ticket thread", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AddTicketMessageResult>> addMessage(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody AddTicketMessageRequest request
    ) {
        AuthorType authorType = request.isInternalNote() ? AuthorType.AGENT : AuthorType.CUSTOMER;
        AddTicketMessageCommand command = new AddTicketMessageCommand(
                ticketId,
                principal.userId(),
                authorType,
                request.message(),
                request.isInternalNote(),
                request.attachmentUrls(),
                UUID.randomUUID().toString()
        );

        AddTicketMessageResult result = addTicketMessageHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Message added successfully", result, null));
    }

    @PostMapping("/tickets/{ticketId}/assign")
    @Operation(summary = "Assign ticket to support agent", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AssignTicketResult>> assignTicket(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody AssignTicketRequest request
    ) {
        AssignTicketCommand command = new AssignTicketCommand(
                ticketId,
                request.agentId(),
                UUID.randomUUID().toString()
        );

        AssignTicketResult result = assignTicketHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket assigned successfully", result, null));
    }

    @PostMapping("/tickets/{ticketId}/start")
    @Operation(summary = "Start progress on assigned ticket", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StartTicketProgressResult>> startProgress(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("ticketId") Long ticketId
    ) {
        StartTicketProgressCommand command = new StartTicketProgressCommand(ticketId, principal.userId());
        StartTicketProgressResult result = startTicketProgressHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket marked in progress", result, null));
    }

    @PostMapping("/tickets/{ticketId}/resolve")
    @Operation(summary = "Resolve a support ticket", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ResolveTicketResult>> resolveTicket(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody ResolveTicketRequest request
    ) {
        ResolveTicketCommand command = new ResolveTicketCommand(
                ticketId,
                principal.userId(),
                request.resolutionNote(),
                UUID.randomUUID().toString()
        );

        ResolveTicketResult result = resolveTicketHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket resolved successfully", result, null));
    }

    @PostMapping("/tickets/{ticketId}/close")
    @Operation(summary = "Close a support ticket", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CloseTicketResult>> closeTicket(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("ticketId") Long ticketId
    ) {
        CloseTicketCommand command = new CloseTicketCommand(
                ticketId,
                principal.userId(),
                UUID.randomUUID().toString()
        );

        CloseTicketResult result = closeTicketHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket closed successfully", result, null));
    }

    @PostMapping("/tickets/{ticketId}/reopen")
    @Operation(summary = "Reopen a closed or resolved ticket", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReopenTicketResult>> reopenTicket(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody ReopenTicketRequest request
    ) {
        ReopenTicketCommand command = new ReopenTicketCommand(
                ticketId,
                principal.userId(),
                request.reason(),
                UUID.randomUUID().toString()
        );

        ReopenTicketResult result = reopenTicketHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket reopened successfully", result, null));
    }

    @PostMapping("/tickets/{ticketId}/escalate")
    @Operation(summary = "Escalate ticket to senior agent", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<EscalateTicketResult>> escalateTicket(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody EscalateTicketRequest request
    ) {
        EscalateTicketCommand command = new EscalateTicketCommand(
                ticketId,
                request.newAgentId(),
                request.escalationReason(),
                UUID.randomUUID().toString()
        );

        EscalateTicketResult result = escalateTicketHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket escalated successfully", result, null));
    }

    @GetMapping("/tickets")
    @Operation(summary = "List organization's support tickets", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<TicketSummaryResponse>>> listMyTickets(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(name = "status", required = false) TicketStatus status
    ) {
        List<TicketSummaryResult> results = listMyTicketsHandler.execute(
                new ListMyTicketsQuery(principal.activeOrganizationId(), status));

        List<TicketSummaryResponse> responses = results.stream()
                .map(this::toSummaryResponse)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Tickets retrieved successfully", responses, null));
    }

    @GetMapping("/tickets/details")
    @Operation(summary = "Get full ticket details with threaded messages", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TicketDetailsResponse>> getTicketDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam("id") Long ticketId,
            @RequestParam(name = "isAgent", defaultValue = "false") boolean isAgent
    ) {
        TicketDetailsResult result = getTicketDetailsHandler.execute(
                new GetTicketDetailsQuery(ticketId, principal.activeOrganizationId(), isAgent));

        List<TicketMessageResponse> messageResponses = result.messages().stream()
                .map(m -> new TicketMessageResponse(
                        m.id(),
                        m.ticketId(),
                        m.authorId(),
                        m.authorType(),
                        m.message(),
                        m.isInternalNote(),
                        m.attachmentUrls(),
                        m.createdAt()
                ))
                .toList();

        TicketDetailsResponse response = new TicketDetailsResponse(
                result.id(),
                result.ticketNumber(),
                result.organizationId(),
                result.raisedBy(),
                result.category(),
                result.priority(),
                result.subject(),
                result.status(),
                result.assignedTo(),
                result.slaBreached(),
                result.relatedEntityType(),
                result.relatedEntityId(),
                result.firstResponseDeadline(),
                result.resolutionDeadline(),
                result.firstRespondedAt(),
                result.resolvedAt(),
                result.resolutionNote(),
                result.closedAt(),
                result.closedBy(),
                result.createdAt(),
                result.updatedAt(),
                messageResponses
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket details retrieved successfully", response, null));
    }

    @GetMapping("/agent/tickets")
    @Operation(summary = "List tickets assigned to agent", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<TicketSummaryResponse>>> listAgentTickets(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(name = "status", required = false) TicketStatus status
    ) {
        List<TicketSummaryResult> results = listAgentTicketsHandler.execute(
                new ListAgentTicketsQuery(principal.userId(), status));

        List<TicketSummaryResponse> responses = results.stream()
                .map(this::toSummaryResponse)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Agent tickets retrieved successfully", responses, null));
    }

    @GetMapping("/admin/tickets")
    @Operation(summary = "Search all tickets with operational filters", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<TicketSummaryResponse>>> listAllTickets(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(name = "status", required = false) TicketStatus status,
            @RequestParam(name = "category", required = false) TicketCategory category,
            @RequestParam(name = "priority", required = false) TicketPriority priority,
            @RequestParam(name = "slaBreached", required = false) Boolean slaBreached
    ) {
        List<TicketSummaryResult> results = listAllTicketsHandler.execute(
                new ListAllTicketsQuery(status, category, priority, slaBreached));

        List<TicketSummaryResponse> responses = results.stream()
                .map(this::toSummaryResponse)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "All tickets retrieved successfully", responses, null));
    }

    @GetMapping("/admin/sla-metrics")
    @Operation(summary = "Get SLA performance and compliance metrics", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SlaMetricsResponse>> getSlaMetrics(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam("dateFrom") ZonedDateTime dateFrom,
            @RequestParam("dateTo") ZonedDateTime dateTo
    ) {
        SlaMetricsResult result = getSlaMetricsHandler.execute(new GetSlaMetricsQuery(dateFrom, dateTo));

        List<SlaCategoryBreakdownResponse> categoryResponses = result.byCategory().stream()
                .map(c -> new SlaCategoryBreakdownResponse(c.category(), c.total(), c.breached(), c.complianceRate()))
                .toList();

        SlaMetricsResponse response = new SlaMetricsResponse(
                result.totalTickets(),
                result.firstResponseSlaRate(),
                result.resolutionSlaRate(),
                result.avgFirstResponseMinutes(),
                result.avgResolutionMinutes(),
                result.breachedCount(),
                categoryResponses
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "SLA metrics retrieved successfully", response, null));
    }

    private TicketSummaryResponse toSummaryResponse(TicketSummaryResult r) {
        return new TicketSummaryResponse(
                r.id(),
                r.ticketNumber(),
                r.organizationId(),
                r.raisedBy(),
                r.category(),
                r.priority(),
                r.subject(),
                r.status(),
                r.assignedTo(),
                r.slaBreached(),
                r.firstResponseDeadline(),
                r.resolutionDeadline(),
                r.createdAt(),
                r.updatedAt()
        );
    }
}
