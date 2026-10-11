package com.atlashub.accounting.cash.application.query.ListBankReconciliations;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListBankReconciliationsHandler extends Query<ListBankReconciliationsQuery, List<BankReconciliationSummaryResult>> {

    private final BankReconciliationRepository bankReconciliationRepository;

    public ListBankReconciliationsHandler(BankReconciliationRepository bankReconciliationRepository) {
        this.bankReconciliationRepository = bankReconciliationRepository;
    }

    @Override
    public List<BankReconciliationSummaryResult> execute(ListBankReconciliationsQuery query) {
        List<BankReconciliation> reconciliations;
        if (query.status() != null) {
            reconciliations = bankReconciliationRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status());
        } else {
            reconciliations = bankReconciliationRepository.findByOrganizationId(query.organizationId());
        }

        return reconciliations.stream()
                .map(r -> new BankReconciliationSummaryResult(
                        r.getId(),
                        r.getOrganizationId(),
                        r.getBankAccountCode(),
                        r.getStatementDate(),
                        r.getStatementClosingBalance(),
                        r.getBookBalance(),
                        r.getDifference(),
                        r.getStatus(),
                        r.getMatchedItems().size(),
                        r.getUnmatchedBankItems().size(),
                        r.getUnmatchedBookItems().size(),
                        r.getCreatedAt(),
                        r.getReconciledAt()
                ))
                .toList();
    }
}
