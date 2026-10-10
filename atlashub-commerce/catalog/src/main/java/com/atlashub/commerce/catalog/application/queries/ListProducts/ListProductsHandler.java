package com.atlashub.commerce.catalog.application.queries.ListProducts;

import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.repositories.ProductRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListProductsHandler extends Query<ListProductsQuery, PageResult<ProductResult>> {

    private final ProductRepository productRepository;

    public ListProductsHandler(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository, "ProductRepository must not be null");
    }

    @Override
    public PageResult<ProductResult> execute(ListProductsQuery query) {
        PageResult<Product> page = productRepository.findByOrganizationId(
                query.organizationId(),
                query.vendorId(),
                query.categoryId(),
                query.status(),
                query.search(),
                query.page(),
                query.size()
        );

        List<ProductResult> results = page.content().stream()
                .map(this::toResult)
                .toList();

        return new PageResult<>(results, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }

    private ProductResult toResult(Product product) {
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
