package com.atlashub.pay.ledger.application.queries.GetPartyLedgerHistory;

import com.atlashub.pay.ledger.application.queries.GetLedgerHistory.LedgerEntryResult;
import com.atlashub.pay.ledger.application.queries.GetLedgerHistory.LedgerTransactionResult;
import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerPartyType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class GetPartyLedgerHistoryHandler
        extends Query<GetPartyLedgerHistoryQuery, PageResult<LedgerTransactionResult>> {
    private final LedgerAccountRepository accountRepository;
    private final LedgerTransactionRepository transactionRepository;

    public GetPartyLedgerHistoryHandler(LedgerAccountRepository accountRepository,
                                        LedgerTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:read')")
    public PageResult<LedgerTransactionResult> execute(GetPartyLedgerHistoryQuery query) {
        ApiEnvironment environment = ApiEnvironment.parse(query.environment());
        LedgerPartyType partyType = LedgerPartyType.valueOf(query.partyType().toUpperCase());
        LedgerAccountType accountType = partyType == LedgerPartyType.CUSTOMER
                ? LedgerAccountType.CUSTOMER_FUNDS : LedgerAccountType.VENDOR_PAYABLE;
        Long accountId = accountRepository.findByOrganizationIdAndEnvironmentAndParty(
                        query.organizationId(), environment, partyType, query.partyReferenceId(), accountType,
                        CurrencyCode.valueOf(query.currency().toUpperCase()))
                .orElseThrow(() -> new LedgerAccountNotFoundException("Party ledger account not found")).getId();
        var page = transactionRepository.findHistory(query.organizationId(), environment, accountId,
                query.dateFrom(), query.dateTo(), PageRequest.of(query.page(), query.size()));
        return new PageResult<>(page.getContent().stream().map(this::result).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private LedgerTransactionResult result(LedgerTransaction transaction) {
        return new LedgerTransactionResult(transaction.getId(), transaction.getReference(),
                transaction.getSourceSystem(), transaction.getSourceReferenceId(),
                transaction.getDescription(), transaction.getCurrency().name(), transaction.getPostedAt(),
                transaction.getEntries().stream().map(entry -> new LedgerEntryResult(
                        entry.getAccountId(), entry.getType(), entry.getAmount().amount())).toList());
    }
}
