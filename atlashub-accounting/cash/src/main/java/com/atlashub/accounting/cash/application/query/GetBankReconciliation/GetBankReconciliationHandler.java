package com.atlashub.accounting.cash.application.query.GetBankReconciliation;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.exceptions.BankReconciliationNotFoundException;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetBankReconciliationHandler extends Query<GetBankReconciliationQuery, BankReconciliationResult> {

    private final BankReconciliationRepository bankReconciliationRepository;

    public GetBankReconciliationHandler(BankReconciliationRepository bankReconciliationRepository) {
        this.bankReconciliationRepository = bankReconciliationRepository;
    }

    @Override
    public BankReconciliationResult execute(GetBankReconciliationQuery query) {
        BankReconciliation recon = bankReconciliationRepository.findById(query.reconciliationId())
                .filter(r -> r.getOrganizationId().equals(query.organizationId()))
                .orElseThrow(() -> new BankReconciliationNotFoundException(query.reconciliationId()));

        List<ReconciliationItemResult> matched = recon.getMatchedItems().stream()
                .map(item -> new ReconciliationItemResult(
                        item.getId(),
                        item.getReconciliationId(),
                        item.getBankStatementLineId(),
                        item.getJournalLineId()
                ))
                .toList();

        List<BankStatementLineResult> unmatchedBank = recon.getUnmatchedBankItems().stream()
                .map(line -> new BankStatementLineResult(
                        line.getId(),
                        line.getReconciliationId(),
                        line.getTransactionDate(),
                        line.getDescription(),
                        line.getAmount(),
                        line.getType()
                ))
                .toList();

        return new BankReconciliationResult(
                recon.getId(),
                recon.getOrganizationId(),
                recon.getBankAccountCode(),
                recon.getStatementDate(),
                recon.getStatementClosingBalance(),
                recon.getBookBalance(),
                recon.getDifference(),
                recon.getStatus(),
                matched,
                unmatchedBank,
                recon.getUnmatchedBookItems(),
                recon.getCreatedAt(),
                recon.getReconciledAt()
        );
    }
}
