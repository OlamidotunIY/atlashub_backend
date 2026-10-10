package com.atlashub.commerce.storefront.application.commands.CloseTill;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.commerce.storefront.domain.exceptions.TillNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.TillRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CloseTillHandler extends Command<CloseTillCommand, Void> {

    private final TillRepository tillRepository;

    public CloseTillHandler(TillRepository tillRepository) {
        this.tillRepository = Objects.requireNonNull(tillRepository, "TillRepository must not be null");
    }

    @Override
    public Void execute(CloseTillCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        Till till = tillRepository.findById(command.tillId())
                .orElseThrow(() -> new TillNotFoundException(command.tillId()));

        till.close(command.closedBy(), command.actualClosingBalance());
        tillRepository.save(till);
        return null;
    }
}
