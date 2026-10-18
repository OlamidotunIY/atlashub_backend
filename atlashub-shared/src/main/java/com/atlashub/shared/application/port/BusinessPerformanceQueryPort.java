package com.atlashub.shared.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface BusinessPerformanceQueryPort {

    record BusinessPerformanceSnapshot(
            Long organizationId,
            String environment,
            LocalDate asOfDate,
            BigDecimal totalGrossRevenue,
            BigDecimal totalNetRevenue,
            long totalTransactions,
            BigDecimal averageOrderValue,
            BigDecimal repeatCustomerRate,
            BigDecimal activeOutletsCount
    ) {}

    Optional<BusinessPerformanceSnapshot> getPerformance(Long organizationId, String environment, LocalDate asOfDate);
}
