package com.atlashub.compliance.domain.entities;

import com.atlashub.compliance.domain.events.OrganizationComplianceApprovedEvent;
import com.atlashub.compliance.domain.exception.StepNotCompleteException;
import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComplianceRecordTest {
    @Test
    void does_not_create_test_banking_when_compliance_is_submitted() {
        ComplianceRecord record = readyRecord();
        record.submit();
        assertFalse(record.peekDomainEvents().stream().anyMatch(event ->
                event.getClass().getSimpleName().contains("TestBanking")));
    }

    @Test
    void requires_all_five_semantically_complete_steps_before_submission() {
        ComplianceRecord record = ComplianceRecord.create(1L, 10L);
        record.updateBusinessRegistration(profile(), true);
        assertThrows(StepNotCompleteException.class, record::submit);
    }

    @Test
    void completes_five_steps_and_approves_only_after_anchor_customer_exists() {
        ComplianceRecord record = readyRecord();
        record.submit();
        record.recordAnchorCustomerCreated("anchor-customer", Map.of(20L, "anchor-officer"));
        record.recordVerificationTriggered();
        record.recordAnchorApproved();

        assertEquals(ComplianceStatus.APPROVED, record.getStatus());
        assertEquals(AnchorVerificationStatus.APPROVED, record.getAnchorVerificationStatus());
        assertTrue(record.peekDomainEvents().stream().anyMatch(OrganizationComplianceApprovedEvent.class::isInstance));
    }

    private static ComplianceRecord readyRecord() {
        ComplianceRecord record = ComplianceRecord.create(1L, 10L);
        record.updateBusinessRegistration(profile(), true);
        record.updateContactAndAddresses(contact());
        record.replaceOfficers(List.of(officer()));
        record.recordRequiredDocuments(List.of(new ComplianceDocumentRequirement(30L, null,
                "CERTIFICATE_OF_INCORPORATION", "Certificate", true, RequirementSource.PREFLIGHT,
                null, null, null, null, null, null)));
        record.saveComplianceDocument(30L, "compliance/10/certificate.pdf", null);
        record.acceptServiceAgreement("127.0.0.1", ZonedDateTime.now(), "v1");
        return record;
    }

    private static BusinessProfileData profile() { return new BusinessProfileData("Tolu Store", LegalRegistrationType.PRIVATE_LIMITED_COMPANY, LocalDate.of(2024, 1, 1), "RC123", "22222222226", SupportedBusinessIndustry.RETAIL, "Retail store", null); }
    private static ContactInfoData contact() { AddressData address = new AddressData("1 Main Street", null, "Ikeja", "Lagos", "100001", "NG"); return new ContactInfoData(new EmailAddress("general@example.com"), new EmailAddress("support@example.com"), new EmailAddress("dispute@example.com"), new PhoneNumber("+2348012345678"), address, address); }
    private static BusinessOfficer officer() { return new BusinessOfficer(20L, OfficerRole.OWNER, "Tolu", null, "Owner", null, "NG", LocalDate.of(1990, 1, 1), new EmailAddress("owner@example.com"), new PhoneNumber("+2348012345678"), new AddressData("1 Main Street", null, "Ikeja", "Lagos", "100001", "NG"), "22222222226", "CEO", BigDecimal.valueOf(100), null, null); }
}
