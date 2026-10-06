package com.atlashub.pay.ledger.application.queries.GetLedgerHistory;

import com.atlashub.pay.ledger.domain.entities.LedgerEntry;
import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetLedgerHistoryHandler extends Query<GetLedgerHistoryQuery, PageResult<LedgerTransactionResult>> {

    private static final Logger log = LoggerFactory.getLogger(GetLedgerHistoryHandler.class);

    private final LedgerTransactionRepository ledgerTransactionRepository;

    public GetLedgerHistoryHandler(LedgerTransactionRepository ledgerTransactionRepository) {
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:read')")
    public PageResult<LedgerTransactionResult> execute(GetLedgerHistoryQuery query) {
        log.info("Executing GetLedgerHistoryQuery for organization {} and account {}", 
                query.organizationId(), query.accountId());

        Pageable pageable = PageRequest.of(query.page(), query.size());
        
        Page<LedgerTransaction> transactionPage = ledgerTransactionRepository.findHistory(
                query.organizationId(),
                ApiEnvironment.parse(query.environment()),
                query.accountId(), 
                query.dateFrom(), 
                query.dateTo(), 
                pageable
        );

        List<LedgerTransactionResult> results = transactionPage.getContent().stream()
                .map(this::mapToResult)
                .collect(Collectors.toList());

        return new PageResult<>(
                results,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
    }

    private LedgerTransactionResult mapToResult(LedgerTransaction transaction) {
        List<LedgerEntryResult> entryResults = transaction.getEntries().stream()
                .map(this::mapEntry)
                .collect(Collectors.toList());

        return new LedgerTransactionResult(
                transaction.getId(),
                transaction.getReference(),
                transaction.getSourceSystem(),
                transaction.getSourceReferenceId(),
                transaction.getDescription(),
                transaction.getCurrency().name(),
                transaction.getPostedAt(),
                entryResults
        );
    }

    private LedgerEntryResult mapEntry(LedgerEntry entry) {
        return new LedgerEntryResult(
                entry.getAccountId(),
                entry.getType(),
                entry.getAmount().amount()
        );
    }
}
