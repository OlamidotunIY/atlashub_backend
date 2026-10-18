package com.atlashub.shared.application.port;

import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface ThirdPartyCarrierPort {

    record CarrierQuoteRequest(
            String originState,
            String originCity,
            String destinationState,
            String destinationCity,
            BigDecimal weightKg,
            Money declaredValue,
            boolean urgent
    ) {}

    record CarrierQuoteResult(
            boolean supported,
            String carrierCode,
            Money quotedFee,
            int estimatedMinHours,
            int estimatedMaxHours
    ) {}

    record CarrierBookingRequest(
            String trackingNumber,
            String originAddressText,
            String originState,
            String originCity,
            String destinationAddressText,
            String destinationState,
            String destinationCity,
            BigDecimal weightKg,
            Money declaredValue,
            boolean urgent
    ) {}

    record CarrierBookingResult(
            boolean successful,
            String carrierCode,
            String carrierReference,
            Money actualCost,
            String status,
            String errorMessage
    ) {
        public static CarrierBookingResult success(String carrierCode, String carrierReference, Money actualCost) {
            return new CarrierBookingResult(true, carrierCode, carrierReference, actualCost, "BOOKED", null);
        }

        public static CarrierBookingResult failure(String carrierCode, String errorMessage) {
            return new CarrierBookingResult(false, carrierCode, null, null, "FAILED", errorMessage);
        }
    }

    record CarrierTrackingResult(
            String carrierCode,
            String carrierReference,
            String status,
            String currentLocation,
            ZonedDateTime timestamp
    ) {}

    CarrierQuoteResult quoteShipment(String carrierCode, CarrierQuoteRequest request);

    CarrierBookingResult bookShipment(String carrierCode, CarrierBookingRequest request);

    CarrierTrackingResult getTrackingStatus(String carrierCode, String carrierReference);

    void cancelShipment(String carrierCode, String carrierReference);
}
