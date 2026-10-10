package com.atlashub.shared.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnchorComplianceTransportPort {
    List<DocumentRequirement> previewDocumentRequirements(String registrationType,LocalDate registrationDate);
    BusinessCustomerResult createBusinessCustomer(BusinessCustomerRequest request,ApiEnvironment environment);
    List<DocumentRequirement> fetchCustomerDocumentRequirements(String customerId);
    void uploadDocument(String customerId,String documentId,String textValue,StoredDocument file);
    void triggerBusinessVerification(String customerId);
    BusinessCustomerDetails fetchBusinessCustomer(String customerId);
    record Address(String country,String state,String addressLine1,String addressLine2,String city,String postalCode){}
    record Business(String industry,String registrationType,String country,String legalName,String businessBvn,
                    LocalDate registrationDate,String description,String website){}
    record Contact(String generalEmail,String supportEmail,String disputeEmail,Address mainAddress,
                   Address registeredAddress,String phoneNumber){}
    record Officer(Long localId,String role,String firstName,String middleName,String lastName,String maidenName,
                   String nationality,LocalDate dateOfBirth,String email,String phoneNumber,Address address,String bvn,
                   String title,BigDecimal percentageOwned){}
    record BusinessCustomerRequest(Long organizationId,Business business,Contact contact,List<Officer> officers){}
    record BusinessCustomerResult(String customerId,Map<Long,String> officerIds){}
    record DocumentRequirement(String documentId,String documentType,String description,boolean required){}
    record StoredDocument(String objectKey,String contentType,byte[] bytes){}
    record BusinessCustomerDetails(String customerId,String verificationStatus){}
}
