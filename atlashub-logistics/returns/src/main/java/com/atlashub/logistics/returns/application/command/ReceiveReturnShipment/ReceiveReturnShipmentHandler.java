package com.atlashub.logistics.returns.application.command.ReceiveReturnShipment;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.exceptions.ReturnShipmentNotFoundException;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReceiveReturnShipmentHandler extends Command<ReceiveReturnShipmentCommand, ReceiveReturnShipmentResult> {

    private final ReturnShipmentRepository returnShipmentRepository;

    public ReceiveReturnShipmentHandler(ReturnShipmentRepository returnShipmentRepository) {
        this.returnShipmentRepository = returnShipmentRepository;
    }

    @Override
    @Transactional
    public ReceiveReturnShipmentResult execute(ReceiveReturnShipmentCommand input) {
        ReturnShipment returnShipment = returnShipmentRepository.findById(input.returnShipmentId())
                .orElseThrow(() -> new ReturnShipmentNotFoundException(input.returnShipmentId()));

        returnShipment.markReceived(input.inspectionNotes(), input.receivedAt(), input.correlationId());
        returnShipmentRepository.save(returnShipment);

        return new ReceiveReturnShipmentResult(
                returnShipment.getId(),
                returnShipment.getStatus().name(),
                returnShipment.getCustomerRefundAmount(),
                returnShipment.getMerchantPenaltyAmount(),
                returnShipment.getMerchantCompensationAmount(),
                returnShipment.isRefundCustomerShipping(),
                returnShipment.getReceivedAt()
        );
    }
}
