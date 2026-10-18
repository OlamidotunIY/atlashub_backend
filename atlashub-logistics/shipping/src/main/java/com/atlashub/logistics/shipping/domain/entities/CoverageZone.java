package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class CoverageZone extends AggregateRoot<Long> {

    private final Long id;
    private final String state;
    private final String city;
    private final String zoneCode;
    private boolean internalFleetCovered;
    private boolean partner3PLCovered;
    private String preferred3PLCarrierCode;
    private boolean urgentDeliverySupported;
    private Money baseStandardFee;
    private Money perKgStandardFee;
    private BigDecimal urgentMultiplier;
    private int intraStateStandardMinHours;
    private int intraStateStandardMaxHours;
    private int interStateStandardMinHours;
    private int interStateStandardMaxHours;
    private int urgentMinHours;
    private int urgentMaxHours;
    private boolean active;
    private final ZonedDateTime createdAt;

    public CoverageZone(
            Long id,
            String state,
            String city,
            String zoneCode,
            boolean internalFleetCovered,
            boolean partner3PLCovered,
            String preferred3PLCarrierCode,
            boolean urgentDeliverySupported,
            Money baseStandardFee,
            Money perKgStandardFee,
            BigDecimal urgentMultiplier,
            int intraStateStandardMinHours,
            int intraStateStandardMaxHours,
            int interStateStandardMinHours,
            int interStateStandardMaxHours,
            int urgentMinHours,
            int urgentMaxHours,
            boolean active,
            ZonedDateTime createdAt
    ) {
        this.id = id;
        this.state = state;
        this.city = city;
        this.zoneCode = zoneCode;
        this.internalFleetCovered = internalFleetCovered;
        this.partner3PLCovered = partner3PLCovered;
        this.preferred3PLCarrierCode = preferred3PLCarrierCode;
        this.urgentDeliverySupported = urgentDeliverySupported;
        this.baseStandardFee = baseStandardFee;
        this.perKgStandardFee = perKgStandardFee;
        this.urgentMultiplier = urgentMultiplier;
        this.intraStateStandardMinHours = intraStateStandardMinHours;
        this.intraStateStandardMaxHours = intraStateStandardMaxHours;
        this.interStateStandardMinHours = interStateStandardMinHours;
        this.interStateStandardMaxHours = interStateStandardMaxHours;
        this.urgentMinHours = urgentMinHours;
        this.urgentMaxHours = urgentMaxHours;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static CoverageZone create(
            Long id,
            String state,
            String city,
            String zoneCode,
            boolean internalFleetCovered,
            boolean partner3PLCovered,
            String preferred3PLCarrierCode,
            boolean urgentDeliverySupported,
            Money baseStandardFee,
            Money perKgStandardFee,
            BigDecimal urgentMultiplier,
            int intraStateStandardMinHours,
            int intraStateStandardMaxHours,
            int interStateStandardMinHours,
            int interStateStandardMaxHours,
            int urgentMinHours,
            int urgentMaxHours
    ) {
        return new CoverageZone(
                id,
                state,
                city,
                zoneCode,
                internalFleetCovered,
                partner3PLCovered,
                preferred3PLCarrierCode,
                urgentDeliverySupported,
                baseStandardFee,
                perKgStandardFee,
                urgentMultiplier != null ? urgentMultiplier : new BigDecimal("2.50"),
                intraStateStandardMinHours > 0 ? intraStateStandardMinHours : 12,
                intraStateStandardMaxHours > 0 ? intraStateStandardMaxHours : 36,
                interStateStandardMinHours > 0 ? interStateStandardMinHours : 48,
                interStateStandardMaxHours > 0 ? interStateStandardMaxHours : 96,
                urgentMinHours > 0 ? urgentMinHours : 2,
                urgentMaxHours > 0 ? urgentMaxHours : 6,
                true,
                ZonedDateTime.now()
        );
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public Money calculateStandardFee(BigDecimal weightKg) {
        BigDecimal weight = weightKg != null && weightKg.compareTo(BigDecimal.ZERO) > 0 ? weightKg : BigDecimal.ONE;
        return baseStandardFee.add(perKgStandardFee.multiply(weight));
    }

    public Money calculateUrgentFee(BigDecimal weightKg) {
        return calculateStandardFee(weightKg).multiply(urgentMultiplier);
    }

    @Override
    public Long getId() {
        return id;
    }
}
