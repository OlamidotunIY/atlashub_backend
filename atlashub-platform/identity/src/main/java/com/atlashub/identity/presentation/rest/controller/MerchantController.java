package com.atlashub.identity.presentation.rest.controller;

import com.atlashub.identity.application.command.*;
import com.atlashub.identity.application.dto.MerchantProfileDto;
import com.atlashub.identity.application.dto.RegisterMerchantResult;
import com.atlashub.identity.application.query.GetMerchantProfileQuery;
import com.atlashub.identity.application.usecase.*;
import com.atlashub.identity.domain.model.ComplianceStatus;
import com.atlashub.identity.presentation.rest.request.*;
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
@RequestMapping("/api/v1/merchants")
@Tag(name = "Merchants", description = "Merchant onboarding and profile management")
public class MerchantController {

    private static final Logger log = LoggerFactory.getLogger(MerchantController.class);

    private final RegisterMerchantUseCase registerMerchantUseCase;
    private final GetMerchantProfileUseCase getMerchantProfileUseCase;
    private final CompleteComplianceProfileUseCase completeComplianceProfileUseCase;
    private final CompleteComplianceContactUseCase completeComplianceContactUseCase;
    private final CompleteComplianceOwnerUseCase completeComplianceOwnerUseCase;
    private final CompleteComplianceAccountUseCase completeComplianceAccountUseCase;
    private final CompleteComplianceServiceAgreementUseCase completeComplianceServiceAgreementUseCase;
    private final SubmitComplianceUseCase submitComplianceUseCase;

    public MerchantController(
            RegisterMerchantUseCase registerMerchantUseCase, 
            GetMerchantProfileUseCase getMerchantProfileUseCase,
            CompleteComplianceProfileUseCase completeComplianceProfileUseCase,
            CompleteComplianceContactUseCase completeComplianceContactUseCase,
            CompleteComplianceOwnerUseCase completeComplianceOwnerUseCase,
            CompleteComplianceAccountUseCase completeComplianceAccountUseCase,
            CompleteComplianceServiceAgreementUseCase completeComplianceServiceAgreementUseCase,
            SubmitComplianceUseCase submitComplianceUseCase) {
        this.registerMerchantUseCase = registerMerchantUseCase;
        this.getMerchantProfileUseCase = getMerchantProfileUseCase;
        this.completeComplianceProfileUseCase = completeComplianceProfileUseCase;
        this.completeComplianceContactUseCase = completeComplianceContactUseCase;
        this.completeComplianceOwnerUseCase = completeComplianceOwnerUseCase;
        this.completeComplianceAccountUseCase = completeComplianceAccountUseCase;
        this.completeComplianceServiceAgreementUseCase = completeComplianceServiceAgreementUseCase;
        this.submitComplianceUseCase = submitComplianceUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a new merchant", description = "Creates a new merchant account")
    public ResponseEntity<ApiResponse<RegisterMerchantResult>> register(@Valid @RequestBody RegisterMerchantRequest request) {
        log.info("Received request to register merchant with email: {}", request.email());
        RegisterMerchantCommand command = new RegisterMerchantCommand(
                request.country(),
                request.businessName(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.businessType()
        );
        RegisterMerchantResult result = registerMerchantUseCase.execute(command);
        log.info("Successfully processed registration for merchant ID: {}", result.merchantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Merchant registered successfully", result, null));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get merchant profile", description = "Retrieves the profile of the authenticated merchant")
    public ResponseEntity<ApiResponse<MerchantProfileDto>> getProfile(Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        log.debug("Fetching profile for merchant ID: {}", merchantIdStr);
        GetMerchantProfileQuery query = new GetMerchantProfileQuery(Long.valueOf(merchantIdStr));
        MerchantProfileDto result = getMerchantProfileUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", result, null));
    }
    
    @GetMapping("/compliance")
    @Operation(summary = "Get compliance status")
    public ResponseEntity<ApiResponse<ComplianceStatus>> getComplianceStatus(Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        GetMerchantProfileQuery query = new GetMerchantProfileQuery(Long.valueOf(merchantIdStr));
        MerchantProfileDto result = getMerchantProfileUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Compliance status retrieved", ComplianceStatus.valueOf(result.kycStatus()), null));
    }

    @PutMapping("/compliance/profile")
    @Operation(summary = "Complete compliance profile step")
    public ResponseEntity<ApiResponse<Void>> completeProfile(@Valid @RequestBody CompleteComplianceProfileRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceProfileCommand command = new CompleteComplianceProfileCommand(
            Long.valueOf(merchantIdStr),
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
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceContactCommand command = new CompleteComplianceContactCommand(
            Long.valueOf(merchantIdStr),
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
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceOwnerCommand command = new CompleteComplianceOwnerCommand(
            Long.valueOf(merchantIdStr),
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
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceAccountCommand command = new CompleteComplianceAccountCommand(
            Long.valueOf(merchantIdStr),
            request.bankCode(),
            request.accountNumber()
        );
        completeComplianceAccountUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }

    @PutMapping("/compliance/service-agreement")
    @Operation(summary = "Complete compliance service agreement step")
    public ResponseEntity<ApiResponse<Void>> completeServiceAgreement(@Valid @RequestBody CompleteComplianceServiceAgreementRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceServiceAgreementCommand command = new CompleteComplianceServiceAgreementCommand(
            Long.valueOf(merchantIdStr),
            request.agreed()
        );
        completeComplianceServiceAgreementUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }

    @PostMapping("/compliance/submit")
    @Operation(summary = "Submit compliance for review")
    public ResponseEntity<ApiResponse<Void>> submitCompliance(Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        SubmitComplianceCommand command = new SubmitComplianceCommand(Long.valueOf(merchantIdStr));
        submitComplianceUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operation successful", null, null));
    }
}


