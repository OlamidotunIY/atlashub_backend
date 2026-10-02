package com.atlashub.compliance.presentation.rest;

import com.atlashub.compliance.application.commands.AcceptServiceAgreement.AcceptServiceAgreementCommand;
import com.atlashub.compliance.application.commands.AcceptServiceAgreement.AcceptServiceAgreementHandler;
import com.atlashub.compliance.application.commands.SubmitCompliance.SubmitComplianceCommand;
import com.atlashub.compliance.application.commands.SubmitCompliance.SubmitComplianceHandler;
import com.atlashub.compliance.application.commands.UpdateBusinessProfile.UpdateBusinessProfileCommand;
import com.atlashub.compliance.application.commands.UpdateBusinessProfile.UpdateBusinessProfileHandler;
import com.atlashub.compliance.application.commands.UpdateComplianceDocuments.UpdateComplianceDocumentsCommand;
import com.atlashub.compliance.application.commands.UpdateComplianceDocuments.UpdateComplianceDocumentsHandler;
import com.atlashub.compliance.application.commands.UpdateContactInfo.UpdateContactInfoCommand;
import com.atlashub.compliance.application.commands.UpdateContactInfo.UpdateContactInfoHandler;
import com.atlashub.compliance.application.commands.UpdateOwnerIdentity.UpdateOwnerIdentityCommand;
import com.atlashub.compliance.application.commands.UpdateOwnerIdentity.UpdateOwnerIdentityHandler;
import com.atlashub.compliance.application.queries.GetComplianceStatus.ComplianceStatusResult;
import com.atlashub.compliance.application.queries.GetComplianceStatus.GetComplianceStatusHandler;
import com.atlashub.compliance.application.queries.GetComplianceStatus.GetComplianceStatusQuery;
import com.atlashub.compliance.domain.valueobject.BusinessProfileData;
import com.atlashub.compliance.domain.valueobject.ComplianceDocumentsData;
import com.atlashub.compliance.domain.valueobject.ContactInfoData;
import com.atlashub.compliance.domain.valueobject.OwnerIdentityData;
import com.atlashub.compliance.domain.valueobject.AddressData;
import com.atlashub.compliance.domain.valueobject.GovernmentIdType;
import com.atlashub.compliance.presentation.dto.BusinessProfileRequest;
import com.atlashub.compliance.presentation.dto.ComplianceDocumentsRequest;
import com.atlashub.compliance.presentation.dto.ContactInfoRequest;
import com.atlashub.compliance.presentation.dto.OwnerIdentityRequest;
import com.atlashub.compliance.presentation.dto.ServiceAgreementRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {
    private final UpdateBusinessProfileHandler businessProfileHandler;
    private final UpdateContactInfoHandler contactInfoHandler;
    private final UpdateOwnerIdentityHandler ownerIdentityHandler;
    private final UpdateComplianceDocumentsHandler documentsHandler;
    private final AcceptServiceAgreementHandler agreementHandler;
    private final SubmitComplianceHandler submitHandler;
    private final GetComplianceStatusHandler statusHandler;

    public ComplianceController(UpdateBusinessProfileHandler businessProfileHandler,
                                UpdateContactInfoHandler contactInfoHandler,
                                UpdateOwnerIdentityHandler ownerIdentityHandler,
                                UpdateComplianceDocumentsHandler documentsHandler,
                                AcceptServiceAgreementHandler agreementHandler,
                                SubmitComplianceHandler submitHandler,
                                GetComplianceStatusHandler statusHandler) {
        this.businessProfileHandler = businessProfileHandler;
        this.contactInfoHandler = contactInfoHandler;
        this.ownerIdentityHandler = ownerIdentityHandler;
        this.documentsHandler = documentsHandler;
        this.agreementHandler = agreementHandler;
        this.submitHandler = submitHandler;
        this.statusHandler = statusHandler;
    }

    @PutMapping("/business-profile")
    public ResponseEntity<ApiResponse<Void>> updateBusinessProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody BusinessProfileRequest request) {
        BusinessProfileData data = new BusinessProfileData(
                request.businessDescription(), request.industry(), request.annualTransactionVolume(),
                Money.of(request.expectedMonthlyVolume(), CurrencyCode.valueOf(request.currency())),
                request.staffCount());
        businessProfileHandler.execute(new UpdateBusinessProfileCommand(principal.activeOrganizationId(), data));
        return saved();
    }

    @PutMapping("/contact-info")
    public ResponseEntity<ApiResponse<Void>> updateContactInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ContactInfoRequest request) {
        ContactInfoData data = new ContactInfoData(
                new EmailAddress(request.supportEmail()), new EmailAddress(request.disputeEmail()),
                new PhoneNumber(request.whatsappNumber()),
                new AddressData(request.street(), request.city(), request.state(), request.country()));
        contactInfoHandler.execute(new UpdateContactInfoCommand(principal.activeOrganizationId(), data));
        return saved();
    }

    @PutMapping("/owners-and-officers")
    public ResponseEntity<ApiResponse<Void>> updateOwner(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody OwnerIdentityRequest request) {
        OwnerIdentityData data = new OwnerIdentityData(
                request.bvn(), request.nin(), request.dateOfBirth(),
                GovernmentIdType.valueOf(request.governmentIdType()), request.governmentIdNumber(),
                request.governmentIdFrontUrl(), request.governmentIdBackUrl(), request.selfieUrl());
        ownerIdentityHandler.execute(new UpdateOwnerIdentityCommand(principal.activeOrganizationId(), data));
        return saved();
    }

    @PutMapping("/documents")
    public ResponseEntity<ApiResponse<Void>> updateDocuments(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ComplianceDocumentsRequest request) {
        ComplianceDocumentsData data = new ComplianceDocumentsData(request.suppliedDocumentTypes());
        documentsHandler.execute(new UpdateComplianceDocumentsCommand(principal.activeOrganizationId(), data));
        return saved();
    }

    @PutMapping("/service-agreement")
    public ResponseEntity<ApiResponse<Void>> acceptAgreement(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ServiceAgreementRequest request,
            HttpServletRequest httpRequest) {
        agreementHandler.execute(new AcceptServiceAgreementCommand(
                principal.activeOrganizationId(), clientIp(httpRequest), request.termsVersion()));
        return saved();
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Void>> submit(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        submitHandler.execute(new SubmitComplianceCommand(principal.activeOrganizationId()));
        return done("Compliance submitted");
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<ComplianceStatusResult>> status(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ok(statusHandler.execute(new GetComplianceStatusQuery(principal.activeOrganizationId())));
    }

    private ResponseEntity<ApiResponse<Void>> saved() {
        return done("Compliance step saved");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
