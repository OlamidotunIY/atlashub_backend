package com.atlashub.pay.txquery.presentation.rest;

import com.atlashub.pay.txquery.application.queries.GetTransactionByReference.GetTransactionByReferenceHandler;
import com.atlashub.pay.txquery.application.queries.GetTransactionByReference.GetTransactionByReferenceQuery;
import com.atlashub.pay.txquery.application.queries.GetTransactionDetails.GetTransactionDetailsHandler;
import com.atlashub.pay.txquery.application.queries.GetTransactionDetails.GetTransactionDetailsQuery;
import com.atlashub.pay.txquery.application.queries.GetTransactionVolume.GetTransactionVolumeHandler;
import com.atlashub.pay.txquery.application.queries.GetTransactionVolume.GetTransactionVolumeQuery;
import com.atlashub.pay.txquery.application.queries.ListTransactions.ListTransactionsHandler;
import com.atlashub.pay.txquery.application.queries.ListTransactions.ListTransactionsQuery;
import com.atlashub.pay.txquery.application.queries.TransactionResults.TransactionResult;
import com.atlashub.pay.txquery.domain.valueobject.TransactionDirection;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.pay.txquery.presentation.dto.TransactionResponse;
import com.atlashub.pay.txquery.presentation.dto.TransactionResponseMapper;
import com.atlashub.pay.txquery.presentation.dto.TransactionVolumeResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/v1/pay/transactions")
@Tag(name = "Transactions")
@SecurityRequirement(name = "bearerAuth")
public class FinancialTransactionController {
    private final ListTransactionsHandler listHandler;
    private final GetTransactionDetailsHandler detailsHandler;
    private final GetTransactionByReferenceHandler referenceHandler;
    private final GetTransactionVolumeHandler volumeHandler;
    private final TransactionResponseMapper mapper;

    public FinancialTransactionController(ListTransactionsHandler listHandler,
                                          GetTransactionDetailsHandler detailsHandler,
                                          GetTransactionByReferenceHandler referenceHandler,
                                          GetTransactionVolumeHandler volumeHandler, TransactionResponseMapper mapper) {
        this.listHandler = listHandler;
        this.detailsHandler = detailsHandler;
        this.referenceHandler = referenceHandler;
        this.volumeHandler = volumeHandler;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "List organization transactions")
    public ResponseEntity<ApiResponse<PageResult<TransactionResponse>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionDirection direction,
            @RequestParam(required = false) String channel, @RequestParam(required = false) String provider,
            @RequestParam(required = false) String sourceSystem,
            @RequestParam(required = false) String sourceReferenceId, @RequestParam(required = false) String partyType,
            @RequestParam(required = false) String partyReferenceId, @RequestParam(required = false) Long outletId,
            @RequestParam(required = false) String currency, @RequestParam(required = false) String reference,
            @RequestParam(required = false) String search, @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort) {
        return page(
                execute(principal, null, type, status, direction, channel, provider, sourceSystem, sourceReferenceId,
                        partyType, partyReferenceId, outletId, currency, reference, search, minAmount, maxAmount, from,
                        to, page, size, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> get(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable Long id) {
        return ok(mapper.map(detailsHandler.execute(
                new GetTransactionDetailsQuery(principal.activeOrganizationId(), principal.apiEnvironment(), id))));
    }

    @GetMapping("/by-reference")
    public ResponseEntity<ApiResponse<TransactionResponse>> byReference(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @RequestParam String reference) {
        return ok(mapper.map(referenceHandler.execute(
                new GetTransactionByReferenceQuery(principal.activeOrganizationId(), principal.apiEnvironment(),
                        reference))));
    }

    @GetMapping("/volume")
    public ResponseEntity<ApiResponse<TransactionVolumeResponse>> volume(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @RequestParam YearMonth month) {
        var result = volumeHandler.execute(
                new GetTransactionVolumeQuery(principal.activeOrganizationId(), principal.apiEnvironment(), month));
        return ok(new TransactionVolumeResponse(result.organizationId(), result.month(), result.chargeCount(),
                result.chargeAmount(), result.payoutCount(), result.payoutAmount(), result.currency()));
    }

    PageResult<TransactionResult> execute(AuthenticatedPrincipal principal, Long accountId, TransactionType type,
                                          TransactionStatus status, TransactionDirection direction, String channel,
                                          String provider, String sourceSystem, String sourceReferenceId,
                                          String partyType, String partyReferenceId, Long outletId, String currency,
                                          String reference, String search, BigDecimal minAmount, BigDecimal maxAmount,
                                          ZonedDateTime from, ZonedDateTime to, int page, int size, String sort) {
        return listHandler.execute(
                new ListTransactionsQuery(principal.activeOrganizationId(), principal.apiEnvironment(), accountId, type,
                        status, direction, channel, provider, sourceSystem, sourceReferenceId, partyType,
                        partyReferenceId, outletId, currency, reference, search, minAmount, maxAmount, from, to, page,
                        size, sort));
    }

    private ResponseEntity<ApiResponse<PageResult<TransactionResponse>>> page(PageResult<TransactionResult> result) {
        return ok(new PageResult<>(result.content().stream().map(mapper::map).toList(), result.pageNumber(),
                result.pageSize(), result.totalElements(), result.totalPages()));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", data, null));
    }
}
