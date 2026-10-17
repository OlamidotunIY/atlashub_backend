package com.atlashub.analytics.application.query.GetTopSellingProducts;

import com.atlashub.analytics.domain.entities.ProductDemandProjection;
import com.atlashub.analytics.domain.repositories.ProductDemandProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetTopSellingProductsHandler extends Query<GetTopSellingProductsQuery, List<TopProductResult>> {

    private final ProductDemandProjectionRepository repository;

    public GetTopSellingProductsHandler(ProductDemandProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductResult> execute(GetTopSellingProductsQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        List<ProductDemandProjection> list = repository.findTopProducts(
                query.organizationId(),
                env,
                query.month()
        );

        int max = query.limit() > 0 ? query.limit() : 10;
        return list.stream()
                .limit(max)
                .map(p -> new TopProductResult(
                        p.getProductId(),
                        p.getQuantitySold(),
                        p.getTotalRevenue(),
                        p.getMetricMonth()
                ))
                .collect(Collectors.toList());
    }
}
