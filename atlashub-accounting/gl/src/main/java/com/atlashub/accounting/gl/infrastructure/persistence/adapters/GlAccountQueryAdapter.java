package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.infrastructure.persistence.entities.AccountJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalLineJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataAccountRepository;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataBalanceSnapshotRepository;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataJournalLineRepository;
import com.atlashub.shared.application.port.GlAccountQueryPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class GlAccountQueryAdapter implements GlAccountQueryPort {

    private final SpringDataAccountRepository accountRepository;
    private final SpringDataBalanceSnapshotRepository balanceSnapshotRepository;
    private final SpringDataJournalLineRepository journalLineRepository;

    public GlAccountQueryAdapter(SpringDataAccountRepository accountRepository,
                                 SpringDataBalanceSnapshotRepository balanceSnapshotRepository,
                                 SpringDataJournalLineRepository journalLineRepository) {
        this.accountRepository = accountRepository;
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.journalLineRepository = journalLineRepository;
    }

    @Override
    public Optional<Money> getAccountBalance(Long organizationId, String accountCode, LocalDate asOf) {
        if (organizationId == null || accountCode == null || asOf == null) {
            return Optional.empty();
        }

        Optional<AccountJpa> accountOpt = accountRepository.findByOrganizationIdAndCode(organizationId, accountCode);
        if (accountOpt.isEmpty()) {
            return Optional.empty();
        }

        Long accountId = accountOpt.get().getId();
        return balanceSnapshotRepository
                .findTopByAccountIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(accountId, asOf)
                .map(snapshot -> Money.of(snapshot.getBalanceAmount(), CurrencyCode.valueOf(snapshot.getBalanceCurrency())));
    }

    @Override
    public List<Long> getJournalLineIds(Long organizationId, String accountCode, LocalDate asOf) {
        if (organizationId == null || accountCode == null || asOf == null) {
            return Collections.emptyList();
        }

        Optional<AccountJpa> accountOpt = accountRepository.findByOrganizationIdAndCode(organizationId, accountCode);
        if (accountOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Long accountId = accountOpt.get().getId();
        return journalLineRepository.findAllByAccountId(accountId)
                .stream()
                .map(JournalLineJpa::getId)
                .toList();
    }
}
