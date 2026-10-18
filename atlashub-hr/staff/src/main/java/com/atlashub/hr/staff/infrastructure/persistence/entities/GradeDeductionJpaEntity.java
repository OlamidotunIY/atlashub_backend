package com.atlashub.hr.staff.infrastructure.persistence.entities;

import com.atlashub.hr.staff.domain.valueobject.DeductionType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "hr_grade_deductions",
        indexes = {
                @Index(name = "idx_deduction_grade_id", columnList = "grade_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GradeDeductionJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeductionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    @Column(name = "is_statutory", nullable = false)
    private boolean isStatutory;

    @Version
    private Long version;
}
