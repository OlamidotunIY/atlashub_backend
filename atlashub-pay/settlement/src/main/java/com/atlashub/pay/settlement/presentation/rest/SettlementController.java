package com.atlashub.pay.settlement.presentation.rest;

import com.atlashub.pay.settlement.application.commands.DisputeSettlement.DisputeSettlementCommand;
import com.atlashub.pay.settlement.application.commands.DisputeSettlement.DisputeSettlementHandler;
import com.atlashub.pay.settlement.application.commands.ResolveSettlementDispute.ResolveSettlementDisputeCommand;
import com.atlashub.pay.settlement.application.commands.ResolveSettlementDispute.ResolveSettlementDisputeHandler;
import com.atlashub.pay.settlement.application.commands.RetrySettlementReconciliation.RetrySettlementReconciliationCommand;
import com.atlashub.pay.settlement.application.commands.RetrySettlementReconciliation.RetrySettlementReconciliationHandler;
import com.atlashub.pay.settlement.application.queries.GetSettlementDetails.GetSettlementDetailsHandler;
import com.atlashub.pay.settlement.application.queries.GetSettlementDetails.GetSettlementDetailsQuery;
import com.atlashub.pay.settlement.application.queries.GetSettlementDetails.SettlementResult;
import com.atlashub.pay.settlement.application.queries.ListSettlements.ListSettlementsHandler;
import com.atlashub.pay.settlement.application.queries.ListSettlements.ListSettlementsQuery;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.pay.settlement.presentation.dto.DisputeSettlementRequest;
import com.atlashub.pay.settlement.presentation.dto.SettlementResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZonedDateTime;

@RestController @RequestMapping("/api/v1/pay/settlements")
public class SettlementController{
    private final ListSettlementsHandler listHandler;private final GetSettlementDetailsHandler getHandler;
    private final DisputeSettlementHandler disputeHandler;private final RetrySettlementReconciliationHandler retryHandler;
    private final ResolveSettlementDisputeHandler resolveHandler;
    public SettlementController(ListSettlementsHandler listHandler,GetSettlementDetailsHandler getHandler,
            DisputeSettlementHandler disputeHandler,RetrySettlementReconciliationHandler retryHandler,
            ResolveSettlementDisputeHandler resolveHandler){this.listHandler=listHandler;this.getHandler=getHandler;
        this.disputeHandler=disputeHandler;this.retryHandler=retryHandler;this.resolveHandler=resolveHandler;}
    @GetMapping public ResponseEntity<ApiResponse<PageResult<SettlementResponse>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,@RequestParam(required=false) SettlementStatus status,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        var result=listHandler.execute(new ListSettlementsQuery(principal.activeOrganizationId(),principal.apiEnvironment(),status,from,to,page,size));
        var mapped=new PageResult<>(result.content().stream().map(this::response).toList(),result.pageNumber(),
                result.pageSize(),result.totalElements(),result.totalPages());
        return ResponseEntity.ok(new ApiResponse<>(true,"Success",mapped,null));}
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<SettlementResponse>> get(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,@PathVariable Long id){var result=getHandler.execute(
            new GetSettlementDetailsQuery(id,principal.activeOrganizationId(),principal.apiEnvironment()));
        return ResponseEntity.ok(new ApiResponse<>(true,"Success",response(result),null));}
    @PostMapping("/{id}/dispute") public ResponseEntity<ApiResponse<Void>> dispute(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,@PathVariable Long id,
            @Valid @RequestBody DisputeSettlementRequest request){disputeHandler.execute(new DisputeSettlementCommand(
            id,principal.activeOrganizationId(),principal.apiEnvironment(),request.reason()));
        return ResponseEntity.ok(new ApiResponse<>(true,"Settlement disputed",null,null));}
    @PostMapping("/{id}/retry-reconciliation") public ResponseEntity<ApiResponse<Void>> retry(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,@PathVariable Long id){retryHandler.execute(
            new RetrySettlementReconciliationCommand(id,principal.activeOrganizationId(),principal.apiEnvironment()));
        return ResponseEntity.ok(new ApiResponse<>(true,"Reconciliation queued",null,null));}
    @PostMapping("/{id}/resolve-dispute") public ResponseEntity<ApiResponse<Void>> resolve(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,@PathVariable Long id){resolveHandler.execute(
            new ResolveSettlementDisputeCommand(id,principal.activeOrganizationId(),principal.apiEnvironment()));
        return ResponseEntity.ok(new ApiResponse<>(true,"Dispute resolved",null,null));}
    private SettlementResponse response(SettlementResult r){return new SettlementResponse(r.id(),r.provider().name(),
            r.providerSettlementId(),r.amount().amount(),r.amount().currency().name(),r.settledAt(),r.status().name(),r.description());}
}
