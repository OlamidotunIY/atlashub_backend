package com.atlashub.analytics.application.query.GetBusinessOverview;

import com.atlashub.analytics.domain.entities.InventoryHealthProjection;
import com.atlashub.analytics.domain.repositories.AnalyticsMetricEventRepository;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import com.atlashub.analytics.domain.repositories.DeliveryPerformanceProjectionRepository;
import com.atlashub.analytics.domain.repositories.FinancialHealthProjectionRepository;
import com.atlashub.analytics.domain.repositories.HeadcountProjectionRepository;
import com.atlashub.analytics.domain.repositories.InventoryHealthProjectionRepository;
import com.atlashub.analytics.domain.repositories.OutletPerformanceProjectionRepository;
import com.atlashub.analytics.domain.repositories.PayrollCostProjectionRepository;
import com.atlashub.analytics.domain.repositories.ProductDemandProjectionRepository;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class GetBusinessOverviewHandler extends Query<GetBusinessOverviewQuery, BusinessOverviewResult> {
    private final DailySalesProjectionRepository sales;
    private final TransactionVolumeProjectionRepository payments;
    private final HeadcountProjectionRepository people;
    private final PayrollCostProjectionRepository payroll;
    private final OutletPerformanceProjectionRepository outlets;
    private final ProductDemandProjectionRepository products;
    private final DeliveryPerformanceProjectionRepository logistics;
    private final InventoryHealthProjectionRepository inventory;
    private final FinancialHealthProjectionRepository finance;
    private final AnalyticsMetricEventRepository activity;

    public GetBusinessOverviewHandler(
            DailySalesProjectionRepository sales,
            TransactionVolumeProjectionRepository payments,
            HeadcountProjectionRepository people,
            PayrollCostProjectionRepository payroll,
            OutletPerformanceProjectionRepository outlets,
            ProductDemandProjectionRepository products,
            DeliveryPerformanceProjectionRepository logistics,
            InventoryHealthProjectionRepository inventory,
            FinancialHealthProjectionRepository finance,
            AnalyticsMetricEventRepository activity
    ) {
        this.sales = sales;
        this.payments = payments;
        this.people = people;
        this.payroll = payroll;
        this.outlets = outlets;
        this.products = products;
        this.logistics = logistics;
        this.inventory = inventory;
        this.finance = finance;
        this.activity = activity;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('analytics:reports:read')")
    public BusinessOverviewResult execute(GetBusinessOverviewQuery query) {
        String environment = query.environment() != null ? query.environment() : "live";
        LocalDate to = query.asOfDate() != null ? query.asOfDate() : LocalDate.now();
        LocalDate from = to.minusDays(29);
        String month = String.format("%04d-%02d", to.getYear(), to.getMonthValue());

        var salesRows = sales.findByOrgEnvAndDateRange(query.organizationId(), environment, null, from, to);
        long transactionCount = salesRows.stream().mapToLong(row -> row.getTotalTransactions()).sum();
        BigDecimal netRevenue = sum(salesRows.stream().map(row -> row.getTotalNet()).toList());
        BigDecimal averageOrderValue = transactionCount == 0 ? BigDecimal.ZERO
                : netRevenue.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP);

        var payment = payments.findByOrgEnvAndMonth(query.organizationId(), environment, month).orElse(null);
        var headcount = people.findByOrgEnv(query.organizationId(), environment).orElse(null);
        var payrollCost = payroll.findByOrgEnvAndMonth(query.organizationId(), environment, month).orElse(null);
        var outletRows = outlets.findByOrgEnvAndMonth(query.organizationId(), environment, month);
        var productRows = products.findTopProducts(query.organizationId(), environment, month);
        var delivery = logistics.findByOrgEnvAndMonth(query.organizationId(), environment, month).orElse(null);
        List<InventoryHealthProjection> stock = inventory.findByOrgEnvAndOutlet(
                query.organizationId(), environment, null);
        var financialHealth = finance.findLatestByOrgEnv(query.organizationId(), environment).orElse(null);
        var recent = activity.findRecent(query.organizationId(), environment, 12);

        int outOfStock = (int) stock.stream().filter(item -> item.getCurrentStock() <= 0).count();
        int lowStock = (int) stock.stream()
                .filter(item -> item.getCurrentStock() > 0 && item.getCurrentStock() <= item.getReorderPoint()).count();
        int deadStock = (int) stock.stream().filter(InventoryHealthProjection::isDeadStock).count();

        List<BusinessOverviewResult.Attention> attention = new ArrayList<>();
        addAttention(attention, "OUT_OF_STOCK", "commerce", "critical", outOfStock);
        addAttention(attention, "LOW_STOCK", "commerce", "warning", lowStock);
        addAttention(attention, "FAILED_PAYMENTS", "pay", "warning",
                payment == null ? 0 : (int) payment.getFailedCharges());
        addAttention(attention, "FAILED_DELIVERIES", "logistics", "warning",
                delivery == null ? 0 : delivery.getFailed());

        return new BusinessOverviewResult(
                from,
                to,
                new BusinessOverviewResult.Sales(netRevenue, transactionCount, averageOrderValue),
                new BusinessOverviewResult.Payments(
                        payment == null ? BigDecimal.ZERO : payment.getTotalVolume(),
                        payment == null ? 0 : payment.getSuccessfulCharges(),
                        payment == null ? 0 : payment.getFailedCharges(),
                        payment == null ? BigDecimal.ZERO : payment.getTotalPayoutAmount()),
                new BusinessOverviewResult.People(
                        headcount == null ? 0 : headcount.getTotal(),
                        headcount == null ? 0 : headcount.getActive(),
                        headcount == null ? 0 : headcount.getSuspended()),
                new BusinessOverviewResult.Payroll(
                        payrollCost == null ? BigDecimal.ZERO : payrollCost.getTotalGrossPayroll(),
                        payrollCost == null ? BigDecimal.ZERO : payrollCost.getTotalDeductions(),
                        payrollCost == null ? BigDecimal.ZERO : payrollCost.getTotalNetPayroll(),
                        payrollCost == null ? 0 : payrollCost.getHeadcount()),
                new BusinessOverviewResult.Commerce(
                        outletRows.size(),
                        sum(outletRows.stream().map(row -> row.getTotalSales()).toList()),
                        sum(outletRows.stream().map(row -> row.getTotalDiscounts()).toList()),
                        sum(outletRows.stream().map(row -> row.getTotalExpenses()).toList()),
                        sum(outletRows.stream().map(row -> row.getNetProfit()).toList()),
                        productRows.size(),
                        productRows.stream().mapToInt(row -> row.getQuantitySold()).sum(),
                        sum(productRows.stream().map(row -> row.getTotalRevenue()).toList())),
                new BusinessOverviewResult.Logistics(
                        delivery == null ? 0 : delivery.getTotalShipments(),
                        delivery == null ? 0 : delivery.getDelivered(),
                        delivery == null ? 0 : delivery.getFailed(),
                        delivery == null ? BigDecimal.ZERO : delivery.getDeliverySuccessRate()),
                new BusinessOverviewResult.Inventory(outOfStock, lowStock, deadStock,
                        sum(stock.stream().map(item -> item.getInventoryValue()).toList())),
                new BusinessOverviewResult.Finance(
                        financialHealth != null,
                        financialHealth == null ? BigDecimal.ZERO : financialHealth.getCashBalance(),
                        financialHealth == null ? BigDecimal.ZERO : financialHealth.getAccountsReceivable(),
                        financialHealth == null ? BigDecimal.ZERO : financialHealth.getAccountsPayable(),
                        financialHealth == null ? "UNAVAILABLE" : financialHealth.getReconciliationStatus()),
                List.copyOf(attention),
                recent.stream().map(event -> new BusinessOverviewResult.Activity(
                        event.getEventId(), event.getModule(), event.getMetricType(), event.getAmount(),
                        event.getCurrency(), event.getOccurredAt())).toList()
        );
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void addAttention(List<BusinessOverviewResult.Attention> items, String code, String module,
                              String severity, int count) {
        if (count > 0) {
            items.add(new BusinessOverviewResult.Attention(code, module, severity, count));
        }
    }
}
