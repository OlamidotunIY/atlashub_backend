package com.atlashub.compliance.presentation.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record BusinessOfficerRequest(@NotBlank String role, @NotBlank String firstName, String middleName,
        @NotBlank String lastName, String maidenName, @NotBlank String nationality, @NotNull LocalDate dateOfBirth,
        @NotBlank @Email String email, @NotBlank String phoneNumber, @NotNull @Valid AddressRequest address,
        @NotBlank String bvn, @NotBlank String title, BigDecimal percentageOwned) {}
