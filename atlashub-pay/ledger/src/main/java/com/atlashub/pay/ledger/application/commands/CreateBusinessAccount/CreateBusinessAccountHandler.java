package com.atlashub.pay.ledger.application.commands.CreateBusinessAccount;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.DuplicateLedgerAccountException;
import com.atlashub.pay.ledger.domain.exceptions.UnsupportedBusinessAccountTypeException;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.NormalBalance;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;

@Component
public class CreateBusinessAccountHandler extends Command<CreateBusinessAccountCommand, CreateBusinessAccountResult> {
    private static final Set<LedgerAccountType> ALLOWED = Set.of(
            LedgerAccountType.PAYROLL_RESERVE,
            LedgerAccountType.TAX_HOLDING,
            LedgerAccountType.ESCROW,
            LedgerAccountType.SPLIT_HOLDING,
            LedgerAccountType.CUSTOM);

    private final LedgerAccountRepository accounts;
    private final BalanceSnapshotRepository snapshots;

    public CreateBusinessAccountHandler(LedgerAccountRepository accounts, BalanceSnapshotRepository snapshots) {
        this.accounts = accounts;
        this.snapshots = snapshots;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:manage')")
    public CreateBusinessAccountResult execute(CreateBusinessAccountCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        LedgerAccountType type = LedgerAccountType.valueOf(command.accountType().toUpperCase(Locale.ROOT));
        CurrencyCode currency = CurrencyCode.valueOf(command.currency().toUpperCase(Locale.ROOT));
        if (!ALLOWED.contains(type)) {
            throw new UnsupportedBusinessAccountTypeException(type.name());
        }
        if (accounts.findByOrganizationIdAndEnvironmentAndAccountNameAndCurrency(command.organizationId(),
                environment, command.name(), currency).isPresent()) {
            throw new DuplicateLedgerAccountException(command.name());
        }
        if (type != LedgerAccountType.CUSTOM
                && accounts.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(command.organizationId(),
                environment, type, currency).isPresent()) {
            throw new DuplicateLedgerAccountException(type.name());
        }
        Long id = accounts.nextIdentity();
        LedgerAccount account = LedgerAccount.create(id, command.organizationId(), environment, type, command.name(),
                null, null, null, currency, NormalBalance.CREDIT);
        accounts.save(account);
        snapshots.save(BalanceSnapshot.create(snapshots.nextIdentity(), id, Money.of(BigDecimal.ZERO, currency),
                account.getCreatedAt()));
        return new CreateBusinessAccountResult(id);
    }
}
