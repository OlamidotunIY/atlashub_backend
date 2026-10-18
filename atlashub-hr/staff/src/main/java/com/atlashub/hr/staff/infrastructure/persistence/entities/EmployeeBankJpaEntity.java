package com.atlashub.hr.staff.infrastructure.persistence.entities;

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

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "hr_employee_banks",
        indexes = {
                @Index(name = "idx_emp_bank_emp_id", columnList = "employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmployeeBankJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "bank_code", nullable = false, length = 32)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 128)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 32)
    private String accountNumber;

    @Column(name = "account_name", length = 128)
    private String accountName;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "added_at", nullable = false)
    private ZonedDateTime addedAt;

    @Version
    private Long version;
}
