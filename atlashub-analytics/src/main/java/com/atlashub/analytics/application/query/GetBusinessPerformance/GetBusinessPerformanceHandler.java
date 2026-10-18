package com.atlashub.analytics.application.query.GetBusinessPerformance;

import com.atlashub.analytics.domain.entities.CustomerPerformanceProjection;
import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.analytics.domain.repositories.CustomerPerformanceProjectionRepository;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GetBusinessPerformanceHandler extends Query<GetBusinessPerformanceQuery, BusinessPerformanceResult> {

    private final DailySalesProjectionRepository salesRepository;
    private final CustomerPerformanceProjectionRepository customerRepository;

    public GetBusinessPerformanceHandler(
            DailySalesProjectionRepository salesRepository,
            CustomerPerformanceProjectionRepository customerRepository
    ) {
        this.salesRepository = salesRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessPerformanceResult execute(GetBusinessPerformanceQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        LocalDate to = query.asOfDate() != null ? query.asOfDate() : LocalDate.now();
        LocalDate from = to.minusDays(30);

        List<DailySalesProjection> sales = salesRepository.findByOrgEnvAndDateRange(
                query.organizationId(),
                env,
                null,
                from,
                to
        );

        long totalTx = sales.stream().mapToLong(DailySalesProjection::getTotalTransactions).sum();
        BigDecimal gross = sales.stream()
                .map(DailySalesProjection::getTotalGross)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = sales.stream()
                .map(DailySalesProjection::getTotalNet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal aov = totalTx > 0 ? net.divide(BigDecimal.valueOf(totalTx), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Set<Long> outlets = sales.stream().map(DailySalesProjection::getOutletId).collect(Collectors.toSet());
        BigDecimal activeOutlets = BigDecimal.valueOf(outlets.size());

        String month = String.format("%04d-%02d", to.getYear(), to.getMonthValue());
        List<CustomerPerformanceProjection> customers = customerRepository.findByOrgEnvAndMonth(
                query.organizationId(),
                env,
                month
        );
        long repeatCount = customers.stream().filter(c -> c.getTotalOrders() > 1).count();
        BigDecimal repeatRate = !customers.isEmpty()
                ? BigDecimal.valueOf(repeatCount).divide(BigDecimal.valueOf(customers.size()), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new BusinessPerformanceResult(
                query.organizationId(),
                env,
                to,
                gross,
                net,
                totalTx,
                aov,
                repeatRate,
                activeOutlets
        );
    }
}
