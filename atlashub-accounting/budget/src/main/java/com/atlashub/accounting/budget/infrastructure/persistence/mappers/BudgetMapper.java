package com.atlashub.accounting.budget.infrastructure.persistence.mappers;

import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.infrastructure.persistence.entities.BudgetJpaEntity;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BudgetMapper extends DomainMapper<Budget, BudgetJpaEntity> {

    @Override
    @Mapping(target = "budgetedAmount", expression = "java(com.atlashub.shared.domain.valueobject.Money.of(record.getBudgetedAmount(), com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(record.getCurrency())))")
    Budget toDomain(BudgetJpaEntity record);

    @Override
    @Mapping(target = "budgetedAmount", source = "budgetedAmount.amount")
    @Mapping(target = "currency", source = "budgetedAmount.currency")
    @Mapping(target = "version", ignore = true)
    BudgetJpaEntity toPersistence(Budget domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "budgetedAmount", source = "budgetedAmount.amount")
    @Mapping(target = "currency", source = "budgetedAmount.currency")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(Budget domain, @MappingTarget BudgetJpaEntity record);
}
