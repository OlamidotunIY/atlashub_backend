package com.atlashub.pay.txquery.presentation.rest;

import com.atlashub.pay.txquery.application.queries.ListTransactions.ListTransactionsHandler;
import com.atlashub.pay.txquery.application.queries.ListTransactions.ListTransactionsQuery;
import com.atlashub.pay.txquery.presentation.dto.TransactionResponse;
import com.atlashub.pay.txquery.presentation.dto.TransactionResponseMapper;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pay/customers/{customerId}/transactions")
public class CustomerTransactionController {
    private final ListTransactionsHandler handler;
    private final TransactionResponseMapper mapper;

    public CustomerTransactionController(ListTransactionsHandler handler, TransactionResponseMapper mapper) {
        this.handler = handler;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<TransactionResponse>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = handler.execute(new ListTransactionsQuery(principal.activeOrganizationId(),
                principal.apiEnvironment(), null, null, null, null, null, null, null, null, "CUSTOMER", customerId,
                null, null, null, null, null, null, null, null, page, size, "desc"));
        var response = new PageResult<>(result.content().stream().map(mapper::map).toList(), result.pageNumber(),
                result.pageSize(), result.totalElements(), result.totalPages());
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", response, null));
    }
}
