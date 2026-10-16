package com.atlashub.hr.attendance.infrastructure.persistence.entities;

import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "hr_attendance_records",
        indexes = {
                @Index(name = "idx_attendance_emp_date", columnList = "employee_id, date", unique = true),
                @Index(name = "idx_attendance_org_emp_date", columnList = "organization_id, employee_id, date")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AttendanceRecordJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "clock_in")
    private ZonedDateTime clockIn;

    @Column(name = "clock_out")
    private ZonedDateTime clockOut;

    @Column(name = "hours_worked", precision = 5, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Version
    private Long version;

    @Override
    public Long getId() {
        return id;
    }
}
