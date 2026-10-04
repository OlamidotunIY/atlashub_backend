package com.atlashub.compliance.infrastructure.external;

import com.atlashub.anchor.client.*;
import com.atlashub.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.dto.common.AnchorRequest;
import com.atlashub.anchor.dto.customer.*;
import com.atlashub.anchor.dto.document.AnchorDocumentResource;
import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;
import com.atlashub.compliance.domain.valueobject.AddressData;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AnchorComplianceAdapter implements AnchorCompliancePort {
    private final ObjectProvider<AnchorClientRegistry> clients;
    public AnchorComplianceAdapter(ObjectProvider<AnchorClientRegistry> clients) { this.clients = clients; }

    @Override
    public List<DocumentRequirement> previewDocumentRequirements(
            com.atlashub.compliance.domain.valueobject.LegalRegistrationType type, java.time.LocalDate date) {
        return documents().preview(type.anchorCode(), date.toString()).data().stream().map(this::requirement).toList();
    }

    @Override
    public BusinessCustomerResult createBusinessCustomer(BusinessCustomerRequest request) {
        List<CreateBusinessCustomerData.Officer> officers = request.officers().stream().map(this::officer).toList();
        var business = request.business(); var contact = request.contact();
        CreateBusinessCustomerData data = new CreateBusinessCustomerData(new CreateBusinessCustomerData.Attributes(
                new CreateBusinessCustomerData.CountryState(contact.mainAddress().country(), contact.mainAddress().state()),
                new CreateBusinessCustomerData.BasicDetail(business.industry().anchorCode(),
                        business.registrationType().anchorCode(), contact.mainAddress().country(), business.legalName(),
                        business.businessBvn(), business.registrationDate().toString(),
                        business.businessDescription(), business.website()),
                new CreateBusinessCustomerData.Contact(
                        new CreateBusinessCustomerData.Emails(contact.generalEmail().value(),
                                contact.supportEmail().value(), contact.disputeEmail().value()),
                        new CreateBusinessCustomerData.Addresses(address(contact.mainAddress()), address(contact.registeredAddress())),
                        contact.phoneNumber().value()), officers));
        BusinessCustomerResource resource = customers().create(
                "atlashub-compliance-" + request.organizationId(), new AnchorRequest<>(data)).data();
        if (resource == null || resource.id() == null) throw new InvalidComplianceDataException("Anchor did not return a customer id");
        return new BusinessCustomerResult(resource.id(), mapOfficerIds(request.officers(), resource));
    }

    @Override public List<DocumentRequirement> fetchCustomerDocumentRequirements(String customerId) {
        return documents().listForCustomer(customerId).data().stream().map(this::requirement).toList();
    }

    @Override public void uploadDocument(String customerId, String documentId, String textValue, StoredDocument file) {
        ByteArrayResource resource = file == null ? null : new ByteArrayResource(file.bytes()) {
            @Override public String getFilename() { return file.objectKey().substring(file.objectKey().lastIndexOf('/') + 1); }
        };
        documents().upload(customerId, documentId, textValue, resource);
    }

    @Override public void triggerBusinessVerification(String customerId) { customers().triggerVerification(customerId); }

    @Override public BusinessCustomerDetails fetchBusinessCustomer(String customerId) {
        BusinessCustomerResource resource = customers().fetch(customerId).data();
        String status = resource.attributes() == null || resource.attributes().verification() == null
                ? null : resource.attributes().verification().status();
        return new BusinessCustomerDetails(resource.id(), status);
    }

    private AnchorBusinessCustomerClient customers() { return registry().forEnvironment(AnchorEnvironment.LIVE).businessCustomers(); }
    private AnchorDocumentClient documents() { return registry().forEnvironment(AnchorEnvironment.LIVE).documents(); }
    private AnchorClientRegistry registry() {
        AnchorClientRegistry registry = clients.getIfAvailable();
        if (registry == null) throw new IllegalStateException("Anchor integration is disabled or not configured");
        return registry;
    }

    private DocumentRequirement requirement(AnchorDocumentResource resource) {
        String type = resource.attributes() == null ? resource.type() : resource.attributes().type();
        String description = resource.attributes() == null ? null : resource.attributes().description();
        return new DocumentRequirement(resource.id(), type, description, true);
    }

    private CreateBusinessCustomerData.Address address(AddressData value) {
        return new CreateBusinessCustomerData.Address(value.country(), value.state(), value.addressLine1(),
                value.addressLine2(), value.city(), value.postalCode());
    }

    private CreateBusinessCustomerData.Officer officer(Officer value) {
        return new CreateBusinessCustomerData.Officer(value.role().name(),
                new CreateBusinessCustomerData.FullName(value.firstName(), value.lastName(), value.middleName(), value.maidenName()),
                value.nationality(), address(value.address()), value.dateOfBirth().toString(), value.email(),
                value.phoneNumber(), value.bvn(), value.title(), value.percentageOwned());
    }

    private Map<Long, String> mapOfficerIds(List<Officer> local, BusinessCustomerResource resource) {
        if (resource.attributes() == null || resource.attributes().officers() == null) return Map.of();
        Map<String, List<BusinessCustomerResource.Officer>> byName = resource.attributes().officers().stream()
                .collect(Collectors.groupingBy(item -> normalized(item.fullName().firstName(), item.fullName().lastName())));
        Map<Long, String> mapped = new HashMap<>();
        for (Officer officer : local) {
            List<BusinessCustomerResource.Officer> matches = byName.getOrDefault(
                    normalized(officer.firstName(), officer.lastName()), List.of());
            if (matches.size() != 1) throw new InvalidComplianceDataException("Anchor officer response could not be mapped safely");
            mapped.put(officer.localId(), matches.getFirst().officerId());
        }
        return Map.copyOf(mapped);
    }

    private String normalized(String first, String last) {
        return (first + "|" + last).trim().toLowerCase(Locale.ROOT);
    }
}
