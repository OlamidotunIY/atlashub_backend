package com.atlashub.hr.staff.application.command.ResolveInfraction;

import com.atlashub.hr.staff.domain.entities.Infraction;
import com.atlashub.hr.staff.domain.exceptions.InfractionNotFoundException;
import com.atlashub.hr.staff.domain.repositories.InfractionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class ResolveInfractionHandler extends Command<ResolveInfractionCommand, Void> {

    private final InfractionRepository infractionRepository;

    public ResolveInfractionHandler(InfractionRepository infractionRepository) {
        this.infractionRepository = infractionRepository;
    }

    @Override
    public Void execute(ResolveInfractionCommand input) {
        Infraction infraction = infractionRepository.findById(input.infractionId())
                .orElseThrow(() -> new InfractionNotFoundException(input.infractionId()));

        infraction.resolve(input.resolutionNotes());
        infractionRepository.save(infraction);
        return null;
    }
}
