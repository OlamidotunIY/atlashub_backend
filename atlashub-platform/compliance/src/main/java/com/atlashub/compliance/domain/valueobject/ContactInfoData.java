package com.atlashub.compliance.domain.valueobject;

import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.domain.exception.ValidationException;

public record ContactInfoData(
    EmailAddress supportEmail,
    EmailAddress disputeEmail,
    PhoneNumber whatsappNumber,
    AddressData physicalAddress
) {
    public ContactInfoData {
        if (supportEmail == null) {
            throw new ValidationException("supportEmail cannot be null");
        }
        if (disputeEmail == null) {
            throw new ValidationException("disputeEmail cannot be null");
        }
        if (whatsappNumber == null) {
            throw new ValidationException("whatsappNumber cannot be null");
        }
        if (physicalAddress == null) {
            throw new ValidationException("physicalAddress cannot be null");
        }
    }
}
