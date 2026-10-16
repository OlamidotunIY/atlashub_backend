package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SalaryGrade extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private String name;
    private Money baseSalary;

    public SalaryGrade(
            Long id,
            Long organizationId,
            String name,
            Money baseSalary
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.baseSalary = baseSalary;
    }

    public static SalaryGrade create(
            Long id,
            Long organizationId,
            String name,
            Money baseSalary
    ) {
        if (id == null) {
            throw new InvalidStaffOperationException("Salary grade ID cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidStaffOperationException("Organization ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidStaffOperationException("Salary grade name cannot be null or blank");
        }
        if (baseSalary == null || baseSalary.amount() == null || baseSalary.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStaffOperationException("Base salary must be greater than zero");
        }

        return new SalaryGrade(id, organizationId, name, baseSalary);
    }

    public void updateBaseSalary(Money newBaseSalary) {
        if (newBaseSalary == null || newBaseSalary.amount() == null || newBaseSalary.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStaffOperationException("New base salary must be greater than zero");
        }
        this.baseSalary = newBaseSalary;
    }

    @Override
    public Long getId() {
        return id;
    }
}
