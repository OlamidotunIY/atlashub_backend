package com.atlashub.accounts.application.command.DisablePos;

import com.atlashub.accounts.domain.exceptions.OrganizationNotFoundException;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DisablePosHandler extends Command<DisablePosCommand, Void> {

    private final OrganizationRepository organizationRepository;

    public DisablePosHandler(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public Void execute(DisablePosCommand command) {
        var org = organizationRepository.findById(command.organizationId()).orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + command.organizationId()));
        org.disablePos();
        organizationRepository.save(org);
        return null;
    }
}
