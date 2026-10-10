package com.atlashub.commerce.storefront.application.commands.MarkKotReady;

import com.atlashub.commerce.storefront.domain.entities.KitchenOrderTicket;
import com.atlashub.commerce.storefront.domain.exceptions.KitchenOrderTicketNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.KitchenOrderTicketRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MarkKotReadyHandler extends Command<MarkKotReadyCommand, Void> {

    private final KitchenOrderTicketRepository kotRepository;

    public MarkKotReadyHandler(KitchenOrderTicketRepository kotRepository) {
        this.kotRepository = Objects.requireNonNull(kotRepository, "KitchenOrderTicketRepository must not be null");
    }

    @Override
    public Void execute(MarkKotReadyCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        KitchenOrderTicket kot = kotRepository.findById(command.kotId())
                .orElseThrow(() -> new KitchenOrderTicketNotFoundException(command.kotId()));

        kot.markReady();
        kotRepository.save(kot);
        return null;
    }
}
