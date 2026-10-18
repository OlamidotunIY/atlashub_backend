package com.atlashub.logistics.returns.application.query.ListReturnShipments;

import com.atlashub.logistics.returns.application.query.GetReturnShipment.ReturnShipmentResult;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListReturnShipmentsHandler extends Query<ListReturnShipmentsQuery, List<ReturnShipmentResult>> {

    private final ReturnShipmentRepository returnShipmentRepository;

    public ListReturnShipmentsHandler(ReturnShipmentRepository returnShipmentRepository) {
        this.returnShipmentRepository = returnShipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnShipmentResult> execute(ListReturnShipmentsQuery query) {
        return returnShipmentRepository.findByOrganizationId(query.organizationId())
                .stream()
                .map(r -> new ReturnShipmentResult(
                        r.getId(),
                        r.getOrganizationId(),
                        r.getOriginalSalesOrderId(),
                        r.getOriginalShipmentId(),
                        r.getCustomerId(),
                        r.getOriginAddress(),
                        r.getDestinationOutletId(),
                        r.getReturnTrackingNumber(),
                        r.getReasonType().name(),
                        r.getFaultType().name(),
                        r.getDeliveryAttemptsConfirmed(),
                        r.getOriginalItemAmount(),
                        r.getOriginalShippingFee(),
                        r.getReturnShippingFee(),
                        r.getDeclaredValue(),
                        r.getCustomerRefundAmount(),
                        r.getMerchantPenaltyAmount(),
                        r.getMerchantCompensationAmount(),
                        r.isRefundCustomerShipping(),
                        r.getStatus().name(),
                        r.getInspectionNotes(),
                        r.getCreatedAt(),
                        r.getReceivedAt()
                ))
                .toList();
    }
}
