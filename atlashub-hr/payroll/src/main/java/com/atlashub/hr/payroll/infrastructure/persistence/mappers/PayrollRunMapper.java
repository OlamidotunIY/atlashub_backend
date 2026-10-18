package com.atlashub.hr.payroll.infrastructure.persistence.mappers;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.infrastructure.persistence.entities.PayrollRunJpaEntity;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.YearMonth;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class},
        imports = {Money.class, CurrencyCode.class, YearMonth.class}
)
public interface PayrollRunMapper extends DomainMapper<PayrollRun, PayrollRunJpaEntity> {

    @Override
    @Mapping(target = "period", expression = "java(record.getPeriod() == null ? null : YearMonth.parse(record.getPeriod()))")
    @Mapping(target = "totalGross", expression = "java(record.getTotalGross() == null ? null : Money.of(record.getTotalGross(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "totalDeductions", expression = "java(record.getTotalDeductions() == null ? null : Money.of(record.getTotalDeductions(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "totalNet", expression = "java(record.getTotalNet() == null ? null : Money.of(record.getTotalNet(), CurrencyCode.valueOf(record.getCurrency())))")
    @Mapping(target = "payslips", ignore = true)
    PayrollRun toDomain(PayrollRunJpaEntity record);

    @Override
    @Mapping(target = "period", expression = "java(domain.getPeriod() == null ? null : domain.getPeriod().toString())")
    @Mapping(target = "totalGross", expression = "java(domain.getTotalGross() == null ? null : domain.getTotalGross().amount())")
    @Mapping(target = "totalDeductions", expression = "java(domain.getTotalDeductions() == null ? null : domain.getTotalDeductions().amount())")
    @Mapping(target = "totalNet", expression = "java(domain.getTotalNet() == null ? null : domain.getTotalNet().amount())")
    @Mapping(target = "currency", expression = "java(domain.getTotalGross() == null ? \"NGN\" : domain.getTotalGross().currency().name())")
    @Mapping(target = "version", ignore = true)
    PayrollRunJpaEntity toPersistence(PayrollRun domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "period", expression = "java(domain.getPeriod() == null ? null : domain.getPeriod().toString())")
    @Mapping(target = "totalGross", expression = "java(domain.getTotalGross() == null ? null : domain.getTotalGross().amount())")
    @Mapping(target = "totalDeductions", expression = "java(domain.getTotalDeductions() == null ? null : domain.getTotalDeductions().amount())")
    @Mapping(target = "totalNet", expression = "java(domain.getTotalNet() == null ? null : domain.getTotalNet().amount())")
    @Mapping(target = "currency", expression = "java(domain.getTotalGross() == null ? \"NGN\" : domain.getTotalGross().currency().name())")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(PayrollRun domain, @MappingTarget PayrollRunJpaEntity record);
}
