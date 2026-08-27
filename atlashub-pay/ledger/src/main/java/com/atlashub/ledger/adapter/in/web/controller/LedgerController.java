package com.atlashub.ledger.adapter.in.web.controller;

import com.atlashub.ledger.application.result.BalanceDto;
import com.atlashub.ledger.application.result.LedgerHistoryDto;
import com.atlashub.ledger.application.query.GetAccountBalanceQuery;
import com.atlashub.ledger.application.query.GetLedgerHistoryQuery;
import com.atlashub.ledger.application.usecase.GetAccountBalanceUseCase;
import com.atlashub.ledger.application.usecase.GetLedgerHistoryUseCase;
import com.atlashub.shared.dto.ApiResponse;
import com.atlashub.shared.money.Money;
import com.atlashub.shared.port.out.AccountQueryPort;
import com.atlashub.shared.port.out.AccountDetailsDto;
import com.atlashub.shared.util.PageResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@io.swagger.v3.oas.annotations.tags.Tag(name = "Ledger", description = "Core ledger balances and entries")
@RequestMapping("/api/v1/balance")
public class LedgerController {

    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final GetLedgerHistoryUseCase getLedgerHistoryUseCase;
    private final AccountQueryPort accountQueryPort;

    public LedgerController(GetAccountBalanceUseCase getAccountBalanceUseCase, GetLedgerHistoryUseCase getLedgerHistoryUseCase, AccountQueryPort accountQueryPort) {
        this.getAccountBalanceUseCase = getAccountBalanceUseCase;
        this.getLedgerHistoryUseCase = getLedgerHistoryUseCase;
        this.accountQueryPort = accountQueryPort;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BalanceDto>>> getBalances(@RequestHeader("X-Organization-Id") String integrationStr) {
        Long integration = Long.valueOf(integrationStr);
        List<AccountDetailsDto> accounts = accountQueryPort.findAccountsByIntegration(integration);
        
        List<BalanceDto> balances = accounts.stream().map(acc -> {
            Money balance = getAccountBalanceUseCase.execute(new GetAccountBalanceQuery(acc.accountId(), integration));
            return new BalanceDto(balance.currency().name(), balance.amount());
        }).toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Balances retrieved", balances, null));
    }

    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<List<LedgerHistoryDto>>> getLedgerHistory(
            @RequestHeader("X-Organization-Id") String integrationStr,
            @org.springframework.web.bind.annotation.ModelAttribute com.atlashub.ledger.adapter.in.web.request.GetLedgerHistoryRequestDto request) {
        
        Long integration = Long.valueOf(integrationStr);
        int page = request.page();
        int perPage = request.perPage();
        PageResult<LedgerHistoryDto> result = getLedgerHistoryUseCase.execute(new GetLedgerHistoryQuery(integration, page, perPage));
        
        ApiResponse.Meta meta = new ApiResponse.Meta(
                result.totalElements(),
                (page - 1) * perPage,
                result.pageSize(),
                result.pageNumber(),
                result.totalPages()
        );
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Balance ledger retrieved", result.content(), meta));
    }
}

