package com.atlashub.commerce.catalog.application.commands.CreateSupplier;

import com.atlashub.commerce.catalog.domain.entities.Supplier;
import com.atlashub.commerce.catalog.domain.repositories.SupplierRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreateSupplierHandler extends Command<CreateSupplierCommand, CreateSupplierResult> {

    private final SupplierRepository supplierRepository;

    public CreateSupplierHandler(SupplierRepository supplierRepository) {
        this.supplierRepository = Objects.requireNonNull(supplierRepository, "SupplierRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:products:create')")
    public CreateSupplierResult execute(CreateSupplierCommand command) {
        Long supplierId = supplierRepository.nextIdentity();
        EmailAddress email = command.email() != null && !command.email().isBlank() ?
                new EmailAddress(command.email().trim()) : null;
        PhoneNumber phone = command.phone() != null && !command.phone().isBlank() ?
                new PhoneNumber(command.phone().trim()) : null;

        Supplier supplier = Supplier.create(
                supplierId,
                command.organizationId(),
                command.name(),
                email,
                phone,
                command.address()
        );

        supplierRepository.save(supplier);
        return new CreateSupplierResult(supplierId);
    }
}
