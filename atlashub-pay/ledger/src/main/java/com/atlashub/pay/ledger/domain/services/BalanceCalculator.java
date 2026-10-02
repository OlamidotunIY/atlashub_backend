package com.atlashub.pay.ledger.domain.services;

import com.atlashub.pay.ledger.domain.entities.LedgerEntry;
import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.domain.valueobject.EntryType;
import com.atlashub.pay.ledger.domain.valueobject.NormalBalance;

import java.math.BigDecimal;
import java.util.List;

public class BalanceCalculator {

    public BigDecimal calculateRunningBalance(Long accountId, NormalBalance normalBalance,
                                              BigDecimal startingBalance, List<LedgerTransaction> transactions) {
        BigDecimal balance = startingBalance;
        for (LedgerTransaction tx : transactions) {
            for (LedgerEntry entry : tx.getEntries()) {
                if (entry.getAccountId().equals(accountId)) {
                    if ((normalBalance == NormalBalance.CREDIT && entry.getType() == EntryType.CREDIT)
                            || (normalBalance == NormalBalance.DEBIT && entry.getType() == EntryType.DEBIT)) {
                        balance = balance.add(entry.getAmount().amount());
                    } else {
                        balance = balance.subtract(entry.getAmount().amount());
                    }
                }
            }
        }
        return balance;
    }
}
