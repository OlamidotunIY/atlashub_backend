package com.atlashub.commerce.catalog.application.commands.SetProductPrice;

import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.entities.ProductPrice;
import com.atlashub.commerce.catalog.domain.exceptions.ProductNotFoundException;
import com.atlashub.commerce.catalog.domain.repositories.ProductRepository;
import com.atlashub.shared.application.port.ProductPricePort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SetProductPriceHandler extends Command<SetProductPriceCommand, Void> {

    private final ProductRepository productRepository;
    private final ProductPricePort productPricePort;

    public SetProductPriceHandler(ProductRepository productRepository, ProductPricePort productPricePort) {
        this.productRepository = Objects.requireNonNull(productRepository, "ProductRepository must not be null");
        this.productPricePort = Objects.requireNonNull(productPricePort, "ProductPricePort must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:products:create')")
    public Void execute(SetProductPriceCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        Long priceId = productPricePort.nextIdentity();
        ProductPrice price = ProductPrice.create(
                priceId,
                product.getId(),
                command.variantId(),
                command.priceLevel(),
                command.costPrice(),
                command.sellingPrice()
        );

        productPricePort.savePrice(
                price.getId(),
                price.getProductId(),
                price.getVariantId(),
                price.getPriceLevel().name(),
                price.getCostPrice(),
                price.getSellingPrice(),
                price.getMarkup()
        );
        return null;
    }
}
