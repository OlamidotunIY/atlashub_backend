package com.atlashub.pay.ledger.presentation.rest;

import com.atlashub.pay.ledger.application.queries.GetLedgerHistory.GetLedgerHistoryHandler;
import com.atlashub.pay.ledger.application.queries.GetLedgerHistory.GetLedgerHistoryQuery;
import com.atlashub.pay.ledger.application.queries.GetLedgerHistory.LedgerTransactionResult;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/pay/ledger/history")
public class LedgerTransactionController {
    private final GetLedgerHistoryHandler historyHandler;

    public LedgerTransactionController(GetLedgerHistoryHandler historyHandler) {
        this.historyHandler = historyHandler;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<LedgerTransactionResult>>> history(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ok(historyHandler.execute(new GetLedgerHistoryQuery(
                principal.activeOrganizationId(), accountId, dateFrom, dateTo, page, size)));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }
}
