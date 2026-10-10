package com.atlashub.commerce.storefront.application.commands.OpenTill;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.commerce.storefront.domain.exceptions.TillAlreadyOpenException;
import com.atlashub.commerce.storefront.domain.repositories.TillRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OpenTillHandler extends Command<OpenTillCommand, OpenTillResult> {

    private final TillRepository tillRepository;

    public OpenTillHandler(TillRepository tillRepository) {
        this.tillRepository = Objects.requireNonNull(tillRepository, "TillRepository must not be null");
    }

    @Override
    public OpenTillResult execute(OpenTillCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        if (tillRepository.findActiveTillByOutletId(command.outletId()).isPresent()) {
            throw new TillAlreadyOpenException(command.outletId());
        }

        Long tillId = tillRepository.nextIdentity();
        Till till = Till.create(
                tillId,
                command.organizationId(),
                command.outletId(),
                command.name(),
                command.openedBy(),
                command.openingFloat()
        );

        Till savedTill = tillRepository.save(till);
        return new OpenTillResult(savedTill.getId());
    }
}
