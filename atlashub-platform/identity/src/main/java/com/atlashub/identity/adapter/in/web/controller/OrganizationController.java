package com.atlashub.identity.adapter.in.web.controller;

import com.atlashub.identity.application.command.*;
import com.atlashub.identity.application.result.OrganizationProfileDto;
import com.atlashub.identity.application.result.RegisterOrganizationResult;
import com.atlashub.identity.application.query.GetOrganizationProfileQuery;
import com.atlashub.identity.application.usecase.*;
import com.atlashub.identity.domain.valueobject.ComplianceStatus;
import com.atlashub.identity.adapter.in.web.request.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.atlashub.shared.dto.ApiResponse;
import java.security.Principal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/Organizations")
@Tag(name = "Organizations", description = "Organization onboarding and profile management")
public class OrganizationController {

    private static final Logger log = LoggerFactory.getLogger(OrganizationController.class);

    private final RegisterOrganizationUseCase registerOrganizationUseCase;
    private final GetOrganizationProfileUseCase getOrganizationProfileUseCase;
    private final CompleteComplianceProfileUseCase completeComplianceProfileUseCase;
    private final CompleteComplianceContactUseCase completeComplianceContactUseCase;
    private final CompleteComplianceOwnerUseCase completeComplianceOwnerUseCase;
    private final CompleteComplianceAccountUseCase completeComplianceAccountUseCase;
    private final CompleteComplianceServiceAgreementUseCase completeComplianceServiceAgreementUseCase;
    private final SubmitComplianceUseCase submitComplianceUseCase;

    public OrganizationController(
            RegisterOrganizationUseCase registerOrganizationUseCase, 
            GetOrganizationProfileUseCase getOrganizationProfileUseCase,
            CompleteComplianceProfileUseCase completeComplianceProfileUseCase,
            CompleteComplianceContactUseCase completeComplianceContactUseCase,
            CompleteComplianceOwnerUseCase completeComplianceOwnerUseCase,
            CompleteComplianceAccountUseCase completeComplianceAccountUseCase,
            CompleteComplianceServiceAgreementUseCase completeComplianceServiceAgreementUseCase,
            SubmitComplianceUseCase submitComplianceUseCase) {
        this.registerOrganizationUseCase = registerOrganizationUseCase;
        this.getOrganizationProfileUseCase = getOrganizationProfileUseCase;
        this.completeComplianceProfileUseCase = completeComplianceProfileUseCase;
        this.completeComplianceContactUseCase = completeComplianceContactUseCase;
        this.completeComplianceOwnerUseCase = completeComplianceOwnerUseCase;
        this.completeComplianceAccountUseCase = completeComplianceAccountUseCase;
        this.completeComplianceServiceAgreementUseCase = completeComplianceServiceAgreementUseCase;
        this.submitComplianceUseCase = submitComplianceUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a new Organization", description = "Creates a new Organization account")
    public ResponseEntity<ApiResponse<RegisterOrganizationResult>> register(@Valid @RequestBody RegisterOrganizationRequest request, Principal principal) {
        log.info("Received request to register Organization");
        Long userId = Long.valueOf(principal.getName());
        RegisterOrganizationCommand command = new RegisterOrganizationCommand(
                userId,
                request.businessName(),
                request.businessType()
        );
        RegisterOrganizationResult result = registerOrganizationUseCase.execute(command);
        log.info("Successfully processed registration for Organization ID: {}", result.organizationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Organization registered successfully", result, null));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get Organization profile", description = "Retrieves the profile of the authenticated Organization")
    public ResponseEntity<ApiResponse<OrganizationProfileDto>> getProfile(Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        log.debug("Fetching profile for Organization ID: {}", OrganizationIdStr);
        GetOrganizationProfileQuery query = new GetOrganizationProfileQuery(Long.valueOf(OrganizationIdStr));
        OrganizationProfileDto result = getOrganizationProfileUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", result, null));
    }
    
    @GetMapping("/compliance")
    @Operation(summary = "Get compliance status")
    public ResponseEntity<ApiResponse<ComplianceStatus>> getComplianceStatus(Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        GetOrganizationProfileQuery query = new GetOrganizationProfileQuery(Long.valueOf(OrganizationIdStr));
        OrganizationProfileDto result = getOrganizationProfileUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Compliance status retrieved", ComplianceStatus.valueOf(result.kycStatus()), null));
    }

    @PutMapping("/compliance/profile")
    @Operation(summary = "Complete compliance profile step")
    public ResponseEntity<ApiResponse<Void>> completeProfile(@Valid @RequestBody CompleteComplianceProfileRequest request, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceProfileCommand command = new CompleteComplianceProfileCommand(
            Long.valueOf(OrganizationIdStr),
            request.description(),
            request.staffSize(),
            request.industry(),
            request.category(),
            request.annualProjectedSalesVolume() != null ? request.annualProjectedSalesVolume().amount() : null,
            request.annualProjectedSalesVolume() != null ? request.annualProjectedSalesVolume().currency() : null
        );
        completeComplianceProfileUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }

    @PutMapping("/compliance/contact")
    @Operation(summary = "Complete compliance contact step")
    public ResponseEntity<ApiResponse<Void>> completeContact(@Valid @RequestBody CompleteComplianceContactRequest request, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceContactCommand command = new CompleteComplianceContactCommand(
            Long.valueOf(OrganizationIdStr),
            request.supportEmail(),
            request.disputeEmail(),
            request.whatsappPhone(),
            request.whatsappName(),
            request.websiteUrl(),
            request.twitterHandle(),
            request.facebookUsername(),
            request.instagramHandle(),
            request.businessState(),
            request.businessLga(),
            request.businessCity(),
            request.businessStreet()
        );
        completeComplianceContactUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }

    @PutMapping("/compliance/owner")
    @Operation(summary = "Complete compliance owner step")
    public ResponseEntity<ApiResponse<Void>> completeOwner(@Valid @RequestBody CompleteComplianceOwnerRequest request, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceOwnerCommand command = new CompleteComplianceOwnerCommand(
            Long.valueOf(OrganizationIdStr),
            request.bvn(),
            request.nin(),
            request.dateOfBirth(),
            request.address(),
            request.idType(),
            request.idNumber(),
            request.rcNumber()
        );
        completeComplianceOwnerUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }

    @PutMapping("/compliance/account")
    @Operation(summary = "Complete compliance account step")
    public ResponseEntity<ApiResponse<Void>> completeAccount(@Valid @RequestBody CompleteComplianceAccountRequest request, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceAccountCommand command = new CompleteComplianceAccountCommand(
            Long.valueOf(OrganizationIdStr),
            request.bankCode(),
            request.accountNumber()
        );
        completeComplianceAccountUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }

    @PutMapping("/compliance/service-agreement")
    @Operation(summary = "Complete compliance service agreement step")
    public ResponseEntity<ApiResponse<Void>> completeServiceAgreement(@Valid @RequestBody CompleteComplianceServiceAgreementRequest request, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceServiceAgreementCommand command = new CompleteComplianceServiceAgreementCommand(
            Long.valueOf(OrganizationIdStr),
            request.agreed()
        );
        completeComplianceServiceAgreementUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }

    @PostMapping("/compliance/submit")
    @Operation(summary = "Submit compliance for review")
    public ResponseEntity<ApiResponse<Void>> submitCompliance(Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        SubmitComplianceCommand command = new SubmitComplianceCommand(Long.valueOf(OrganizationIdStr));
        submitComplianceUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }
}



