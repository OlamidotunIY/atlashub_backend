package com.atlashub.hr.staff.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OnboardEmployeeRequest(
        Long organizationId,
        @NotNull(message = "User ID is required")
        Long userId,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Phone number is required")
        String phone,
        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,
        String address,
        String nationality,
        @NotNull(message = "Hire date is required")
        LocalDate hireDate,
        @NotBlank(message = "Designation is required")
        String designation,
        @NotBlank(message = "Department is required")
        String department,
        Long reportingManagerId,
        @NotNull(message = "Salary grade ID is required")
        Long salaryGradeId,
        @NotNull(message = "Base salary is required")
        @Positive(message = "Base salary must be positive")
        BigDecimal baseSalary,
        String currency
) {}
