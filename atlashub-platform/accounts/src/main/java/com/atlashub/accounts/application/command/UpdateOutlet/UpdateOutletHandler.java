package com.atlashub.accounts.application.command.UpdateOutlet;

import com.atlashub.accounts.domain.exceptions.OutletNotFoundException;
import com.atlashub.accounts.domain.repositories.OutletRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class UpdateOutletHandler extends Command<UpdateOutletCommand, Void> {

    private final OutletRepository outletRepository;

    public UpdateOutletHandler(OutletRepository outletRepository) {
        this.outletRepository = outletRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('accounts:outlets:manage')")
    public Void execute(UpdateOutletCommand command) {
        var outlet = outletRepository.findById(command.outletId()).filter(found -> found.getOrganizationId().equals(command.organizationId())).orElseThrow(() -> new OutletNotFoundException("Outlet not found: " + command.outletId()));

        outlet.updateDetails(command.name(), command.address(), command.city(), command.state(), command.managerId());

        outletRepository.save(outlet);
        return null;
    }
}
