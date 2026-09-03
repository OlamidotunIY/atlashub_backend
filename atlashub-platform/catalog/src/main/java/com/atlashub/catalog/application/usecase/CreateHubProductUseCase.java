package com.atlashub.catalog.application.usecase;

import com.atlashub.catalog.application.command.CreateHubProductCommand;
import com.atlashub.catalog.application.result.CreateHubProductResult;
import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.repository.HubProductRepository;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class CreateHubProductUseCase extends BaseUseCase<CreateHubProductCommand, CreateHubProductResult> {

    private static final Logger log = LoggerFactory.getLogger(CreateHubProductUseCase.class);
    private final DomainEventPublisher eventPublisher;
    private final HubProductRepository hubProductRepository;


    public CreateHubProductUseCase(DomainEventPublisher eventPublisher, HubProductRepository hubProductRepository) {
        this.eventPublisher = eventPublisher;
        this.hubProductRepository = hubProductRepository;
    }

    @Override
    @Transactional
    public CreateHubProductResult execute(CreateHubProductCommand input) {
        log.info("Starting Hub Product creation for product {}", input.name());

        HubProduct product = HubProduct.create(
                hubProductRepository.nextIdentity(),
                input.key(),
                input.name(),
                input.description()
        );

        hubProductRepository.save(product);

        log.info("Hub Product Saved");

        publishEvents(product, eventPublisher);

        return new CreateHubProductResult(product);
    }
}
