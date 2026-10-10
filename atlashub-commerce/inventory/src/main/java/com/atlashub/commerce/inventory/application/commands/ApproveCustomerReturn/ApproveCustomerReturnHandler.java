package com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn;

import com.atlashub.commerce.inventory.domain.entities.CustomerReturn;
import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.ReturnItem;
import com.atlashub.commerce.inventory.domain.exceptions.CustomerReturnNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.CustomerReturnRepository;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class ApproveCustomerReturnHandler extends Command<ApproveCustomerReturnCommand, Void> {

    private final CustomerReturnRepository customerReturnRepository;
    private final InventoryRepository inventoryRepository;

    public ApproveCustomerReturnHandler(CustomerReturnRepository customerReturnRepository,
                                        InventoryRepository inventoryRepository) {
        this.customerReturnRepository =
                Objects.requireNonNull(customerReturnRepository, "CustomerReturnRepository must not be null");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
    }

    @Override
    public Void execute(ApproveCustomerReturnCommand command) {
        CustomerReturn customerReturn = customerReturnRepository.findById(command.returnId())
                .orElseThrow(() -> new CustomerReturnNotFoundException(command.returnId()));

        customerReturn.approve();

        for (ReturnItem item : customerReturn.getItems()) {
            if (item.getQuantity() > 0) {
                Optional<Inventory> existing = inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(
                        customerReturn.getOrganizationId(), customerReturn.getOutletId(), item.getProductId());

                if (existing.isPresent()) {
                    Inventory inventory = existing.get();
                    inventory.addStock(item.getQuantity());
                    inventoryRepository.save(inventory);
                } else {
                    Long newInvId = inventoryRepository.nextIdentity();
                    Inventory newInventory =
                            Inventory.create(newInvId, customerReturn.getOrganizationId(), customerReturn.getOutletId(),
                                    item.getProductId(), null, item.getQuantity(), 10, 5);
                    inventoryRepository.save(newInventory);
                }
            }
        }

        customerReturnRepository.save(customerReturn);
        return null;
    }
}
