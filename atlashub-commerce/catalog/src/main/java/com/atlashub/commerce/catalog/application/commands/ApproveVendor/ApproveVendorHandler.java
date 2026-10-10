package com.atlashub.commerce.catalog.application.commands.ApproveVendor;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.domain.exceptions.VendorNotFoundException;
import com.atlashub.commerce.catalog.domain.repositories.VendorRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ApproveVendorHandler extends Command<ApproveVendorCommand, Void> {

    private final VendorRepository vendorRepository;

    public ApproveVendorHandler(VendorRepository vendorRepository) {
        this.vendorRepository = Objects.requireNonNull(vendorRepository, "VendorRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:vendors:approve')")
    public Void execute(ApproveVendorCommand command) {
        Vendor vendor = vendorRepository.findById(command.vendorId())
                .orElseThrow(() -> new VendorNotFoundException(command.vendorId()));

        vendor.approve();
        vendorRepository.save(vendor);
        return null;
    }
}
