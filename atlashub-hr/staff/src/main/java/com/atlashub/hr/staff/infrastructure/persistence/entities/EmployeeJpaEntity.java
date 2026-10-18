package com.atlashub.hr.staff.infrastructure.persistence.entities;

import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
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
        name = "hr_employees",
        indexes = {
                @Index(name = "idx_employee_org_user", columnList = "organization_id, user_id", unique = true),
                @Index(name = "idx_employee_org_number", columnList = "organization_id, employee_number", unique = true),
                @Index(name = "idx_employee_org_status", columnList = "organization_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmployeeJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "employee_number", nullable = false, length = 32)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 32)
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(length = 255)
    private String address;

    @Column(length = 64)
    private String nationality;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(nullable = false, length = 128)
    private String designation;

    @Column(nullable = false, length = 64)
    private String department;

    @Column(name = "reporting_manager_id")
    private Long reportingManagerId;

    @Column(name = "salary_grade_id", nullable = false)
    private Long salaryGradeId;

    @Column(name = "base_salary", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseSalary;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
