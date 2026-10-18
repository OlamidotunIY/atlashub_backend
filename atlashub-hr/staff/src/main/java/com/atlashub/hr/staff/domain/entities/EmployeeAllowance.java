package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.hr.staff.domain.valueobject.AllowanceType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class EmployeeAllowance extends AggregateRoot<Long> {

    private final Long id;
    private final Long employeeId;
    private final String name;
    private final AllowanceType type;
    private final BigDecimal value;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;

    public EmployeeAllowance(
            Long id,
            Long employeeId,
            String name,
            AllowanceType type,
            BigDecimal value,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
        this.type = type;
        this.value = value;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public static EmployeeAllowance create(
            Long id,
            Long employeeId,
            String name,
            AllowanceType type,
            BigDecimal value,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        if (id == null) {
            throw new InvalidStaffOperationException("Employee allowance ID cannot be null");
        }
        if (employeeId == null) {
            throw new InvalidStaffOperationException("Employee ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidStaffOperationException("Allowance name cannot be null or blank");
        }
        if (type == null) {
            throw new InvalidStaffOperationException("Allowance type cannot be null");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStaffOperationException("Allowance value must be greater than zero");
        }
        if (effectiveFrom == null) {
            throw new InvalidStaffOperationException("Effective from date cannot be null");
        }

        return new EmployeeAllowance(id, employeeId, name, type, value, effectiveFrom, effectiveTo);
    }

    @Override
    public Long getId() {
        return id;
    }
}
