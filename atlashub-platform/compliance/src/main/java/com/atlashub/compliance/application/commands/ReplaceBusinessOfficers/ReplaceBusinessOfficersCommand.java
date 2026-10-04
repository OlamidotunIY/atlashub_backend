package com.atlashub.compliance.application.commands.ReplaceBusinessOfficers;
import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record ReplaceBusinessOfficersCommand(Long organizationId, List<OfficerInput> officers) {
    public record OfficerInput(OfficerRole role, String firstName, String middleName, String lastName,
            String maidenName, String nationality, LocalDate dateOfBirth, EmailAddress email,
            PhoneNumber phoneNumber, AddressData address, String bvn, String title, BigDecimal percentageOwned) {}
}
