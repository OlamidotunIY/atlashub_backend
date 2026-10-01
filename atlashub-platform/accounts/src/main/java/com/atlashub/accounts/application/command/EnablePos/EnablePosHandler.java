package com.atlashub.accounts.application.command.EnablePos;

import com.atlashub.accounts.domain.exceptions.OrganizationNotFoundException;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EnablePosHandler extends Command<EnablePosCommand, Void> {

    private final OrganizationRepository organizationRepository;

    public EnablePosHandler(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public Void execute(EnablePosCommand command) {
        var org = organizationRepository.findById(command.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "Organization not found: " + command.organizationId()));
        org.enablePos();
        organizationRepository.save(org);
        return null;
    }
}
