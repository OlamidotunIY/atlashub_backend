package com.atlashub.commerce.catalog.application.commands.CreateDiscount;

import com.atlashub.commerce.catalog.domain.entities.Discount;
import com.atlashub.commerce.catalog.domain.repositories.DiscountRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreateDiscountHandler extends Command<CreateDiscountCommand, CreateDiscountResult> {

    private final DiscountRepository discountRepository;

    public CreateDiscountHandler(DiscountRepository discountRepository) {
        this.discountRepository = Objects.requireNonNull(discountRepository, "DiscountRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:products:create')")
    public CreateDiscountResult execute(CreateDiscountCommand command) {
        Long discountId = discountRepository.nextIdentity();
        Discount discount =
                Discount.create(discountId, command.organizationId(), command.name(), command.type(), command.value(),
                        command.scope(), command.minOrderAmount(), command.maxUses(), command.validFrom(),
                        command.validTo());

        discountRepository.save(discount);
        return new CreateDiscountResult(discountId);
    }
}
