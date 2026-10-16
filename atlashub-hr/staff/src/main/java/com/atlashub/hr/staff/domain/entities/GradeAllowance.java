package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.hr.staff.domain.valueobject.AllowanceType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GradeAllowance extends AggregateRoot<Long> {

    private final Long id;
    private final Long gradeId;
    private final String name;
    private final AllowanceType type;
    private final BigDecimal value;

    public GradeAllowance(Long id, Long gradeId, String name, AllowanceType type, BigDecimal value) {
        this.id = id;
        this.gradeId = gradeId;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public static GradeAllowance create(Long id, Long gradeId, String name, AllowanceType type, BigDecimal value) {
        if (id == null) {
            throw new InvalidStaffOperationException("Allowance ID cannot be null");
        }
        if (gradeId == null) {
            throw new InvalidStaffOperationException("Grade ID cannot be null");
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

        return new GradeAllowance(id, gradeId, name, type, value);
    }

    @Override
    public Long getId() {
        return id;
    }
}
