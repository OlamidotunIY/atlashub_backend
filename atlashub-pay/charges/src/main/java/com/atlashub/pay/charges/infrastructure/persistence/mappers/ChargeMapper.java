package com.atlashub.pay.charges.infrastructure.persistence.mappers;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.infrastructure.persistence.entities.ChargeJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ChargeMapper extends DomainMapper<Charge, ChargeJpa> {
    @Override
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    @Mapping(target = "providerFee", source = "providerFee.amount")
    @Mapping(target = "version", ignore = true)
    ChargeJpa toPersistence(Charge domain);

    @Override
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    @Mapping(target = "providerFee", source = "providerFee.amount")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(Charge domain, @MappingTarget ChargeJpa persistence);

    @Override
    default Charge toDomain(ChargeJpa persistence) {
        if (persistence == null) {
            return null;
        }
        return new Charge(
                persistence.getId(), persistence.getOrganizationId(), persistence.getEnvironment(),
                persistence.getReference(),
                new com.atlashub.shared.domain.valueobject.Money(persistence.getAmount(), persistence.getCurrency()),
                persistence.getChannel(), persistence.getProvider(), persistence.getProviderProfileId(),
                persistence.getProviderReference(), persistence.getSourceSystem(), persistence.getSourceReferenceId(),
                persistence.getStatus(), persistence.getAuthorizationUrl(), persistence.getAccessCode(),
                persistence.getFailureMessage(), persistence.getProviderRefundReference(), persistence.getRefundReason(),
                persistence.getRefundedAt(), persistence.getCustomerReferenceId(),
                persistence.getProviderFee() == null ? null : new com.atlashub.shared.domain.valueobject.Money(
                        persistence.getProviderFee(), persistence.getCurrency()),
                persistence.getDisputeReference(), persistence.getDisputeStatus(), persistence.getDisputeReason(),
                persistence.getSuccessfulAt(), persistence.getExpiresAt(),
                persistence.getCreatedAt(), persistence.getUpdatedAt());
    }
}
