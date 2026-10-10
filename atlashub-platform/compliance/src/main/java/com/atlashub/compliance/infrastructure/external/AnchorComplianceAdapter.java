package com.atlashub.compliance.infrastructure.external;

import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.valueobject.AddressData;
import com.atlashub.shared.application.port.AnchorComplianceTransportPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class AnchorComplianceAdapter implements AnchorCompliancePort {
    private final AnchorComplianceTransportPort transport;
    public AnchorComplianceAdapter(AnchorComplianceTransportPort transport){this.transport=transport;}
    @Override public List<DocumentRequirement> previewDocumentRequirements(
            com.atlashub.compliance.domain.valueobject.LegalRegistrationType type,LocalDate date){
        return transport.previewDocumentRequirements(type.anchorCode(),date).stream().map(this::requirement).toList();}
    @Override public BusinessCustomerResult createBusinessCustomer(BusinessCustomerRequest request,ApiEnvironment environment){
        var business=request.business();var contact=request.contact();
        var result=transport.createBusinessCustomer(new AnchorComplianceTransportPort.BusinessCustomerRequest(
                request.organizationId(),new AnchorComplianceTransportPort.Business(business.industry().anchorCode(),
                business.registrationType().anchorCode(),contact.mainAddress().country(),business.legalName(),
                business.businessBvn(),business.registrationDate(),business.businessDescription(),business.website()),
                new AnchorComplianceTransportPort.Contact(contact.generalEmail().value(),contact.supportEmail().value(),
                        contact.disputeEmail().value(),address(contact.mainAddress()),address(contact.registeredAddress()),
                        contact.phoneNumber().value()),request.officers().stream().map(officer->
                        new AnchorComplianceTransportPort.Officer(officer.localId(),officer.role().name(),officer.firstName(),
                                officer.middleName(),officer.lastName(),officer.maidenName(),officer.nationality(),
                                officer.dateOfBirth(),officer.email(),officer.phoneNumber(),address(officer.address()),
                                officer.bvn(),officer.title(),officer.percentageOwned())).toList()),environment);
        return new BusinessCustomerResult(result.customerId(),result.officerIds());}
    @Override public List<DocumentRequirement> fetchCustomerDocumentRequirements(String customerId){
        return transport.fetchCustomerDocumentRequirements(customerId).stream().map(this::requirement).toList();}
    @Override public void uploadDocument(String customerId,String documentId,String textValue,StoredDocument file){
        transport.uploadDocument(customerId,documentId,textValue,file==null?null:
                new AnchorComplianceTransportPort.StoredDocument(file.objectKey(),file.contentType(),file.bytes()));}
    @Override public void triggerBusinessVerification(String customerId){transport.triggerBusinessVerification(customerId);}
    @Override public BusinessCustomerDetails fetchBusinessCustomer(String customerId){var result=transport.fetchBusinessCustomer(customerId);
        return new BusinessCustomerDetails(result.customerId(),result.verificationStatus());}
    private AnchorComplianceTransportPort.Address address(AddressData value){return new AnchorComplianceTransportPort.Address(
            value.country(),value.state(),value.addressLine1(),value.addressLine2(),value.city(),value.postalCode());}
    private DocumentRequirement requirement(AnchorComplianceTransportPort.DocumentRequirement value){return new DocumentRequirement(
            value.documentId(),value.documentType(),value.description(),value.required());}
}
