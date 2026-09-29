package com.atlashub.pay.ledger.application.queries.GetWalletBalances;

import java.math.BigDecimal;
import java.util.Map;

public record WalletBalancesResult(Long organizationId, Map<String, BigDecimal> balancesByAccountType, String currency) {}
