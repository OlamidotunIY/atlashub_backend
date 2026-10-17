package com.atlashub.logistics.returns.application.command.RejectReturnShipment;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.exceptions.ReturnShipmentNotFoundException;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RejectReturnShipmentHandler extends Command<RejectReturnShipmentCommand, RejectReturnShipmentResult> {

    private final ReturnShipmentRepository returnShipmentRepository;

    public RejectReturnShipmentHandler(ReturnShipmentRepository returnShipmentRepository) {
        this.returnShipmentRepository = returnShipmentRepository;
    }

    @Override
    @Transactional
    public RejectReturnShipmentResult execute(RejectReturnShipmentCommand input) {
        ReturnShipment returnShipment = returnShipmentRepository.findById(input.returnShipmentId())
                .orElseThrow(() -> new ReturnShipmentNotFoundException(input.returnShipmentId()));

        returnShipment.reject(input.reason(), input.correlationId());
        returnShipmentRepository.save(returnShipment);

        return new RejectReturnShipmentResult(
                returnShipment.getId(),
                returnShipment.getStatus().name(),
                input.reason()
        );
    }
}
