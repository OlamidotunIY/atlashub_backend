package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record BusinessOverviewDto(
        LocalDate fromDate, LocalDate toDate, Sales sales, Payments payments, People people,
        Payroll payroll, Commerce commerce, Logistics logistics, Inventory inventory, Finance finance,
        List<Attention> attention, List<Activity> recentActivity
) {
    public record Sales(BigDecimal netRevenue, long transactions, BigDecimal averageOrderValue) {}
    public record Payments(BigDecimal chargeVolume, long successfulCharges, long failedCharges, BigDecimal payoutAmount) {}
    public record People(int total, int active, int suspended) {}
    public record Payroll(BigDecimal gross, BigDecimal deductions, BigDecimal net, int headcount) {}
    public record Commerce(int outletCount, BigDecimal outletSales, BigDecimal discounts, BigDecimal expenses,
                           BigDecimal netProfit, int trackedProducts, int unitsSold, BigDecimal productRevenue) {}
    public record Logistics(int shipments, int delivered, int failed, BigDecimal deliverySuccessRate) {}
    public record Inventory(int outOfStockCount, int lowStockCount, int deadStockCount, BigDecimal inventoryValue) {}
    public record Finance(boolean available, BigDecimal cashBalance, BigDecimal receivables, BigDecimal payables, String reconciliationStatus) {}
    public record Attention(String code, String module, String severity, int count) {}
    public record Activity(String eventId, String module, String type, BigDecimal amount, String currency,
                           ZonedDateTime occurredAt) {}
}
