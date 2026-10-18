package com.atlashub.logistics.shipping.application.commands.RegisterRider;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.domain.repositories.DispatchRiderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegisterRiderHandler extends Command<RegisterRiderCommand, Long> {

    private final DispatchRiderRepository riderRepository;

    public RegisterRiderHandler(DispatchRiderRepository riderRepository) {
        this.riderRepository = riderRepository;
    }

    @Override
    @Transactional
    public Long execute(RegisterRiderCommand input) {
        Long riderId = riderRepository.nextIdentity();
        DispatchRider rider = DispatchRider.create(
                riderId,
                input.organizationId(),
                input.userId(),
                input.zoneCode(),
                input.vehicleId(),
                input.licenseNumber()
        );

        riderRepository.save(rider);
        return rider.getId();
    }
}
