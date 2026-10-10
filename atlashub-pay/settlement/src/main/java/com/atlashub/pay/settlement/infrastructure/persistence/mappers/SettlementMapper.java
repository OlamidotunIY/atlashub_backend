package com.atlashub.pay.settlement.infrastructure.persistence.mappers;

import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {ValueObjectMapper.class})
public interface SettlementMapper extends DomainMapper<Settlement, SettlementJpa> {
    @Override
    @Mapping(target = "grossAmount", source = "grossAmount.amount")
    @Mapping(target = "netAmount", source = "netAmount.amount")
    @Mapping(target = "providerFeeAmount", source = "providerFeeAmount.amount")
    @Mapping(target = "currency", source = "netAmount.currency")
    @Mapping(target = "version", ignore = true)
    SettlementJpa toPersistence(Settlement settlement);

    @Override
    @Mapping(target = "grossAmount", source = "grossAmount.amount")
    @Mapping(target = "netAmount", source = "netAmount.amount")
    @Mapping(target = "providerFeeAmount", source = "providerFeeAmount.amount")
    @Mapping(target = "currency", source = "netAmount.currency")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(Settlement settlement, @MappingTarget SettlementJpa persistence);

    @Override
    default Settlement toDomain(SettlementJpa persistence) {
        if (persistence == null) return null;
        return new Settlement(persistence.getId(), persistence.getOrganizationId(), persistence.getEnvironment(),
                persistence.getProvider(), persistence.getProviderSettlementId(),
                persistence.getProviderSubaccountCode(),
                new com.atlashub.shared.domain.valueobject.Money(persistence.getGrossAmount(),
                        persistence.getCurrency()),
                new com.atlashub.shared.domain.valueobject.Money(persistence.getNetAmount(), persistence.getCurrency()),
                new com.atlashub.shared.domain.valueobject.Money(persistence.getProviderFeeAmount(),
                        persistence.getCurrency()), persistence.getAnchorDepositAccountId(),
                persistence.getTransactionReferences(), persistence.getAnchorTransferReference(),
                persistence.getSettledAt(), persistence.getStatus(), persistence.getDescription(),
                persistence.getCreatedAt(), persistence.getUpdatedAt());
    }
}
