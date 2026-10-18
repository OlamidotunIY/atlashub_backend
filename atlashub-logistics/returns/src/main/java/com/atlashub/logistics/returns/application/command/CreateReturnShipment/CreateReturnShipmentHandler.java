package com.atlashub.logistics.returns.application.command.CreateReturnShipment;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateReturnShipmentHandler extends Command<CreateReturnShipmentCommand, CreateReturnShipmentResult> {

    private final ReturnShipmentRepository returnShipmentRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public CreateReturnShipmentHandler(ReturnShipmentRepository returnShipmentRepository) {
        this.returnShipmentRepository = returnShipmentRepository;
    }

    @Override
    @Transactional
    public CreateReturnShipmentResult execute(CreateReturnShipmentCommand input) {
        Long id = returnShipmentRepository.nextIdentity();
        String returnTrackingNumber = "RET-" + System.currentTimeMillis() + "-" + (100 + secureRandom.nextInt(900));

        ReturnShipment returnShipment = ReturnShipment.create(
                id,
                input.organizationId(),
                input.originalSalesOrderId(),
                input.originalShipmentId(),
                input.customerId(),
                input.originAddress(),
                input.destinationOutletId(),
                returnTrackingNumber,
                input.reasonType(),
                input.faultType(),
                input.deliveryAttemptsConfirmed(),
                input.originalItemAmount(),
                input.originalShippingFee(),
                input.returnShippingFee(),
                input.declaredValue(),
                input.correlationId()
        );

        returnShipmentRepository.save(returnShipment);

        return new CreateReturnShipmentResult(
                returnShipment.getId(),
                returnShipment.getReturnTrackingNumber(),
                returnShipment.getStatus().name(),
                returnShipment.getFaultType().name(),
                returnShipment.getCustomerRefundAmount(),
                returnShipment.getMerchantPenaltyAmount(),
                returnShipment.getMerchantCompensationAmount(),
                returnShipment.isRefundCustomerShipping()
        );
    }
}
