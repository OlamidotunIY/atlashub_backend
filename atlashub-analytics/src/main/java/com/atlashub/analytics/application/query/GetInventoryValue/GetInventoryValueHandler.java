package com.atlashub.analytics.application.query.GetInventoryValue;

import com.atlashub.analytics.domain.entities.InventoryHealthProjection;
import com.atlashub.analytics.domain.repositories.InventoryHealthProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class GetInventoryValueHandler extends Query<GetInventoryValueQuery, InventoryValueResult> {

    private final InventoryHealthProjectionRepository repository;

    public GetInventoryValueHandler(InventoryHealthProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryValueResult execute(GetInventoryValueQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        List<InventoryHealthProjection> items = repository.findByOrgEnvAndOutlet(
                query.organizationId(),
                env,
                query.outletId()
        );

        int totalSkus = items.size();
        int totalStock = items.stream().mapToInt(InventoryHealthProjection::getCurrentStock).sum();
        BigDecimal totalValue = items.stream()
                .map(InventoryHealthProjection::getInventoryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int lowStockCount = (int) items.stream()
                .filter(i -> i.getCurrentStock() <= i.getReorderPoint())
                .count();

        return new InventoryValueResult(query.outletId(), totalSkus, totalStock, totalValue, lowStockCount);
    }
}
