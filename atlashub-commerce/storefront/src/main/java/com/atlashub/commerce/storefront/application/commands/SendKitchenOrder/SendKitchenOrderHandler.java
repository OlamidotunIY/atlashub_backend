package com.atlashub.commerce.storefront.application.commands.SendKitchenOrder;

import com.atlashub.commerce.storefront.domain.entities.KitchenOrderTicket;
import com.atlashub.commerce.storefront.domain.entities.KotItem;
import com.atlashub.commerce.storefront.domain.repositories.KitchenOrderTicketRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class SendKitchenOrderHandler extends Command<SendKitchenOrderCommand, SendKitchenOrderResult> {

    private final KitchenOrderTicketRepository kotRepository;

    public SendKitchenOrderHandler(KitchenOrderTicketRepository kotRepository) {
        this.kotRepository = Objects.requireNonNull(kotRepository, "KitchenOrderTicketRepository must not be null");
    }

    @Override
    public SendKitchenOrderResult execute(SendKitchenOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        Long kotId = kotRepository.nextIdentity();
        List<KotItem> items = new ArrayList<>();
        if (command.items() != null) {
            for (KotItemDto dto : command.items()) {
                items.add(KotItem.create(null, kotId, dto.productId(), dto.name(), dto.quantity()));
            }
        }

        KitchenOrderTicket kot = KitchenOrderTicket.create(
                kotId,
                command.salesOrderId(),
                command.tableId(),
                command.outletId(),
                items
        );

        KitchenOrderTicket savedKot = kotRepository.save(kot);
        return new SendKitchenOrderResult(savedKot.getId());
    }
}
