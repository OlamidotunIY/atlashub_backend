package com.atlashub.compliance.domain.entities;

import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;
import com.atlashub.compliance.domain.valueobject.AddressData;
import com.atlashub.compliance.domain.valueobject.OfficerRole;
import com.atlashub.compliance.domain.valueobject.OfficerVerificationStatus;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public final class BusinessOfficer {
    private final Long id;
    private final OfficerRole role;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String maidenName;
    private final String nationality;
    private final LocalDate dateOfBirth;
    private final EmailAddress email;
    private final PhoneNumber phoneNumber;
    private final AddressData residentialAddress;
    private final String bvn;
    private final String title;
    private final BigDecimal percentageOwned;
    private String anchorOfficerId;
    private OfficerVerificationStatus verificationStatus;

    public BusinessOfficer(Long id, OfficerRole role, String firstName, String middleName, String lastName,
                           String maidenName, String nationality, LocalDate dateOfBirth, EmailAddress email,
                           PhoneNumber phoneNumber, AddressData residentialAddress, String bvn, String title,
                           BigDecimal percentageOwned, String anchorOfficerId,
                           OfficerVerificationStatus verificationStatus) {
        if (id == null) throw new InvalidComplianceDataException("Officer id is required");
        if (role == null) throw new InvalidComplianceDataException("Officer role is required");
        requireText(firstName, "Officer first name"); requireText(lastName, "Officer last name");
        requireText(nationality, "Officer nationality"); requireText(title, "Officer title");
        if (dateOfBirth == null || !dateOfBirth.isBefore(LocalDate.now()))
            throw new InvalidComplianceDataException("Officer date of birth is invalid");
        if (email == null || phoneNumber == null || residentialAddress == null)
            throw new InvalidComplianceDataException("Officer contact and address are required");
        if (bvn == null || !bvn.matches("\\d{11}"))
            throw new InvalidComplianceDataException("Officer BVN must contain 11 digits");
        if (role == OfficerRole.OWNER && (percentageOwned == null || percentageOwned.signum() < 0
                || percentageOwned.compareTo(BigDecimal.valueOf(100)) > 0))
            throw new InvalidComplianceDataException("Owner percentage must be between 0 and 100");
        if (role == OfficerRole.DIRECTOR && percentageOwned != null)
            throw new InvalidComplianceDataException("Percentage owned applies only to owners");
        this.id = id; this.role = role; this.firstName = firstName; this.middleName = middleName;
        this.lastName = lastName; this.maidenName = maidenName; this.nationality = nationality;
        this.dateOfBirth = dateOfBirth; this.email = email; this.phoneNumber = phoneNumber;
        this.residentialAddress = residentialAddress; this.bvn = bvn; this.title = title;
        this.percentageOwned = percentageOwned; this.anchorOfficerId = anchorOfficerId;
        this.verificationStatus = verificationStatus == null ? OfficerVerificationStatus.NOT_SUBMITTED : verificationStatus;
    }

    public void mapToAnchor(String officerId) {
        requireText(officerId, "Anchor officer id");
        if (anchorOfficerId != null && !anchorOfficerId.equals(officerId))
            throw new InvalidComplianceDataException("Officer is already mapped to Anchor");
        anchorOfficerId = officerId;
        verificationStatus = OfficerVerificationStatus.SUBMITTED;
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new InvalidComplianceDataException(name + " is required");
    }
}
