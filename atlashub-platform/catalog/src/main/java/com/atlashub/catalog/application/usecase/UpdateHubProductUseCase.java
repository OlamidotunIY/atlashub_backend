package com.atlashub.catalog.application.usecase;

import com.atlashub.catalog.application.command.UpdateHubProductCommand;
import com.atlashub.catalog.application.result.UpdateHubProductResult;
import com.atlashub.catalog.domain.exception.CatalogErrorCode;
import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.repository.HubProductRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class UpdateHubProductUseCase extends BaseUseCase<UpdateHubProductCommand, UpdateHubProductResult> {
    private static final Logger log = LoggerFactory.getLogger(UpdateHubProductUseCase.class);
    private final DomainEventPublisher eventPublisher;
    private final HubProductRepository hubProductRepository;

    public UpdateHubProductUseCase(DomainEventPublisher eventPublisher, HubProductRepository hubProductRepository) {
        this.eventPublisher = eventPublisher;
        this.hubProductRepository = hubProductRepository;
    }

    @Override
    @Transactional
    public UpdateHubProductResult execute(UpdateHubProductCommand input) {
        log.info("Starting HubProduct Update for product with id {}", input.productId());

        HubProduct product = hubProductRepository.findById(input.productId()).orElseThrow(() -> new NotFoundException(CatalogErrorCode.PRODUCT_NOT_FOUND, "Product not found"));

        product.updateDetails(input.name(), input.description());

        hubProductRepository.save(product);

        log.debug("Product updated");

        publishEvents(product, eventPublisher);

        return new UpdateHubProductResult(
                product.getName(),
                product.getDescription(),
                product.getId()
        );
    }
}
