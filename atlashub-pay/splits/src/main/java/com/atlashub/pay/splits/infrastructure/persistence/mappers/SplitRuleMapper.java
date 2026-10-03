package com.atlashub.pay.splits.infrastructure.persistence.mappers;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.entities.SplitSubaccount;
import com.atlashub.pay.splits.infrastructure.persistence.entities.SplitRuleJpa;
import com.atlashub.pay.splits.infrastructure.persistence.entities.SplitSubaccountJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = {ValueObjectMapper.class}
)
public interface SplitRuleMapper extends DomainMapper<SplitRule, SplitRuleJpa> {

    @Override
    SplitRule toDomain(SplitRuleJpa jpa);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", source = "active")
    @Override
    SplitRuleJpa toPersistence(SplitRule domain);

    @Mapping(target = "splitRuleId", ignore = true)
    SplitSubaccount toSubaccountDomain(SplitSubaccountJpa jpa);

    @Mapping(target = "splitRuleId", ignore = true)
    SplitSubaccountJpa toSubaccountPersistence(SplitSubaccount domain);
}
