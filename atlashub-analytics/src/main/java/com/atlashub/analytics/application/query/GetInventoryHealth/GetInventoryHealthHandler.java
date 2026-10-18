package com.atlashub.analytics.application.query.GetInventoryHealth;

import com.atlashub.analytics.domain.entities.InventoryHealthProjection;
import com.atlashub.analytics.domain.repositories.InventoryHealthProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetInventoryHealthHandler extends Query<GetInventoryHealthQuery, List<InventoryHealthResult>> {

    private final InventoryHealthProjectionRepository repository;

    public GetInventoryHealthHandler(InventoryHealthProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryHealthResult> execute(GetInventoryHealthQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        List<InventoryHealthProjection> list = repository.findByOrgEnvAndOutlet(
                query.organizationId(),
                env,
                query.outletId()
        );

        return list.stream()
                .map(p -> new InventoryHealthResult(
                        p.getProductId(),
                        p.getOutletId(),
                        p.getCurrentStock(),
                        p.getReorderPoint(),
                        p.getStockCoverageDays(),
                        p.getInventoryValue(),
                        p.isDeadStock()
                ))
                .collect(Collectors.toList());
    }
}
