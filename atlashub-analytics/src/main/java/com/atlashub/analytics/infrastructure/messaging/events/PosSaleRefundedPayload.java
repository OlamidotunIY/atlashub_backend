package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosSaleRefundedPayload(
        String eventId,
        Long organizationId,
        String environment,
        Long outletId,
        Long customerId,
        Long productId,
        int quantity,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal netAmount,
        String paymentMethod,
        LocalDate refundDate
) {}
