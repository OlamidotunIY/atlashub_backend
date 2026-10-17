package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingConfig;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.MerchantShippingConfigJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MerchantShippingConfigMapper extends DomainMapper<MerchantShippingConfig, MerchantShippingConfigJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "defaultStreet", source = "defaultPickupAddress.street")
    @Mapping(target = "defaultCity", source = "defaultPickupAddress.city")
    @Mapping(target = "defaultState", source = "defaultPickupAddress.state")
    @Mapping(target = "defaultCountry", source = "defaultPickupAddress.country")
    @Mapping(target = "defaultPostalCode", source = "defaultPickupAddress.postalCode")
    MerchantShippingConfigJpa toPersistence(MerchantShippingConfig domain);

    @Override
    default MerchantShippingConfig toDomain(MerchantShippingConfigJpa jpa) {
        if (jpa == null) return null;
        Address pickup = (jpa.getDefaultCity() != null || jpa.getDefaultState() != null)
                ? new Address(jpa.getDefaultStreet(), jpa.getDefaultCity(), jpa.getDefaultState(), jpa.getDefaultCountry(), jpa.getDefaultPostalCode())
                : null;

        return new MerchantShippingConfig(
                jpa.getId(),
                jpa.getOrganizationId(),
                jpa.isUrgentDeliveryEnabled(),
                pickup,
                jpa.getUpdatedAt()
        );
    }
}
