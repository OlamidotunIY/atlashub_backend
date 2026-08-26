package com.atlashub.identity.domain.model;

import com.atlashub.identity.domain.event.OrganizationComplianceStepCompleted;
import com.atlashub.identity.domain.event.OrganizationComplianceSubmitted;
import com.atlashub.identity.domain.event.OrganizationRegistered;
import com.atlashub.identity.domain.model.BusinessType;
import com.atlashub.identity.domain.model.ComplianceStatus;
import com.atlashub.identity.domain.model.GovernmentIdType;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.model.StaffSize;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationTest {

    @Test
    void shouldRegisterSuccessfully() {
        Organization Organization = new Organization(1L, Country.NIGERIA, "Test Inc", "John", "Doe", new EmailAddress("test@atlaspay.com"), new PhoneNumber("+2348000000000"), BusinessType.REGISTERED);
        
        assertEquals(ComplianceStatus.NOT_STARTED, Organization.getComplianceStatus());
        assertEquals("Test Inc", Organization.getBusinessName());
        assertEquals(1, Organization.pullDomainEvents().size());
    }

    @Test
    void shouldCompleteComplianceStep() {
        Organization Organization = new Organization(1L, Country.NIGERIA, "Test Inc", "John", "Doe", new EmailAddress("test@atlaspay.com"), new PhoneNumber("+2348000000000"), BusinessType.REGISTERED);
        Organization.pullDomainEvents(); // clear initial events
        
        Organization.updateComplianceProfile("Desc", StaffSize.ONE_TO_TEN, "IT", "Tech", java.math.BigDecimal.valueOf(1000), "NGN");
        assertEquals(1, Organization.pullDomainEvents().size());
        
        Organization.updateComplianceContact(new EmailAddress("support@test.com"), new EmailAddress("dispute@test.com"), null, null, null, null, null, null, "Lagos", "Ikeja", "Ikeja", "Street 1");
        Organization.updateComplianceOwner("12345678901", "12345678901", java.time.LocalDate.now(), "Address", GovernmentIdType.NIN_SLIP, "12345", "RC123");
        Organization.updateComplianceAccount("035", "1234567890", "Test Inc");
        
        // Final step should manually trigger submission
        Organization.acceptServiceAgreement();
        Organization.submitCompliance();
        
        assertEquals(ComplianceStatus.SUBMITTED, Organization.getComplianceStatus());
        
        var events = Organization.pullDomainEvents();
        assertTrue(events.stream().anyMatch(e -> e instanceof OrganizationComplianceSubmitted));
    }
}

