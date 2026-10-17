package com.atlashub.logistics.returns.domain.entities;

import com.atlashub.logistics.returns.domain.events.ReturnShipmentCreatedEvent;
import com.atlashub.logistics.returns.domain.events.ReturnShipmentInTransitEvent;
import com.atlashub.logistics.returns.domain.events.ReturnShipmentReceivedEvent;
import com.atlashub.logistics.returns.domain.events.ReturnShipmentRejectedEvent;
import com.atlashub.logistics.returns.domain.exceptions.InsufficientDeliveryAttemptsForRtoException;
import com.atlashub.logistics.returns.domain.exceptions.InvalidReturnStatusException;
import com.atlashub.logistics.returns.domain.exceptions.ReturnsInvariantException;
import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class ReturnShipment extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long originalSalesOrderId;
    private final Long originalShipmentId;
    private final Long customerId;
    private final Address originAddress;
    private final Long destinationOutletId;
    private final String returnTrackingNumber;
    private final ReturnReasonType reasonType;
    private final ReturnFaultType faultType;
    private final int deliveryAttemptsConfirmed;
    private final Money originalItemAmount;
    private final Money originalShippingFee;
    private final Money returnShippingFee;
    private final Money declaredValue;
    private final Money customerRefundAmount;
    private final Money merchantPenaltyAmount;
    private final Money merchantCompensationAmount;
    private final boolean refundCustomerShipping;
    private ReturnStatus status;
    private String inspectionNotes;
    private final ZonedDateTime createdAt;
    private ZonedDateTime receivedAt;

    public ReturnShipment(
            Long id,
            Long organizationId,
            Long originalSalesOrderId,
            Long originalShipmentId,
            Long customerId,
            Address originAddress,
            Long destinationOutletId,
            String returnTrackingNumber,
            ReturnReasonType reasonType,
            ReturnFaultType faultType,
            int deliveryAttemptsConfirmed,
            Money originalItemAmount,
            Money originalShippingFee,
            Money returnShippingFee,
            Money declaredValue,
            Money customerRefundAmount,
            Money merchantPenaltyAmount,
            Money merchantCompensationAmount,
            boolean refundCustomerShipping,
            ReturnStatus status,
            String inspectionNotes,
            ZonedDateTime createdAt,
            ZonedDateTime receivedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.originalSalesOrderId = originalSalesOrderId;
        this.originalShipmentId = originalShipmentId;
        this.customerId = customerId;
        this.originAddress = originAddress;
        this.destinationOutletId = destinationOutletId;
        this.returnTrackingNumber = returnTrackingNumber;
        this.reasonType = reasonType;
        this.faultType = faultType;
        this.deliveryAttemptsConfirmed = deliveryAttemptsConfirmed;
        this.originalItemAmount = originalItemAmount;
        this.originalShippingFee = originalShippingFee;
        this.returnShippingFee = returnShippingFee;
        this.declaredValue = declaredValue;
        this.customerRefundAmount = customerRefundAmount;
        this.merchantPenaltyAmount = merchantPenaltyAmount;
        this.merchantCompensationAmount = merchantCompensationAmount;
        this.refundCustomerShipping = refundCustomerShipping;
        this.status = status;
        this.inspectionNotes = inspectionNotes;
        this.createdAt = createdAt;
        this.receivedAt = receivedAt;
    }

    public static ReturnShipment create(
            Long id,
            Long organizationId,
            Long originalSalesOrderId,
            Long originalShipmentId,
            Long customerId,
            Address originAddress,
            Long destinationOutletId,
            String returnTrackingNumber,
            ReturnReasonType reasonType,
            ReturnFaultType faultType,
            int deliveryAttemptsConfirmed,
            Money originalItemAmount,
            Money originalShippingFee,
            Money returnShippingFee,
            Money declaredValue,
            String correlationId
    ) {
        if (id == null) {
            throw new ReturnsInvariantException("Return shipment ID cannot be null");
        }
        if (organizationId == null) {
            throw new ReturnsInvariantException("Organization ID cannot be null");
        }
        if (originalSalesOrderId == null) {
            throw new ReturnsInvariantException("Original sales order ID is required");
        }
        if (returnTrackingNumber == null || returnTrackingNumber.isBlank()) {
            throw new ReturnsInvariantException("Return tracking number cannot be blank");
        }
        if (reasonType == null) {
            throw new ReturnsInvariantException("Return reason type cannot be null");
        }
        if (faultType == null) {
            throw new ReturnsInvariantException("Return fault type cannot be null");
        }
        if (originalItemAmount == null || originalShippingFee == null || returnShippingFee == null || declaredValue == null) {
            throw new ReturnsInvariantException("Financial amounts are required");
        }

        // Enforce 3-part failed delivery policy: customer fault requires at least 2 attempts
        if (reasonType == ReturnReasonType.FAILED_DELIVERY_RTO && faultType == ReturnFaultType.CUSTOMER_FAULT) {
            if (deliveryAttemptsConfirmed < 2) {
                throw new InsufficientDeliveryAttemptsForRtoException(deliveryAttemptsConfirmed);
            }
        }

        // Financial resolution based on 3-part policy
        final boolean refundCustomerShipping;
        final Money customerRefundAmount;
        final Money merchantPenaltyAmount;
        final Money merchantCompensationAmount;

        switch (faultType) {
            case CUSTOMER_FAULT -> {
                refundCustomerShipping = false;
                customerRefundAmount = originalItemAmount.isGreaterThan(returnShippingFee)
                        ? originalItemAmount.subtract(returnShippingFee)
                        : Money.zero(originalItemAmount.currency());
                merchantPenaltyAmount = Money.zero(originalItemAmount.currency());
                merchantCompensationAmount = Money.zero(originalItemAmount.currency());
            }
            case MERCHANT_FAULT -> {
                refundCustomerShipping = true;
                customerRefundAmount = originalItemAmount.add(originalShippingFee);
                merchantPenaltyAmount = originalShippingFee.add(returnShippingFee);
                merchantCompensationAmount = Money.zero(originalItemAmount.currency());
            }
            case CARRIER_FAULT -> {
                refundCustomerShipping = true;
                customerRefundAmount = originalItemAmount.add(originalShippingFee);
                merchantPenaltyAmount = Money.zero(originalItemAmount.currency());
                merchantCompensationAmount = declaredValue;
            }
            default -> throw new ReturnsInvariantException("Unsupported return fault type: " + faultType);
        }

        ZonedDateTime now = ZonedDateTime.now();
        ReturnShipment returnShipment = new ReturnShipment(
                id,
                organizationId,
                originalSalesOrderId,
                originalShipmentId,
                customerId,
                originAddress,
                destinationOutletId,
                returnTrackingNumber.trim(),
                reasonType,
                faultType,
                deliveryAttemptsConfirmed,
                originalItemAmount,
                originalShippingFee,
                returnShippingFee,
                declaredValue,
                customerRefundAmount,
                merchantPenaltyAmount,
                merchantCompensationAmount,
                refundCustomerShipping,
                ReturnStatus.CREATED,
                null,
                now,
                null
        );

        returnShipment.registerEvent(ReturnShipmentCreatedEvent.of(
                id,
                correlationId,
                new ReturnShipmentCreatedEvent.Payload(
                        id,
                        organizationId,
                        originalSalesOrderId,
                        originalShipmentId,
                        customerId,
                        returnShipment.returnTrackingNumber,
                        reasonType.name(),
                        faultType.name(),
                        originalItemAmount,
                        originalShippingFee,
                        returnShippingFee,
                        customerRefundAmount,
                        merchantPenaltyAmount,
                        merchantCompensationAmount,
                        refundCustomerShipping,
                        ReturnStatus.CREATED.name(),
                        now
                )
        ));

        return returnShipment;
    }

    public void markInTransit(String correlationId) {
        if (this.status != ReturnStatus.CREATED) {
            throw new InvalidReturnStatusException("Only CREATED returns can be marked IN_TRANSIT. Current status: " + this.status);
        }
        this.status = ReturnStatus.IN_TRANSIT;

        ZonedDateTime now = ZonedDateTime.now();
        registerEvent(ReturnShipmentInTransitEvent.of(
                this.id,
                correlationId,
                new ReturnShipmentInTransitEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.returnTrackingNumber,
                        now
                )
        ));
    }

    public void markReceived(String inspectionNotes, ZonedDateTime receivedAt, String correlationId) {
        if (this.status != ReturnStatus.IN_TRANSIT && this.status != ReturnStatus.CREATED) {
            throw new InvalidReturnStatusException(
                    "Only CREATED or IN_TRANSIT returns can be received. Current status: " + this.status);
        }
        this.status = ReturnStatus.RECEIVED;
        this.inspectionNotes = inspectionNotes;
        this.receivedAt = receivedAt != null ? receivedAt : ZonedDateTime.now();

        registerEvent(ReturnShipmentReceivedEvent.of(
                this.id,
                correlationId,
                new ReturnShipmentReceivedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.originalSalesOrderId,
                        this.customerId,
                        this.faultType.name(),
                        this.customerRefundAmount,
                        this.merchantPenaltyAmount,
                        this.merchantCompensationAmount,
                        this.refundCustomerShipping,
                        this.inspectionNotes,
                        this.receivedAt
                )
        ));
    }

    public void reject(String reason, String correlationId) {
        if (this.status == ReturnStatus.REJECTED) {
            throw new InvalidReturnStatusException("Return is already rejected");
        }
        this.status = ReturnStatus.REJECTED;
        this.inspectionNotes = reason;

        ZonedDateTime now = ZonedDateTime.now();
        registerEvent(ReturnShipmentRejectedEvent.of(
                this.id,
                correlationId,
                new ReturnShipmentRejectedEvent.Payload(
                        this.id,
                        this.organizationId,
                        reason,
                        now
                )
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
