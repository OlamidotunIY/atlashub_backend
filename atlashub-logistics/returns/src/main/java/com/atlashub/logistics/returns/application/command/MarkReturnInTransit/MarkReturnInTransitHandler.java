package com.atlashub.logistics.returns.application.command.MarkReturnInTransit;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.exceptions.ReturnShipmentNotFoundException;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MarkReturnInTransitHandler extends Command<MarkReturnInTransitCommand, MarkReturnInTransitResult> {

    private final ReturnShipmentRepository returnShipmentRepository;

    public MarkReturnInTransitHandler(ReturnShipmentRepository returnShipmentRepository) {
        this.returnShipmentRepository = returnShipmentRepository;
    }

    @Override
    @Transactional
    public MarkReturnInTransitResult execute(MarkReturnInTransitCommand input) {
        ReturnShipment returnShipment = returnShipmentRepository.findById(input.returnShipmentId())
                .orElseThrow(() -> new ReturnShipmentNotFoundException(input.returnShipmentId()));

        returnShipment.markInTransit(input.correlationId());
        returnShipmentRepository.save(returnShipment);

        return new MarkReturnInTransitResult(
                returnShipment.getId(),
                returnShipment.getStatus().name()
        );
    }
}
