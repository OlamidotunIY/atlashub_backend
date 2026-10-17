package com.atlashub.logistics.shipping.application.commands.SetRiderOffline;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.domain.exceptions.RiderNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.DispatchRiderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SetRiderOfflineHandler extends Command<SetRiderOfflineCommand, Void> {

    private final DispatchRiderRepository riderRepository;

    public SetRiderOfflineHandler(DispatchRiderRepository riderRepository) {
        this.riderRepository = riderRepository;
    }

    @Override
    @Transactional
    public Void execute(SetRiderOfflineCommand input) {
        DispatchRider rider = riderRepository.findById(input.riderId())
                .orElseThrow(() -> new RiderNotFoundException(input.riderId()));

        rider.goOffline();
        riderRepository.save(rider);
        return null;
    }
}
