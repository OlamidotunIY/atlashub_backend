package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BusinessOverviewDto(
        LocalDate fromDate, LocalDate toDate, Sales sales, Payments payments, People people,
        Logistics logistics, Inventory inventory, Finance finance
) {
    public record Sales(BigDecimal netRevenue, long transactions, BigDecimal averageOrderValue) {}
    public record Payments(BigDecimal chargeVolume, long successfulCharges, long failedCharges, BigDecimal payoutAmount) {}
    public record People(int total, int active, int suspended) {}
    public record Logistics(int shipments, int delivered, int failed, BigDecimal deliverySuccessRate) {}
    public record Inventory(int outOfStockCount, int lowStockCount, int deadStockCount, BigDecimal inventoryValue) {}
    public record Finance(boolean available, BigDecimal cashBalance, BigDecimal receivables, BigDecimal payables, String reconciliationStatus) {}
}
