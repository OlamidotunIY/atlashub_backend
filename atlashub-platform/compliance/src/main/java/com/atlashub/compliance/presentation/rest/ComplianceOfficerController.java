package com.atlashub.compliance.presentation.rest;

import com.atlashub.compliance.application.commands.ReplaceBusinessOfficers.*;
import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.compliance.presentation.dto.*;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/compliance/officers")
@Tag(name = "Compliance Officers", description = "Business officer verification details")
public class ComplianceOfficerController {
    private final ReplaceBusinessOfficersHandler handler;
    public ComplianceOfficerController(ReplaceBusinessOfficersHandler handler) { this.handler = handler; }

    @PutMapping
    @Operation(summary = "Replace business officers")
    public ResponseEntity<ApiResponse<Void>> replace(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                                     @Valid @RequestBody ReplaceBusinessOfficersRequest request) {
        handler.execute(new ReplaceBusinessOfficersCommand(principal.activeOrganizationId(), request.officers().stream()
                .map(this::input).toList()));
        return done("Business officers saved");
    }

    private ReplaceBusinessOfficersCommand.OfficerInput input(BusinessOfficerRequest request) {
        AddressRequest address = request.address();
        return new ReplaceBusinessOfficersCommand.OfficerInput(request.role(), request.firstName(),
                request.middleName(), request.lastName(), request.maidenName(), request.nationality(), request.dateOfBirth(),
                new EmailAddress(request.email()), new PhoneNumber(request.phoneNumber()),
                new AddressData(address.addressLine1(), address.addressLine2(), address.city(), address.state(),
                        address.postalCode(), address.country()), request.bvn(), request.title(), request.percentageOwned());
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
