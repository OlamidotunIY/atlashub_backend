package com.atlashub.compliance.domain.valueobject;

import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;

public record ContactInfoData(
    EmailAddress generalEmail,
    EmailAddress supportEmail,
    EmailAddress disputeEmail,
    PhoneNumber phoneNumber,
    AddressData mainAddress,
    AddressData registeredAddress
) {
    public ContactInfoData {
        if (generalEmail == null || supportEmail == null || disputeEmail == null)
            throw new InvalidComplianceDataException("General, support, and dispute emails are required");
        if (phoneNumber == null) throw new InvalidComplianceDataException("Phone number is required");
        if (mainAddress == null || registeredAddress == null)
            throw new InvalidComplianceDataException("Main and registered addresses are required");
    }
}
