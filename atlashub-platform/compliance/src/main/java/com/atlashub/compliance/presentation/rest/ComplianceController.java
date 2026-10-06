package com.atlashub.compliance.presentation.rest;

import com.atlashub.compliance.application.commands.AcceptServiceAgreement.AcceptServiceAgreementCommand;
import com.atlashub.compliance.application.commands.AcceptServiceAgreement.AcceptServiceAgreementHandler;
import com.atlashub.compliance.application.commands.SubmitCompliance.SubmitComplianceCommand;
import com.atlashub.compliance.application.commands.SubmitCompliance.SubmitComplianceHandler;
import com.atlashub.compliance.application.commands.UpdateBusinessProfile.UpdateBusinessProfileCommand;
import com.atlashub.compliance.application.commands.UpdateBusinessProfile.UpdateBusinessProfileHandler;
import com.atlashub.compliance.application.commands.UpdateContactInfo.UpdateContactInfoCommand;
import com.atlashub.compliance.application.commands.UpdateContactInfo.UpdateContactInfoHandler;
import com.atlashub.compliance.application.queries.GetComplianceDetails.*;
import com.atlashub.compliance.domain.valueobject.ContactInfoData;
import com.atlashub.compliance.domain.valueobject.AddressData;
import com.atlashub.compliance.presentation.dto.BusinessProfileRequest;
import com.atlashub.compliance.presentation.dto.ContactInfoRequest;
import com.atlashub.compliance.presentation.dto.ServiceAgreementRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.atlashub.shared.domain.valueobject.EmailAddress;
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
@Tag(name = "Compliance", description = "Business compliance collection and submission")
public class ComplianceController {
    private final UpdateBusinessProfileHandler businessProfileHandler;
    private final UpdateContactInfoHandler contactInfoHandler;
    private final AcceptServiceAgreementHandler agreementHandler;
    private final SubmitComplianceHandler submitHandler;
    private final GetComplianceDetailsHandler detailsHandler;

    public ComplianceController(UpdateBusinessProfileHandler businessProfileHandler,
                                UpdateContactInfoHandler contactInfoHandler,
                                AcceptServiceAgreementHandler agreementHandler,
                                SubmitComplianceHandler submitHandler,
                                GetComplianceDetailsHandler detailsHandler) {
        this.businessProfileHandler = businessProfileHandler;
        this.contactInfoHandler = contactInfoHandler;
        this.agreementHandler = agreementHandler;
        this.submitHandler = submitHandler;
        this.detailsHandler = detailsHandler;
    }

    @PutMapping("/business")
    @Operation(summary = "Save business compliance details")
    public ResponseEntity<ApiResponse<Void>> updateBusinessProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody BusinessProfileRequest request) {
        businessProfileHandler.execute(new UpdateBusinessProfileCommand(principal.activeOrganizationId(),
                request.businessRegistrationNumber(), request.businessBvn(),
                request.businessDescription(), request.website()));
        return saved();
    }

    @PutMapping("/contact")
    @Operation(summary = "Save compliance contact details")
    public ResponseEntity<ApiResponse<Void>> updateContactInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ContactInfoRequest request) {
        ContactInfoData data = new ContactInfoData(
                new EmailAddress(request.generalEmail()), new EmailAddress(request.supportEmail()),
                new EmailAddress(request.disputeEmail()), new PhoneNumber(request.phoneNumber()),
                address(request.mainAddress()), address(request.registeredAddress()));
        contactInfoHandler.execute(new UpdateContactInfoCommand(principal.activeOrganizationId(), data));
        return saved();
    }

    @PostMapping("/agreement")
    @Operation(summary = "Accept the compliance service agreement")
    public ResponseEntity<ApiResponse<Void>> acceptAgreement(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ServiceAgreementRequest request,
            HttpServletRequest httpRequest) {
        agreementHandler.execute(new AcceptServiceAgreementCommand(
                principal.activeOrganizationId(), clientIp(httpRequest), request.termsVersion()));
        return saved();
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit compliance for verification")
    public ResponseEntity<ApiResponse<Void>> submit(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        submitHandler.execute(new SubmitComplianceCommand(principal.activeOrganizationId()));
        return done("Compliance submitted");
    }

    @GetMapping
    @Operation(summary = "Get compliance status and requirements")
    public ResponseEntity<ApiResponse<ComplianceDetailsResult>> details(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ok(detailsHandler.execute(new GetComplianceDetailsQuery(principal.activeOrganizationId())));
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

    private AddressData address(com.atlashub.compliance.presentation.dto.AddressRequest request) {
        return new AddressData(request.addressLine1(), request.addressLine2(), request.city(), request.state(),
                request.postalCode(), request.country());
    }
}
