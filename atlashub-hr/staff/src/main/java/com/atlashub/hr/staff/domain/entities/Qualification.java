package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import lombok.Getter;

@Getter
public class Qualification {

    private final Long id;
    private final Long employeeId;
    private final String degree;
    private final String institution;
    private final Integer yearAwarded;

    public Qualification(Long id, Long employeeId, String degree, String institution, Integer yearAwarded) {
        this.id = id;
        this.employeeId = employeeId;
        this.degree = degree;
        this.institution = institution;
        this.yearAwarded = yearAwarded;
    }

    public static Qualification create(Long id, Long employeeId, String degree, String institution, Integer yearAwarded) {
        if (id == null) {
            throw new InvalidStaffOperationException("Qualification ID cannot be null");
        }
        if (employeeId == null) {
            throw new InvalidStaffOperationException("Employee ID cannot be null");
        }
        if (degree == null || degree.isBlank()) {
            throw new InvalidStaffOperationException("Degree cannot be null or blank");
        }
        if (institution == null || institution.isBlank()) {
            throw new InvalidStaffOperationException("Institution cannot be null or blank");
        }
        if (yearAwarded == null || yearAwarded < 1900) {
            throw new InvalidStaffOperationException("Valid year awarded is required");
        }

        return new Qualification(id, employeeId, degree, institution, yearAwarded);
    }
}
