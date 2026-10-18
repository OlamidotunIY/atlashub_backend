package com.atlashub.logistics.shipping.infrastructure.services;

import com.atlashub.shared.application.port.ThirdPartyCarrierPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;

@Component
public class Partner3PLCarrierAdapter implements ThirdPartyCarrierPort {

    @Override
    public CarrierQuoteResult quoteShipment(String carrierCode, CarrierQuoteRequest request) {
        CurrencyCode currency = request.declaredValue() != null ? request.declaredValue().currency() : CurrencyCode.NGN;
        BigDecimal weight = request.weightKg() != null && request.weightKg().compareTo(BigDecimal.ZERO) > 0
                ? request.weightKg()
                : BigDecimal.ONE;

        Money baseCost = Money.of(new BigDecimal("1200.00"), currency).add(Money.of(new BigDecimal("250.00"), currency).multiply(weight));
        if (request.urgent()) {
            baseCost = baseCost.multiply(new BigDecimal("2.20"));
        }

        int minHours = request.urgent() ? 3 : 24;
        int maxHours = request.urgent() ? 8 : 72;

        return new CarrierQuoteResult(
                true,
                carrierCode != null ? carrierCode : "PARTNER_3PL",
                baseCost,
                minHours,
                maxHours
        );
    }

    @Override
    public CarrierBookingResult bookShipment(String carrierCode, CarrierBookingRequest request) {
        String effectiveCarrier = (carrierCode != null && !carrierCode.isBlank()) ? carrierCode : "GIG_LOGISTICS";
        String carrierRef = effectiveCarrier.toUpperCase() + "-" + System.currentTimeMillis();

        CurrencyCode currency = request.declaredValue() != null ? request.declaredValue().currency() : CurrencyCode.NGN;
        BigDecimal weight = request.weightKg() != null && request.weightKg().compareTo(BigDecimal.ZERO) > 0
                ? request.weightKg()
                : BigDecimal.ONE;

        Money actualCost = Money.of(new BigDecimal("1100.00"), currency).add(Money.of(new BigDecimal("220.00"), currency).multiply(weight));
        if (request.urgent()) {
            actualCost = actualCost.multiply(new BigDecimal("2.20"));
        }

        return CarrierBookingResult.success(effectiveCarrier, carrierRef, actualCost);
    }

    @Override
    public CarrierTrackingResult getTrackingStatus(String carrierCode, String carrierReference) {
        return new CarrierTrackingResult(
                carrierCode,
                carrierReference,
                "IN_TRANSIT",
                "Distribution Center Transit",
                ZonedDateTime.now()
        );
    }

    @Override
    public void cancelShipment(String carrierCode, String carrierReference) {
        // cancellation acknowledged by carrier adapter
    }
}
