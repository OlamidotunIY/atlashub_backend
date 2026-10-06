package com.atlashub.accounts.application.command.CreateOutlet;

import com.atlashub.accounts.domain.entities.Outlet;
import com.atlashub.accounts.domain.exceptions.OrganizationNotFoundException;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.accounts.domain.repositories.OutletRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.accounts.domain.entities.Organization;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateOutletHandler extends Command<CreateOutletCommand, Long> {

    private final OutletRepository outletRepository;
    private final OrganizationRepository organizationRepository;

    public CreateOutletHandler(OutletRepository outletRepository, OrganizationRepository organizationRepository) {
        this.outletRepository = outletRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('accounts:outlets:manage')")
    public Long execute(CreateOutletCommand command) {
        Organization organization = organizationRepository.findById(command.organizationId()).orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + command.organizationId()));

        Outlet outlet = Outlet.create(outletRepository.nextIdentity(), command.organizationId(), command.name(), command.address(), command.city(), command.state(), organization.getCountry(), organization.getBaseCurrency(), command.managerId());

        outletRepository.save(outlet);
        return outlet.getId();
    }
}
