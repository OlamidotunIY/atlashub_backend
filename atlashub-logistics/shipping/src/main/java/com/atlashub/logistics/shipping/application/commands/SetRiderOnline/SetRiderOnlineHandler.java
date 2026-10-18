package com.atlashub.logistics.shipping.application.commands.SetRiderOnline;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.domain.exceptions.RiderNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.DispatchRiderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SetRiderOnlineHandler extends Command<SetRiderOnlineCommand, Void> {

    private final DispatchRiderRepository riderRepository;

    public SetRiderOnlineHandler(DispatchRiderRepository riderRepository) {
        this.riderRepository = riderRepository;
    }

    @Override
    @Transactional
    public Void execute(SetRiderOnlineCommand input) {
        DispatchRider rider = riderRepository.findById(input.riderId())
                .orElseThrow(() -> new RiderNotFoundException(input.riderId()));

        rider.goOnline();
        riderRepository.save(rider);
        return null;
    }
}
