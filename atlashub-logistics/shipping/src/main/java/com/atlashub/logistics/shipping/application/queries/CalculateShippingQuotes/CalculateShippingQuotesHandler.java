package com.atlashub.logistics.shipping.application.queries.CalculateShippingQuotes;

import com.atlashub.logistics.shipping.domain.entities.CoverageZone;
import com.atlashub.logistics.shipping.domain.entities.MerchantShippingConfig;
import com.atlashub.logistics.shipping.domain.repositories.CoverageZoneRepository;
import com.atlashub.logistics.shipping.domain.repositories.MerchantShippingConfigRepository;
import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CalculateShippingQuotesHandler extends Query<CalculateShippingQuotesQuery, ShippingQuoteResult> {

    private final CoverageZoneRepository coverageZoneRepository;
    private final MerchantShippingConfigRepository merchantConfigRepository;

    public CalculateShippingQuotesHandler(
            CoverageZoneRepository coverageZoneRepository,
            MerchantShippingConfigRepository merchantConfigRepository
    ) {
        this.coverageZoneRepository = coverageZoneRepository;
        this.merchantConfigRepository = merchantConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingQuoteResult execute(CalculateShippingQuotesQuery input) {
        boolean isIntraState = input.originAddress().state().trim().equalsIgnoreCase(input.destinationAddress().state().trim());
        RouteType routeType = isIntraState ? RouteType.INTRA_STATE : RouteType.INTER_STATE;

        CoverageZone zone = coverageZoneRepository.findByStateAndCity(
                input.destinationAddress().state(),
                input.destinationAddress().city()
        ).orElse(null);

        boolean merchantUrgentAllowed = merchantConfigRepository.findByOrganizationId(input.organizationId())
                .map(MerchantShippingConfig::isUrgentDeliveryEnabled)
                .orElse(true);

        ZonedDateTime now = ZonedDateTime.now();
        BigDecimal weight = input.weightKg() != null && input.weightKg().compareTo(BigDecimal.ZERO) > 0 ? input.weightKg() : BigDecimal.ONE;

        if (zone != null && zone.isActive()) {
            String channel = zone.isInternalFleetCovered()
                    ? CarrierChannel.INTERNAL_FLEET.name()
                    : CarrierChannel.PARTNER_3PL.name();

            Money standardFee = zone.calculateStandardFee(weight);
            int minStd = isIntraState ? zone.getIntraStateStandardMinHours() : zone.getInterStateStandardMinHours();
            int maxStd = isIntraState ? zone.getIntraStateStandardMaxHours() : zone.getInterStateStandardMaxHours();

            ZonedDateTime stdStart = now.plusHours(minStd);
            ZonedDateTime stdEnd = now.plusHours(maxStd);

            boolean urgentSupported = zone.isUrgentDeliverySupported() && merchantUrgentAllowed;
            Money urgentFee = urgentSupported ? zone.calculateUrgentFee(weight) : null;
            ZonedDateTime urgStart = urgentSupported ? now.plusHours(zone.getUrgentMinHours()) : null;
            ZonedDateTime urgEnd = urgentSupported ? now.plusHours(zone.getUrgentMaxHours()) : null;

            return new ShippingQuoteResult(
                    true,
                    channel,
                    routeType.name(),
                    standardFee,
                    stdStart,
                    stdEnd,
                    urgentSupported,
                    urgentFee,
                    urgStart,
                    urgEnd
            );
        }

        // Default baseline quote when unmapped
        com.atlashub.shared.domain.valueobject.CurrencyCode currency = input.declaredValue() != null ? input.declaredValue().currency() : com.atlashub.shared.domain.valueobject.CurrencyCode.NGN;
        Money standardFee = Money.of(new BigDecimal("1500.00"), currency).add(Money.of(new BigDecimal("300.00"), currency).multiply(weight));
        int minStd = isIntraState ? 12 : 48;
        int maxStd = isIntraState ? 36 : 96;

        return new ShippingQuoteResult(
                true,
                CarrierChannel.PARTNER_3PL.name(),
                routeType.name(),
                standardFee,
                now.plusHours(minStd),
                now.plusHours(maxStd),
                merchantUrgentAllowed,
                standardFee.multiply(new BigDecimal("2.50")),
                now.plusHours(2),
                now.plusHours(6)
        );
    }
}
