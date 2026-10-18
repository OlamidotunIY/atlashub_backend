package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.hr.staff.domain.valueobject.DeductionType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class EmployeeDeduction extends AggregateRoot<Long> {

    private final Long id;
    private final Long employeeId;
    private final String name;
    private final DeductionType type;
    private final BigDecimal value;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;
    private final Long loanId;

    public EmployeeDeduction(
            Long id,
            Long employeeId,
            String name,
            DeductionType type,
            BigDecimal value,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Long loanId
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
        this.type = type;
        this.value = value;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.loanId = loanId;
    }

    public static EmployeeDeduction create(
            Long id,
            Long employeeId,
            String name,
            DeductionType type,
            BigDecimal value,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Long loanId
    ) {
        if (id == null) {
            throw new InvalidStaffOperationException("Employee deduction ID cannot be null");
        }
        if (employeeId == null) {
            throw new InvalidStaffOperationException("Employee ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidStaffOperationException("Deduction name cannot be null or blank");
        }
        if (type == null) {
            throw new InvalidStaffOperationException("Deduction type cannot be null");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStaffOperationException("Deduction value must be greater than zero");
        }
        if (effectiveFrom == null) {
            throw new InvalidStaffOperationException("Effective from date cannot be null");
        }

        return new EmployeeDeduction(id, employeeId, name, type, value, effectiveFrom, effectiveTo, loanId);
    }

    @Override
    public Long getId() {
        return id;
    }
}
