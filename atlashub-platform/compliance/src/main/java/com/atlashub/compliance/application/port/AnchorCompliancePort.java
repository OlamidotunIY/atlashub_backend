package com.atlashub.compliance.application.port;

import com.atlashub.compliance.domain.valueobject.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnchorCompliancePort {
    List<DocumentRequirement> previewDocumentRequirements(LegalRegistrationType type, LocalDate registrationDate);
    BusinessCustomerResult createBusinessCustomer(BusinessCustomerRequest request);
    List<DocumentRequirement> fetchCustomerDocumentRequirements(String customerId);
    void uploadDocument(String customerId, String documentId, String textValue, StoredDocument file);
    void triggerBusinessVerification(String customerId);
    BusinessCustomerDetails fetchBusinessCustomer(String customerId);

    record BusinessCustomerRequest(Long organizationId, BusinessProfileData business, ContactInfoData contact,
                                   List<Officer> officers) {}
    record Officer(Long localId, OfficerRole role, String firstName, String middleName, String lastName,
                   String maidenName, String nationality, LocalDate dateOfBirth, String email, String phoneNumber,
                   AddressData address, String bvn, String title, BigDecimal percentageOwned) {}
    record BusinessCustomerResult(String customerId, Map<Long, String> officerIds) {}
    record DocumentRequirement(String documentId, String documentType, String description, boolean required) {}
    record StoredDocument(String objectKey, String contentType, byte[] bytes) {}
    record BusinessCustomerDetails(String customerId, String verificationStatus) {}
}
