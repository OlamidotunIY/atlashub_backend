package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.hr.staff.domain.valueobject.DeductionType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GradeDeduction extends AggregateRoot<Long> {

    private final Long id;
    private final Long gradeId;
    private final String name;
    private final DeductionType type;
    private final BigDecimal value;
    private final boolean isStatutory;

    public GradeDeduction(Long id, Long gradeId, String name, DeductionType type, BigDecimal value, boolean isStatutory) {
        this.id = id;
        this.gradeId = gradeId;
        this.name = name;
        this.type = type;
        this.value = value;
        this.isStatutory = isStatutory;
    }

    public static GradeDeduction create(Long id, Long gradeId, String name, DeductionType type, BigDecimal value, boolean isStatutory) {
        if (id == null) {
            throw new InvalidStaffOperationException("Deduction ID cannot be null");
        }
        if (gradeId == null) {
            throw new InvalidStaffOperationException("Grade ID cannot be null");
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

        return new GradeDeduction(id, gradeId, name, type, value, isStatutory);
    }

    @Override
    public Long getId() {
        return id;
    }
}
