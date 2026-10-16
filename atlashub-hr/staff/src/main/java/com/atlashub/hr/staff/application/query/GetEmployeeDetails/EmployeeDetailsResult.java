package com.atlashub.hr.staff.application.query.GetEmployeeDetails;

import com.atlashub.hr.staff.domain.valueobject.AllowanceType;
import com.atlashub.hr.staff.domain.valueobject.DeductionType;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeDetailsResult(
        Long id,
        Long organizationId,
        Long userId,
        String employeeNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String address,
        String nationality,
        LocalDate hireDate,
        LocalDate terminationDate,
        String designation,
        String department,
        Long reportingManagerId,
        Long salaryGradeId,
        BigDecimal baseSalary,
        String currency,
        EmployeeStatus status,
        List<BankDetail> banks,
        List<AllowanceDetail> allowances,
        List<DeductionDetail> deductions
) {
    public record BankDetail(
            Long id,
            String bankCode,
            String bankName,
            String accountNumber,
            String accountName,
            boolean isVerified,
            boolean isPrimary
    ) {}

    public record AllowanceDetail(
            Long id,
            String name,
            AllowanceType type,
            BigDecimal value,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {}

    public record DeductionDetail(
            Long id,
            String name,
            DeductionType type,
            BigDecimal value,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Long loanId
    ) {}
}
