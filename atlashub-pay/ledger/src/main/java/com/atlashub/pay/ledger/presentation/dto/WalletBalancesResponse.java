package com.atlashub.pay.ledger.presentation.dto;

import java.math.BigDecimal;
import java.util.Map;

public record WalletBalancesResponse(
        Long organizationId, Map<String, BigDecimal> balancesByAccountType, String currency) {}
