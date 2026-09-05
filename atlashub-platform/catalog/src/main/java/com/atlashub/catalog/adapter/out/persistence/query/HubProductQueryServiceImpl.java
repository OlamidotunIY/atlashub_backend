package com.atlashub.catalog.adapter.out.persistence.query;

import com.atlashub.catalog.adapter.out.persistence.repository.SpringDataProductRepository;
import com.atlashub.catalog.adapter.out.persistence.repository.SpringDataProductTierRepository;
import com.atlashub.catalog.application.port.HubProductQueryService;
import com.atlashub.catalog.application.result.HubProductDetailsResult;
import com.atlashub.catalog.application.result.HubProductDetailsResult.PricingResult;
import com.atlashub.catalog.domain.exception.CatalogErrorCode;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.money.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HubProductQueryServiceImpl implements HubProductQueryService {

    private final SpringDataProductRepository productRepository;
    private final SpringDataProductTierRepository tierRepository;

    @Override
    public HubProductDetailsResult getHubProductDetails(Long productId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(CatalogErrorCode.PRODUCT_NOT_FOUND, "Product not found"));

        var pricingList = tierRepository.findByProductId(productId).stream()
                .map(tier -> new PricingResult(
                        tier.getId(),
                        tier.getBillingCycle(),
                        new Money(tier.getAmount(), tier.getCurrency())
                ))
                .collect(Collectors.toList());

        return new HubProductDetailsResult(
                product.getId(),
                product.getKey(),
                product.getName(),
                product.getDescription(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                pricingList
        );
    }
}
