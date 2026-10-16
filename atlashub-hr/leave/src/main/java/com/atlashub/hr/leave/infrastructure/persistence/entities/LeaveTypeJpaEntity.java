package com.atlashub.hr.leave.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "hr_leave_types",
        indexes = {
                @Index(name = "idx_hr_leave_types_org_id", columnList = "organization_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LeaveTypeJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "max_days_per_year", nullable = false)
    private Integer maxDaysPerYear;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval;

    @Column(name = "carry_over_allowed", nullable = false)
    private Boolean carryOverAllowed;

    @Column(name = "max_carry_over_days", nullable = false)
    private Integer maxCarryOverDays;

    @Version
    private Long version;

    @Override
    public Long getId() {
        return id;
    }
}
