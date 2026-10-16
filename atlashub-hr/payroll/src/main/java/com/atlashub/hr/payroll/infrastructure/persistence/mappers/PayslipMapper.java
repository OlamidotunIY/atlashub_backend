package com.atlashub.hr.payroll.infrastructure.persistence.mappers;

import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.hr.payroll.infrastructure.persistence.entities.PayslipJpaEntity;
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
public interface PayslipMapper extends DomainMapper<Payslip, PayslipJpaEntity> {

    @Override
    @Mapping(target = "grossPay", expression = "java(record.getGrossPay() == null ? null : Money.of(record.getGrossPay(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "totalAllowances", expression = "java(record.getTotalAllowances() == null ? null : Money.of(record.getTotalAllowances(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "totalDeductions", expression = "java(record.getTotalDeductions() == null ? null : Money.of(record.getTotalDeductions(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "netPay", expression = "java(record.getNetPay() == null ? null : Money.of(record.getNetPay(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "breakdown", ignore = true)
    Payslip toDomain(PayslipJpaEntity record);

    @Override
    @Mapping(target = "grossPay", expression = "java(domain.getGrossPay() == null ? null : domain.getGrossPay().amount())")
    @Mapping(target = "totalAllowances", expression = "java(domain.getTotalAllowances() == null ? null : domain.getTotalAllowances().amount())")
    @Mapping(target = "totalDeductions", expression = "java(domain.getTotalDeductions() == null ? null : domain.getTotalDeductions().amount())")
    @Mapping(target = "netPay", expression = "java(domain.getNetPay() == null ? null : domain.getNetPay().amount())")
    @Mapping(target = "currency", expression = "java(domain.getGrossPay() == null ? \"NGN\" : domain.getGrossPay().currency().name())")
    @Mapping(target = "version", ignore = true)
    PayslipJpaEntity toPersistence(Payslip domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "grossPay", expression = "java(domain.getGrossPay() == null ? null : domain.getGrossPay().amount())")
    @Mapping(target = "totalAllowances", expression = "java(domain.getTotalAllowances() == null ? null : domain.getTotalAllowances().amount())")
    @Mapping(target = "totalDeductions", expression = "java(domain.getTotalDeductions() == null ? null : domain.getTotalDeductions().amount())")
    @Mapping(target = "netPay", expression = "java(domain.getNetPay() == null ? null : domain.getNetPay().amount())")
    @Mapping(target = "currency", expression = "java(domain.getGrossPay() == null ? \"NGN\" : domain.getGrossPay().currency().name())")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(Payslip domain, @MappingTarget PayslipJpaEntity record);
}
