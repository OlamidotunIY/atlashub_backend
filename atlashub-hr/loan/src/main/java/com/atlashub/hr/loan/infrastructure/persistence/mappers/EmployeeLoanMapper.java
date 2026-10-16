package com.atlashub.hr.loan.infrastructure.persistence.mappers;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.infrastructure.persistence.entities.EmployeeLoanJpaEntity;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class},
        imports = {Money.class, CurrencyCode.class}
)
public interface EmployeeLoanMapper extends DomainMapper<EmployeeLoan, EmployeeLoanJpaEntity> {

    @Override
    @Mapping(target = "principalAmount", expression = "java(record.getPrincipalAmount() == null ? null : Money.of(record.getPrincipalAmount(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "outstandingBalance", expression = "java(record.getOutstandingBalance() == null ? null : Money.of(record.getOutstandingBalance(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "monthlyDeduction", expression = "java(record.getMonthlyDeduction() == null ? null : Money.of(record.getMonthlyDeduction(), CurrencyCode.valueOf(record.getCurrency())))")
    EmployeeLoan toDomain(EmployeeLoanJpaEntity record);

    @Override
    @Mapping(target = "principalAmount", source = "principalAmount.amount")
    @Mapping(target = "outstandingBalance", source = "outstandingBalance.amount")
    @Mapping(target = "monthlyDeduction", source = "monthlyDeduction.amount")
    @Mapping(target = "currency", expression = "java(domain.getPrincipalAmount() == null ? null : domain.getPrincipalAmount().currency().name())")
    @Mapping(target = "version", ignore = true)
    EmployeeLoanJpaEntity toPersistence(EmployeeLoan domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "principalAmount", source = "principalAmount.amount")
    @Mapping(target = "outstandingBalance", source = "outstandingBalance.amount")
    @Mapping(target = "monthlyDeduction", source = "monthlyDeduction.amount")
    @Mapping(target = "currency", expression = "java(domain.getPrincipalAmount() == null ? null : domain.getPrincipalAmount().currency().name())")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(EmployeeLoan domain, @MappingTarget EmployeeLoanJpaEntity record);
}
