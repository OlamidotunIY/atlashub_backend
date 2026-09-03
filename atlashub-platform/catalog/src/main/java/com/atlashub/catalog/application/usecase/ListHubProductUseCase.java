package com.atlashub.catalog.application.usecase;

import com.atlashub.catalog.application.query.ListHubProductsQuery;
import com.atlashub.catalog.application.result.HubProductResult;
import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.repository.HubProductRepository;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

public class ListHubProductUseCase extends BaseUseCase<ListHubProductsQuery, List<HubProductResult>> {

    private final HubProductRepository hubProductRepository;

    public ListHubProductUseCase(HubProductRepository hubProductRepository) {
        this.hubProductRepository = hubProductRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubProductResult> execute(ListHubProductsQuery input) {
        List<HubProduct> products;
        
        if (input.status() != null) {
            products = hubProductRepository.findByStatus(input.status());
        } else {
            products = hubProductRepository.findAll();
        }

        return products.stream()
                .map(HubProductResult::from)
                .collect(Collectors.toList());
    }
}
