package com.atlashub.accounts.application.command.SuspendOrganizationSubscription;

import com.atlashub.accounts.domain.exceptions.OrganizationNotFoundException;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the downstream reaction when billing suspends an organization's subscription.
 * Currently logs the suspension; future iterations may flag the organization in the DB.
 */
@Component
public class SuspendOrganizationSubscriptionHandler extends Command<SuspendOrganizationSubscriptionCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(SuspendOrganizationSubscriptionHandler.class);

    private final OrganizationRepository organizationRepository;

    public SuspendOrganizationSubscriptionHandler(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public Void execute(SuspendOrganizationSubscriptionCommand command) {
        organizationRepository.findById(command.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "Organization not found: " + command.organizationId()));

        log.warn("Subscription suspended for organizationId={} reason={}",
                command.organizationId(), command.reason());
        // Future: set org suspended flag, disable POS, etc.
        return null;
    }
}
