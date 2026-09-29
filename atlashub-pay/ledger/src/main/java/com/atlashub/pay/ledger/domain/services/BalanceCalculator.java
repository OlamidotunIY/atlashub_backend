package com.atlashub.pay.ledger.domain.services;

import com.atlashub.pay.ledger.domain.entities.LedgerEntry;
import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.domain.valueobject.EntryType;

import java.math.BigDecimal;
import java.util.List;

public class BalanceCalculator {

    public BigDecimal calculateRunningBalance(Long accountId, BigDecimal startingBalance, List<LedgerTransaction> transactions) {
        BigDecimal balance = startingBalance;
        for (LedgerTransaction tx : transactions) {
            for (LedgerEntry entry : tx.getEntries()) {
                if (entry.getAccountId().equals(accountId)) {
                    if (entry.getType() == EntryType.CREDIT) {
                        balance = balance.add(entry.getAmount().amount());
                    } else if (entry.getType() == EntryType.DEBIT) {
                        balance = balance.subtract(entry.getAmount().amount());
                    }
                }
            }
        }
        return balance;
    }
}
