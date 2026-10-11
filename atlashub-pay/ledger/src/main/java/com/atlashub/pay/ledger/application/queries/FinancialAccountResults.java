package com.atlashub.pay.ledger.application.queries;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

public final class FinancialAccountResults {
    private FinancialAccountResults() {
    }

    public record BankAccountResult(Long id, String accountName, String maskedAccountNumber, String bankName,
                                    String bankCode, String currency, String status, ZonedDateTime activatedAt) {
    }

    public record FinancialAccountResult(Long id, String businessName, String name, String type, String scope,
                                         String currency, String status, boolean frozen, Set<String> restrictions,
                                         BigDecimal ledgerBalance, BigDecimal availableBalance,
                                         BigDecimal pendingSettlementBalance, ZonedDateTime balanceAsOf,
                                         BankAccountResult bankAccount, ZonedDateTime createdAt) {
    }
}
