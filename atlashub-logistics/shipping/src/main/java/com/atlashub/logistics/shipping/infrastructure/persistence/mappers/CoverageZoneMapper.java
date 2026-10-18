package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.atlashub.logistics.shipping.domain.entities.CoverageZone;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.CoverageZoneJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CoverageZoneMapper extends DomainMapper<CoverageZone, CoverageZoneJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "baseStandardFeeAmount", source = "baseStandardFee.amount")
    @Mapping(target = "baseStandardFeeCurrency", source = "baseStandardFee.currency")
    @Mapping(target = "perKgStandardFeeAmount", source = "perKgStandardFee.amount")
    @Mapping(target = "perKgStandardFeeCurrency", source = "perKgStandardFee.currency")
    CoverageZoneJpa toPersistence(CoverageZone domain);

    @Override
    default CoverageZone toDomain(CoverageZoneJpa jpa) {
        if (jpa == null) return null;
        CurrencyCode baseCur = (jpa.getBaseStandardFeeCurrency() != null && !jpa.getBaseStandardFeeCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getBaseStandardFeeCurrency())
                : CurrencyCode.NGN;
        Money baseFee = Money.of(jpa.getBaseStandardFeeAmount(), baseCur);

        CurrencyCode perKgCur = (jpa.getPerKgStandardFeeCurrency() != null && !jpa.getPerKgStandardFeeCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getPerKgStandardFeeCurrency())
                : CurrencyCode.NGN;
        Money perKgFee = Money.of(jpa.getPerKgStandardFeeAmount(), perKgCur);

        return new CoverageZone(
                jpa.getId(),
                jpa.getState(),
                jpa.getCity(),
                jpa.getZoneCode(),
                jpa.isInternalFleetCovered(),
                jpa.isPartner3PLCovered(),
                jpa.getPreferred3PLCarrierCode(),
                jpa.isUrgentDeliverySupported(),
                baseFee,
                perKgFee,
                jpa.getUrgentMultiplier(),
                jpa.getIntraStateStandardMinHours(),
                jpa.getIntraStateStandardMaxHours(),
                jpa.getInterStateStandardMinHours(),
                jpa.getInterStateStandardMaxHours(),
                jpa.getUrgentMinHours(),
                jpa.getUrgentMaxHours(),
                jpa.isActive(),
                jpa.getCreatedAt()
        );
    }
}
