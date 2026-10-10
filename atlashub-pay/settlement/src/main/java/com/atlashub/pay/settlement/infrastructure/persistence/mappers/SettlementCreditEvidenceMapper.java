package com.atlashub.pay.settlement.infrastructure.persistence.mappers;

import com.atlashub.pay.settlement.domain.entities.SettlementCreditEvidence;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementCreditEvidenceJpa;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SettlementCreditEvidenceMapper extends
        DomainMapper<SettlementCreditEvidence, SettlementCreditEvidenceJpa> {
    @Override
    @Mapping(target = "amount", source = "receivedAmount.amount")
    @Mapping(target = "currency", source = "receivedAmount.currency")
    @Mapping(target = "version", ignore = true)
    SettlementCreditEvidenceJpa toPersistence(SettlementCreditEvidence domain);

    @Override
    @Mapping(target = "amount", source = "receivedAmount.amount")
    @Mapping(target = "currency", source = "receivedAmount.currency")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(SettlementCreditEvidence domain, @MappingTarget SettlementCreditEvidenceJpa persistence);

    @Override
    default SettlementCreditEvidence toDomain(SettlementCreditEvidenceJpa persistence) {
        if (persistence == null) return null;
        return new SettlementCreditEvidence(persistence.getId(), persistence.getOrganizationId(),
                persistence.getEnvironment(), persistence.getAnchorDepositAccountId(),
                persistence.getAnchorTransferReference(), new Money(persistence.getAmount(), persistence.getCurrency()),
                persistence.getReceivedAt(), persistence.getMatchedSettlementId(), persistence.getCreatedAt());
    }
}
