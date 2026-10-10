package com.atlashub.commerce.catalog.application.queries.SearchProductByBarcode;

import com.atlashub.commerce.catalog.application.queries.ListProducts.ProductResult;
import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.exceptions.ProductNotFoundException;
import com.atlashub.commerce.catalog.domain.repositories.ProductRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SearchProductByBarcodeHandler extends Query<SearchProductByBarcodeQuery, ProductResult> {

    private final ProductRepository productRepository;

    public SearchProductByBarcodeHandler(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository, "ProductRepository must not be null");
    }

    @Override
    public ProductResult execute(SearchProductByBarcodeQuery query) {
        Product product = productRepository.findByOrganizationIdAndBarcode(query.organizationId(), query.barcode())
                .orElseThrow(() -> new ProductNotFoundException(0L));

        return new ProductResult(
                product.getId(),
                product.getOrganizationId(),
                product.getVendorId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getCategoryId(),
                product.getDepartmentId(),
                product.getManufacturerId(),
                product.isTaxable(),
                product.isService(),
                product.isHasVariants(),
                product.getStatus(),
                product.getType(),
                product.getCreatedAt()
        );
    }
}
