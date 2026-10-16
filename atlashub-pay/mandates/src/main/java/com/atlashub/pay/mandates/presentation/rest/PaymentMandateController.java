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
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.pay.mandates.presentation.dto.CreateMandateRequest;
import com.atlashub.pay.mandates.presentation.dto.MandateResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
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
@RequestMapping("/api/v1/pay/mandates")
@Tag(name = "Payment Mandates", description = "Recurring payment authorizations and subscriptions")
public class PaymentMandateController {

    private final CreateMandateHandler createMandateHandler;
    private final PauseMandateHandler pauseMandateHandler;
    private final ResumeMandateHandler resumeMandateHandler;
    private final RevokeMandateHandler revokeMandateHandler;
    private final GetMandateHandler getMandateHandler;
    private final ListMandatesHandler listMandatesHandler;

    public PaymentMandateController(
            CreateMandateHandler createMandateHandler,
            PauseMandateHandler pauseMandateHandler,
            ResumeMandateHandler resumeMandateHandler,
            RevokeMandateHandler revokeMandateHandler,
            GetMandateHandler getMandateHandler,
            ListMandatesHandler listMandatesHandler
    ) {
        this.createMandateHandler = createMandateHandler;
        this.pauseMandateHandler = pauseMandateHandler;
        this.resumeMandateHandler = resumeMandateHandler;
        this.revokeMandateHandler = revokeMandateHandler;
        this.getMandateHandler = getMandateHandler;
        this.listMandatesHandler = listMandatesHandler;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('pay:mandates:create')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a recurring payment mandate")
    public ResponseEntity<ApiResponse<CreateMandateResponse>> createMandate(
            @Valid @RequestBody CreateMandateRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        CreateMandateResponse response = createMandateHandler.execute(new CreateMandateCommand(
                principal.activeOrganizationId(),
                request.customerId(),
                request.email(),
                request.amount(),
                request.currency(),
                request.frequency(),
                request.authorizationCode(),
                request.firstChargeDate()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Payment mandate created successfully", response, null));
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAuthority('pay:mandates:create')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Pause a payment mandate")
    public ResponseEntity<ApiResponse<Void>> pauseMandate(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        pauseMandateHandler.execute(new PauseMandateCommand(id, principal.activeOrganizationId()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment mandate paused successfully", null, null));
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAuthority('pay:mandates:create')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Resume a paused payment mandate")
    public ResponseEntity<ApiResponse<Void>> resumeMandate(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        resumeMandateHandler.execute(new ResumeMandateCommand(id, principal.activeOrganizationId()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment mandate resumed successfully", null, null));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('pay:mandates:revoke')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Permanently revoke a payment mandate")
    public ResponseEntity<ApiResponse<Void>> revokeMandate(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        revokeMandateHandler.execute(new RevokeMandateCommand(id, principal.activeOrganizationId()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment mandate revoked successfully", null, null));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('pay:mandates:read')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List payment mandates with optional customer or status filter")
    public ResponseEntity<ApiResponse<List<MandateResponse>>> listMandates(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) MandateStatus status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        List<MandateResult> results = listMandatesHandler.execute(new ListMandatesQuery(
                principal.activeOrganizationId(),
                customerId,
                status
        ));

        List<MandateResponse> responses = results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Payment mandates retrieved successfully", responses, null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('pay:mandates:read')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payment mandate details by ID")
    public ResponseEntity<ApiResponse<MandateResponse>> getMandate(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        MandateResult result = getMandateHandler.execute(new GetMandateQuery(
                id,
                principal.activeOrganizationId()
        ));

        return ResponseEntity.ok(new ApiResponse<>(true, "Payment mandate retrieved successfully", toResponse(result), null));
    }

    private MandateResponse toResponse(MandateResult r) {
        return new MandateResponse(
                r.id(),
                r.organizationId(),
                r.customerId(),
                r.email(),
                r.amount(),
                r.currency(),
                r.frequency(),
                r.status(),
                r.nextChargeDate(),
                r.createdAt(),
                r.updatedAt()
        );
    }
}
