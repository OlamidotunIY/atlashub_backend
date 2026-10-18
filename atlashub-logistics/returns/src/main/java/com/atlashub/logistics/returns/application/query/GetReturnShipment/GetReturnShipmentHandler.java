package com.atlashub.logistics.returns.application.query.GetReturnShipment;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.exceptions.ReturnShipmentNotFoundException;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetReturnShipmentHandler extends Query<GetReturnShipmentQuery, ReturnShipmentResult> {

    private final ReturnShipmentRepository returnShipmentRepository;

    public GetReturnShipmentHandler(ReturnShipmentRepository returnShipmentRepository) {
        this.returnShipmentRepository = returnShipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnShipmentResult execute(GetReturnShipmentQuery query) {
        ReturnShipment returnShipment = returnShipmentRepository.findById(query.returnShipmentId())
                .orElseThrow(() -> new ReturnShipmentNotFoundException(query.returnShipmentId()));

        return new ReturnShipmentResult(
                returnShipment.getId(),
                returnShipment.getOrganizationId(),
                returnShipment.getOriginalSalesOrderId(),
                returnShipment.getOriginalShipmentId(),
                returnShipment.getCustomerId(),
                returnShipment.getOriginAddress(),
                returnShipment.getDestinationOutletId(),
                returnShipment.getReturnTrackingNumber(),
                returnShipment.getReasonType().name(),
                returnShipment.getFaultType().name(),
                returnShipment.getDeliveryAttemptsConfirmed(),
                returnShipment.getOriginalItemAmount(),
                returnShipment.getOriginalShippingFee(),
                returnShipment.getReturnShippingFee(),
                returnShipment.getDeclaredValue(),
                returnShipment.getCustomerRefundAmount(),
                returnShipment.getMerchantPenaltyAmount(),
                returnShipment.getMerchantCompensationAmount(),
                returnShipment.isRefundCustomerShipping(),
                returnShipment.getStatus().name(),
                returnShipment.getInspectionNotes(),
                returnShipment.getCreatedAt(),
                returnShipment.getReceivedAt()
        );
    }
}
