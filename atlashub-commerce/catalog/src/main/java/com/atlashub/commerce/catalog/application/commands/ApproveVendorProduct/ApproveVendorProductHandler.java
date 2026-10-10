package com.atlashub.commerce.catalog.application.commands.ApproveVendorProduct;

import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.exceptions.ProductNotFoundException;
import com.atlashub.commerce.catalog.domain.repositories.ProductRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ApproveVendorProductHandler extends Command<ApproveVendorProductCommand, Void> {

    private final ProductRepository productRepository;

    public ApproveVendorProductHandler(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository, "ProductRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:vendors:approve')")
    public Void execute(ApproveVendorProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.approve();
        productRepository.save(product);
        return null;
    }
}
