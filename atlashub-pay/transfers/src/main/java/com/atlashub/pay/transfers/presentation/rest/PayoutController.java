package com.atlashub.pay.transfers.presentation.rest;

import com.atlashub.pay.transfers.application.commands.ApprovePayout.ApprovePayoutCommand;
import com.atlashub.pay.transfers.application.commands.ApprovePayout.ApprovePayoutHandler;
import com.atlashub.pay.transfers.application.commands.InitiateBulkPayout.InitiateBulkPayoutCommand;
import com.atlashub.pay.transfers.application.commands.InitiateBulkPayout.InitiateBulkPayoutHandler;
import com.atlashub.pay.transfers.application.commands.InitiateBulkPayout.InitiateBulkPayoutResponse;
import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutCommand;
import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutHandler;
import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutResponse;
import com.atlashub.pay.transfers.application.queries.GetPayout.GetPayoutHandler;
import com.atlashub.pay.transfers.application.queries.GetPayout.GetPayoutQuery;
import com.atlashub.pay.transfers.application.queries.GetPayout.PayoutResult;
import com.atlashub.pay.transfers.application.queries.ListPayouts.ListPayoutsHandler;
import com.atlashub.pay.transfers.application.queries.ListPayouts.ListPayoutsQuery;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.presentation.dto.InitiateBulkPayoutRequest;
import com.atlashub.pay.transfers.presentation.dto.InitiatePayoutRequest;
import com.atlashub.pay.transfers.presentation.dto.PayoutResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pay/transfers")
@Tag(name = "Transfers")
public class PayoutController {

    private final InitiatePayoutHandler initiatePayoutHandler;
    private final InitiateBulkPayoutHandler initiateBulkPayoutHandler;
    private final ApprovePayoutHandler approvePayoutHandler;
    private final GetPayoutHandler getPayoutHandler;
    private final ListPayoutsHandler listPayoutsHandler;

    public PayoutController(
            InitiatePayoutHandler initiatePayoutHandler,
            InitiateBulkPayoutHandler initiateBulkPayoutHandler,
            ApprovePayoutHandler approvePayoutHandler,
            GetPayoutHandler getPayoutHandler,
            ListPayoutsHandler listPayoutsHandler
    ) {
        this.initiatePayoutHandler = initiatePayoutHandler;
        this.initiateBulkPayoutHandler = initiateBulkPayoutHandler;
        this.approvePayoutHandler = approvePayoutHandler;
        this.getPayoutHandler = getPayoutHandler;
        this.listPayoutsHandler = listPayoutsHandler;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('pay:transfers:initiate')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Initiate a single outbound transfer")
    public ResponseEntity<ApiResponse<InitiatePayoutResponse>> initiatePayout(
            @Valid @RequestBody InitiatePayoutRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long orgId = principal.activeOrganizationId();
        Long userId = principal.userId();

        InitiatePayoutResponse response = initiatePayoutHandler.execute(new InitiatePayoutCommand(
                orgId,
                userId,
                request.amount(),
                request.currency(),
                request.recipientBankCode(),
                request.recipientAccountNumber(),
                request.recipientAccountName(),
                request.narration(),
                request.reference(),
                request.provider(),
                request.sourceSystem(),
                request.sourceReferenceId()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Payout initiated successfully", response, null));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('pay:transfers:initiate')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Initiate a batch of outbound transfers")
    public ResponseEntity<ApiResponse<InitiateBulkPayoutResponse>> initiateBulkPayout(
            @Valid @RequestBody InitiateBulkPayoutRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long orgId = principal.activeOrganizationId();
        Long userId = principal.userId();

        List<InitiateBulkPayoutCommand.BulkPayoutItem> items = request.items().stream()
                .map(i -> new InitiateBulkPayoutCommand.BulkPayoutItem(
                        i.recipientBankCode(),
                        i.recipientAccountNumber(),
                        i.recipientAccountName(),
                        i.amount(),
                        i.currency(),
                        i.narration(),
                        i.itemReference()
                ))
                .collect(Collectors.toList());

        InitiateBulkPayoutResponse response = initiateBulkPayoutHandler.execute(new InitiateBulkPayoutCommand(
                orgId,
                userId,
                request.bulkReference(),
                request.sourceSystem(),
                request.sourceReferenceId(),
                items
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Bulk payout initiated successfully", response, null));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('pay:transfers:approve')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve a pending payout (Maker-Checker)")
    public ResponseEntity<ApiResponse<Void>> approvePayout(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        approvePayoutHandler.execute(new ApprovePayoutCommand(id, principal.userId()));

        return ResponseEntity.ok(new ApiResponse<>(true, "Payout approved successfully", null, null));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('pay:transfers:read')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List payouts with pagination and filters")
    public ResponseEntity<ApiResponse<PageResult<PayoutResponse>>> listPayouts(
            @RequestParam(required = false) PayoutStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long orgId = principal.activeOrganizationId();
        PageResult<PayoutResult> result = listPayoutsHandler.execute(new ListPayoutsQuery(orgId, status, page, size));

        List<PayoutResponse> responses = result.content().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PageResult<PayoutResponse> pageResponse = new PageResult<>(
                responses,
                result.pageNumber(),
                result.pageSize(),
                result.totalElements(),
                result.totalPages()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Payouts retrieved successfully", pageResponse, null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('pay:transfers:read')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payout details by ID")
    public ResponseEntity<ApiResponse<PayoutResponse>> getPayout(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long orgId = principal.activeOrganizationId();
        PayoutResult result = getPayoutHandler.execute(new GetPayoutQuery(orgId, id));

        return ResponseEntity.ok(new ApiResponse<>(true, "Payout retrieved successfully", toResponse(result), null));
    }

    private PayoutResponse toResponse(PayoutResult r) {
        return new PayoutResponse(
                r.id(),
                r.organizationId(),
                r.amount(),
                r.currency(),
                r.recipientBankCode(),
                r.recipientAccountNumber(),
                r.recipientAccountName(),
                r.narration(),
                r.status(),
                r.reference(),
                r.provider(),
                r.providerReference(),
                r.sourceSystem(),
                r.sourceReferenceId(),
                r.initiatedBy(),
                r.approvedBy(),
                r.approvedAt(),
                r.failureReason(),
                r.createdAt(),
                r.completedAt()
        );
    }
}
