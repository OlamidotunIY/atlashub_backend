package com.atlashub.compliance.application.commands.OrchestrateAnchorCompliance;

import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.entities.*;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.shared.application.port.StoredObjectQueryPort;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrchestrateAnchorComplianceHandlerTest {
    @Test
    void creates_customer_uploads_documents_and_triggers_verification() {
        ComplianceRecordRepository repository = mock(ComplianceRecordRepository.class);
        AnchorCompliancePort anchor = mock(AnchorCompliancePort.class);
        StoredObjectQueryPort storage = mock(StoredObjectQueryPort.class);
        ComplianceRecord record = readySubmittedRecord();
        when(repository.findByOrganizationId(10L)).thenReturn(Optional.of(record));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        when(repository.nextIdentity()).thenReturn(99L);
        when(anchor.createBusinessCustomer(any())).thenReturn(
                new AnchorCompliancePort.BusinessCustomerResult("anchor-customer", Map.of(20L, "anchor-officer")));
        when(anchor.fetchCustomerDocumentRequirements("anchor-customer")).thenReturn(List.of(
                new AnchorCompliancePort.DocumentRequirement("anchor-document", "CERTIFICATE", "Certificate", true)));
        when(storage.readPrivateObject("compliance/10/certificate.pdf")).thenReturn(
                new StoredObjectQueryPort.StoredObject("compliance/10/certificate.pdf", "application/pdf", new byte[]{1}));

        new OrchestrateAnchorComplianceHandler(repository, anchor, storage)
                .execute(new OrchestrateAnchorComplianceCommand(10L));

        verify(anchor).uploadDocument(eq("anchor-customer"), eq("anchor-document"), isNull(), any());
        verify(anchor).triggerBusinessVerification("anchor-customer");
    }

    private ComplianceRecord readySubmittedRecord() {
        ComplianceRecord record = ComplianceRecord.create(1L, 10L);
        record.updateBusinessRegistration(new BusinessProfileData("Tolu Store",
                LegalRegistrationType.PRIVATE_LIMITED_COMPANY, LocalDate.of(2024, 1, 1), "RC1",
                "22222222226", SupportedBusinessIndustry.RETAIL, "Retail", null), true);
        AddressData address = new AddressData("1 Main", null, "Ikeja", "Lagos", "100001", "NG");
        record.updateContactAndAddresses(new ContactInfoData(new EmailAddress("general@example.com"),
                new EmailAddress("support@example.com"), new EmailAddress("dispute@example.com"),
                new PhoneNumber("+2348012345678"), address, address));
        record.replaceOfficers(List.of(new BusinessOfficer(20L, OfficerRole.OWNER, "Tolu", null, "Owner",
                null, "NG", LocalDate.of(1990, 1, 1), new EmailAddress("owner@example.com"),
                new PhoneNumber("+2348012345678"), address, "22222222226", "CEO", BigDecimal.valueOf(100), null, null)));
        record.recordRequiredDocuments(List.of(new ComplianceDocumentRequirement(30L, null, "CERTIFICATE",
                "Certificate", true, RequirementSource.PREFLIGHT, null, null, null, null, null, null)));
        record.saveComplianceDocument(30L, "compliance/10/certificate.pdf", null);
        record.acceptServiceAgreement("127.0.0.1", ZonedDateTime.now(), "v1");
        record.submit();
        return record;
    }
}
