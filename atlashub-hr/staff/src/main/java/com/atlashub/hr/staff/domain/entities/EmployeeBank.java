package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class EmployeeBank extends AggregateRoot<Long> {

    private final Long id;
    private final Long employeeId;
    private final String bankCode;
    private final String bankName;
    private final String accountNumber;
    private String accountName;
    private boolean isVerified;
    private boolean isPrimary;
    private final ZonedDateTime addedAt;

    public EmployeeBank(
            Long id,
            Long employeeId,
            String bankCode,
            String bankName,
            String accountNumber,
            String accountName,
            boolean isVerified,
            boolean isPrimary,
            ZonedDateTime addedAt
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.isVerified = isVerified;
        this.isPrimary = isPrimary;
        this.addedAt = addedAt;
    }

    public static EmployeeBank create(
            Long id,
            Long employeeId,
            String bankCode,
            String bankName,
            String accountNumber,
            String accountName,
            boolean isVerified,
            boolean isPrimary
    ) {
        if (id == null) {
            throw new InvalidStaffOperationException("Employee bank ID cannot be null");
        }
        if (employeeId == null) {
            throw new InvalidStaffOperationException("Employee ID cannot be null");
        }
        if (bankCode == null || bankCode.isBlank()) {
            throw new InvalidStaffOperationException("Bank code cannot be null or blank");
        }
        if (bankName == null || bankName.isBlank()) {
            throw new InvalidStaffOperationException("Bank name cannot be null or blank");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new InvalidStaffOperationException("Account number cannot be null or blank");
        }

        return new EmployeeBank(
                id,
                employeeId,
                bankCode,
                bankName,
                accountNumber,
                accountName,
                isVerified,
                isPrimary,
                ZonedDateTime.now()
        );
    }

    public void verify(String resolvedAccountName) {
        if (resolvedAccountName == null || resolvedAccountName.isBlank()) {
            throw new InvalidStaffOperationException("Resolved account name cannot be null or blank");
        }
        this.accountName = resolvedAccountName;
        this.isVerified = true;
    }

    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
    }

    @Override
    public Long getId() {
        return id;
    }
}
