package com.atlashub.compliance.domain.valueobject;

import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;

import java.time.LocalDate;

public record BusinessProfileData(
    String legalName,
    LegalRegistrationType registrationType,
    LocalDate registrationDate,
    String businessRegistrationNumber,
    String businessBvn,
    SupportedBusinessIndustry industry,
    String businessDescription,
    String website
) {
    public BusinessProfileData {
        requireText(legalName, "Legal name");
        if (registrationType == null) throw new InvalidComplianceDataException("Registration type is required");
        if (registrationDate == null || registrationDate.isAfter(LocalDate.now()))
            throw new InvalidComplianceDataException("Registration date must not be in the future");
        requireText(businessRegistrationNumber, "Business registration number");
        if (businessBvn == null || !businessBvn.matches("\\d{11}"))
            throw new InvalidComplianceDataException("Business BVN must contain 11 digits");
        if (industry == null) throw new InvalidComplianceDataException("Industry is required");
        requireText(businessDescription, "Business description");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new InvalidComplianceDataException(name + " is required");
    }
}
