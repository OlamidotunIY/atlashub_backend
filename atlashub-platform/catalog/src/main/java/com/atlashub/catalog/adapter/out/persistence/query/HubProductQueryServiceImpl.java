package com.atlashub.catalog.adapter.out.persistence.query;

import com.atlashub.catalog.application.port.HubProductQueryService;
import com.atlashub.catalog.application.result.HubProductDetailsResult;
import com.atlashub.catalog.application.result.HubProductDetailsResult.PricingResult;
import com.atlashub.catalog.domain.exception.ProductNotFoundException;
import com.atlashub.catalog.domain.repository.HubProductRepository;
import com.atlashub.catalog.domain.repository.ProductPricingRepository;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HubProductQueryServiceImpl implements HubProductQueryService {

    private final HubProductRepository productRepository;
    private final ProductPricingRepository tierRepository;

    @Override
    public HubProductDetailsResult getHubProductDetails(Long productId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        var pricingList = tierRepository.findAllByProductId(productId).stream()
                .map(tier -> new PricingResult(
                        tier.getId(),
                        tier.getBillingCycle(),
                        new Money(tier.getAmount().amount(), tier.getAmount().currency())
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


