package com.atlashub.pay.ledger.presentation.rest;

import com.atlashub.pay.ledger.application.queries.GetLedgerHistory.GetLedgerHistoryHandler;
import com.atlashub.pay.ledger.application.queries.GetLedgerHistory.GetLedgerHistoryQuery;
import com.atlashub.pay.ledger.application.queries.GetLedgerHistory.LedgerTransactionResult;
import com.atlashub.pay.ledger.presentation.dto.LedgerEntryResponse;
import com.atlashub.pay.ledger.presentation.dto.LedgerTransactionResponse;
import com.atlashub.pay.ledger.application.queries.GetPartyLedgerHistory.GetPartyLedgerHistoryHandler;
import com.atlashub.pay.ledger.application.queries.GetPartyLedgerHistory.GetPartyLedgerHistoryQuery;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/ledger-entries")
@Tag(name = "Ledger Transactions", description = "Immutable payment ledger transaction history")
public class LedgerTransactionController {
    private final GetLedgerHistoryHandler historyHandler;
    private final GetPartyLedgerHistoryHandler partyHistoryHandler;

    public LedgerTransactionController(GetLedgerHistoryHandler historyHandler,
                                       GetPartyLedgerHistoryHandler partyHistoryHandler) {
        this.historyHandler = historyHandler;
        this.partyHistoryHandler = partyHistoryHandler;
    }

    @GetMapping(params = {"partyType", "partyReferenceId"})
    @Operation(summary = "Get party ledger history")
    public ResponseEntity<ApiResponse<PageResult<LedgerTransactionResponse>>> partyHistory(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam String partyType,
            @RequestParam String partyReferenceId,
            @RequestParam(defaultValue = "NGN") String currency,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<LedgerTransactionResult> result = partyHistoryHandler.execute(new GetPartyLedgerHistoryQuery(
                principal.activeOrganizationId(), principal.environment(), partyType, partyReferenceId,
                currency, dateFrom, dateTo, page, size));
        return ok(new PageResult<>(result.content().stream().map(this::response).toList(),
                result.pageNumber(), result.pageSize(), result.totalElements(), result.totalPages()));
    }

    @GetMapping
    @Operation(summary = "Get organization ledger history")
    public ResponseEntity<ApiResponse<PageResult<LedgerTransactionResponse>>> history(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<LedgerTransactionResult> result = historyHandler.execute(new GetLedgerHistoryQuery(
                principal.activeOrganizationId(), principal.environment(), accountId,
                dateFrom, dateTo, page, size));
        return ok(new PageResult<>(result.content().stream().map(this::response).toList(),
                result.pageNumber(), result.pageSize(), result.totalElements(), result.totalPages()));
    }

    private LedgerTransactionResponse response(LedgerTransactionResult result) {
        return new LedgerTransactionResponse(result.transactionId(), result.reference(), result.sourceSystem(),
                result.sourceReferenceId(), result.description(), result.currency(), result.postedAt(),
                result.entries().stream().map(entry -> new LedgerEntryResponse(
                        entry.accountId(), entry.entryType(), entry.amount())).toList());
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }
}
