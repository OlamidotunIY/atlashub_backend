package com.atlashub.logistics.shipping.application.queries.CalculateShippingQuotes;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record ShippingQuoteResult(
        boolean covered,
        String fulfillmentChannel,
        String routeType,
        Money standardFee,
        ZonedDateTime standardWindowStart,
        ZonedDateTime standardWindowEnd,
        boolean urgentSupported,
        Money urgentFee,
        ZonedDateTime urgentWindowStart,
        ZonedDateTime urgentWindowEnd
) {}
