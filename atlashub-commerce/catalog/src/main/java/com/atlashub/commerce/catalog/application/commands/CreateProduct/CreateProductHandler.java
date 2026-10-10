package com.atlashub.commerce.catalog.application.commands.CreateProduct;

import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.exceptions.ProductCodeAlreadyExistsException;
import com.atlashub.commerce.catalog.domain.repositories.ProductRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreateProductHandler extends Command<CreateProductCommand, CreateProductResult> {

    private final ProductRepository productRepository;

    public CreateProductHandler(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository, "ProductRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:products:create')")
    public CreateProductResult execute(CreateProductCommand command) {
        productRepository.findByOrganizationIdAndCode(command.organizationId(), command.code())
                .ifPresent(p -> {
                    throw new ProductCodeAlreadyExistsException(command.code());
                });

        Long productId = productRepository.nextIdentity();
        Product product = Product.create(
                productId,
                command.organizationId(),
                command.vendorId(),
                command.code(),
                command.name(),
                command.description(),
                command.categoryId(),
                command.departmentId(),
                command.manufacturerId(),
                command.taxable(),
                command.service(),
                command.hasVariants(),
                command.type()
        );

        productRepository.save(product);
        return new CreateProductResult(productId);
    }
}
