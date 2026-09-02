package com.atlashub.catalog.application.usecase;

import com.atlashub.catalog.application.query.GetHubProductDetailsQuery;
import com.atlashub.catalog.application.result.HubProductDetailsResult;
import com.atlashub.catalog.domain.exception.CatalogErrorCode;
import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.model.ProductPricing;
import com.atlashub.catalog.domain.repository.HubProductRepository;
import com.atlashub.catalog.domain.repository.ProductPricingRepository;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GetHubProductDetailsUseCase extends BaseUseCase<GetHubProductDetailsQuery, HubProductDetailsResult> {

    private final HubProductRepository hubProductRepository;
    private final ProductPricingRepository productPricingRepository;

    public GetHubProductDetailsUseCase(
            HubProductRepository hubProductRepository,
            ProductPricingRepository productPricingRepository) {
        this.hubProductRepository = hubProductRepository;
        this.productPricingRepository = productPricingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public HubProductDetailsResult execute(GetHubProductDetailsQuery input) {
        HubProduct product = hubProductRepository.findById(input.productId())
                .orElseThrow(() -> new NotFoundException(
                        CatalogErrorCode.PRODUCT_NOT_FOUND,
                        "HubProduct not found with id: " + input.productId()));

        List<ProductPricing> pricingList = productPricingRepository.findAllByProductId(input.productId());

        return HubProductDetailsResult.from(product, pricingList);
    }
}
