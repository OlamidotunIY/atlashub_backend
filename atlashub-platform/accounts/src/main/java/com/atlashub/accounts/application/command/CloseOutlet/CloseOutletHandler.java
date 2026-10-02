package com.atlashub.accounts.application.command.CloseOutlet;

import com.atlashub.accounts.domain.exceptions.OutletNotFoundException;
import com.atlashub.accounts.domain.repositories.OutletRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class CloseOutletHandler extends Command<CloseOutletCommand, Void> {

    private final OutletRepository outletRepository;

    public CloseOutletHandler(OutletRepository outletRepository) {
        this.outletRepository = outletRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('accounts:outlets:manage')")
    public Void execute(CloseOutletCommand command) {
        var outlet = outletRepository.findById(command.outletId()).filter(found -> found.getOrganizationId().equals(command.organizationId())).orElseThrow(() -> new OutletNotFoundException("Outlet not found: " + command.outletId()));

        outlet.close();
        outletRepository.save(outlet);
        return null;
    }
}
