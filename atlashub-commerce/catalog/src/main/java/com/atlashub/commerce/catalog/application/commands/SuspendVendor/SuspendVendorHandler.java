package com.atlashub.commerce.catalog.application.commands.SuspendVendor;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.domain.exceptions.VendorNotFoundException;
import com.atlashub.commerce.catalog.domain.repositories.VendorRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SuspendVendorHandler extends Command<SuspendVendorCommand, Void> {

    private final VendorRepository vendorRepository;

    public SuspendVendorHandler(VendorRepository vendorRepository) {
        this.vendorRepository = Objects.requireNonNull(vendorRepository, "VendorRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:vendors:approve')")
    public Void execute(SuspendVendorCommand command) {
        Vendor vendor = vendorRepository.findById(command.vendorId())
                .orElseThrow(() -> new VendorNotFoundException(command.vendorId()));

        vendor.suspend(command.reason());
        vendorRepository.save(vendor);
        return null;
    }
}
