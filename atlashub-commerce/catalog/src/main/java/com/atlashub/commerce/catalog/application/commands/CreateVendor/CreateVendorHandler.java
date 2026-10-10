package com.atlashub.commerce.catalog.application.commands.CreateVendor;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.domain.repositories.VendorRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreateVendorHandler extends Command<CreateVendorCommand, CreateVendorResult> {

    private final VendorRepository vendorRepository;

    public CreateVendorHandler(VendorRepository vendorRepository) {
        this.vendorRepository = Objects.requireNonNull(vendorRepository, "VendorRepository must not be null");
    }

    @Override
    public CreateVendorResult execute(CreateVendorCommand command) {
        Long vendorId = vendorRepository.nextIdentity();
        EmailAddress email = command.email() != null && !command.email().isBlank() ?
                new EmailAddress(command.email().trim()) : null;
        PhoneNumber phone = command.phone() != null && !command.phone().isBlank() ?
                new PhoneNumber(command.phone().trim()) : null;

        Vendor vendor = Vendor.create(
                vendorId,
                command.organizationId(),
                command.userId(),
                command.businessName(),
                email,
                phone,
                command.settlementBankCode(),
                command.settlementAccountNumber(),
                command.settlementAccountName(),
                command.commissionRate(),
                command.disbursementSchedule()
        );

        vendorRepository.save(vendor);
        return new CreateVendorResult(vendorId);
    }
}
