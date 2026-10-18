package com.atlashub.pay.controls.presentation.rest;

import com.atlashub.pay.controls.application.commands.ResolveControlCase.ResolveControlCaseCommand;
import com.atlashub.pay.controls.application.commands.ResolveControlCase.ResolveControlCaseHandler;
import com.atlashub.pay.controls.application.commands.SubmitControlEvidence.SubmitControlEvidenceCommand;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/pay/controls")
@Tag(name = "Payment Controls", description = "Financing policy enforcement and payment control decisions")
public class PaymentControlController {

    private final GetActiveControlledFundsPolicyHandler getPolicyHandler;
    private final ListControlDecisionsHandler listDecisionsHandler;
    private final ListControlCasesHandler listCasesHandler;
    private final SubmitControlEvidenceHandler submitEvidenceHandler;
    private final ResolveControlCaseHandler resolveCaseHandler;

    public PaymentControlController(
            GetActiveControlledFundsPolicyHandler getPolicyHandler,
            ListControlDecisionsHandler listDecisionsHandler,
            ListControlCasesHandler listCasesHandler,
            SubmitControlEvidenceHandler submitEvidenceHandler,
            ResolveControlCaseHandler resolveCaseHandler
    ) {
        this.getPolicyHandler = getPolicyHandler;
        this.listDecisionsHandler = listDecisionsHandler;
        this.listCasesHandler = listCasesHandler;
        this.submitEvidenceHandler = submitEvidenceHandler;
        this.resolveCaseHandler = resolveCaseHandler;
    }

    @GetMapping("/policies")
    @PreAuthorize("hasAuthority('financing:facilities:read')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get active controlled funds policy for a facility")
    public ResponseEntity<ApiResponse<ControlledFundsPolicyResponse>> getActivePolicy(
            @RequestParam Long facilityId
    ) {
        ControlledFundsPolicyResult result = getPolicyHandler.execute(new GetActiveControlledFundsPolicyQuery(facilityId));
        ControlledFundsPolicyResponse response = new ControlledFundsPolicyResponse(
                result.id(),
                result.facilityId(),
                result.organizationId(),
                result.environment(),
                result.policyVersion(),
                result.mode(),
                result.permittedCategories(),
                result.restrictedCategories(),
                result.status(),
                result.effectiveFrom(),
                result.effectiveUntil(),
                result.activatedBy()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Active policy retrieved successfully", response, null));
    }

    @GetMapping("/decisions")
    @PreAuthorize("hasAuthority('financing:monitoring:read')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List payment control decisions for organization")
    public ResponseEntity<ApiResponse<PageResult<PaymentControlDecisionResponse>>> listDecisions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        PageResult<PaymentControlDecisionResult> result = listDecisionsHandler.execute(
                new ListControlDecisionsQuery(principal.activeOrganizationId(), page, size)
        );

        List<PaymentControlDecisionResponse> content = result.content().stream()
                .map(d -> new PaymentControlDecisionResponse(
                        d.decisionId(),
                        d.policyId(),
                        d.facilityId(),
                        d.organizationId(),
                        d.paymentInstructionReference(),
                        d.paymentRail(),
                        d.paymentContext(),
                        d.evaluatedAction(),
                        d.enforcedAction(),
                        d.reasonCodes(),
                        d.riskSignalId(),
                        d.providerControlReference(),
                        d.status(),
                        d.evaluatedAt(),
                        d.appliedAt()
                ))
                .collect(Collectors.toList());

        PageResult<PaymentControlDecisionResponse> pageResponse = new PageResult<>(
                content,
                result.pageNumber(),
                result.pageSize(),
                result.totalElements(),
                result.totalPages()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Decisions retrieved successfully", pageResponse, null));
    }

    @GetMapping("/cases")
    @PreAuthorize("hasAuthority('financing:monitoring:read')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List control review cases for organization")
    public ResponseEntity<ApiResponse<PageResult<ControlCaseResponse>>> listCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        PageResult<ControlCaseResult> result = listCasesHandler.execute(
                new ListControlCasesQuery(principal.activeOrganizationId(), page, size)
        );

        List<ControlCaseResponse> content = result.content().stream()
                .map(c -> new ControlCaseResponse(
                        c.caseId(),
                        c.decisionId(),
                        c.organizationId(),
                        c.status(),
                        c.submittedEvidenceRefs(),
                        c.openedAt(),
                        c.resolvedBy(),
                        c.resolvedAt(),
                        c.resolutionReason()
                ))
                .collect(Collectors.toList());

        PageResult<ControlCaseResponse> pageResponse = new PageResult<>(
                content,
                result.pageNumber(),
                result.pageSize(),
                result.totalElements(),
                result.totalPages()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Control cases retrieved successfully", pageResponse, null));
    }

    @PostMapping("/cases/{id}/evidence")
    @PreAuthorize("hasAuthority('financing:controls:respond')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit evidence for a control case")
    public ResponseEntity<ApiResponse<Void>> submitEvidence(
            @PathVariable Long id,
            @Valid @RequestBody SubmitControlEvidenceRequest request
    ) {
        submitEvidenceHandler.execute(new SubmitControlEvidenceCommand(
                id,
                request.evidenceReference(),
                request.description()
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Evidence submitted successfully", null, null));
    }

    @PostMapping("/cases/{id}/resolve")
    @PreAuthorize("hasAuthority('financing:controls:review')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Review and resolve a control case")
    public ResponseEntity<ApiResponse<Void>> resolveCase(
            @PathVariable Long id,
            @Valid @RequestBody ResolveControlCaseRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        resolveCaseHandler.execute(new ResolveControlCaseCommand(
                id,
                principal.userId(),
                request.approve(),
                request.reason()
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Control case resolved successfully", null, null));
    }
}
