package com.atlashub.compliance.presentation.rest;

import com.atlashub.compliance.application.commands.SaveComplianceDocument.*;
import com.atlashub.compliance.presentation.dto.SaveComplianceDocumentRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/compliance/documents")
public class ComplianceDocumentController {
    private final SaveComplianceDocumentHandler handler;
    public ComplianceDocumentController(SaveComplianceDocumentHandler handler) { this.handler = handler; }

    @PostMapping("/{requirementId}")
    public ResponseEntity<ApiResponse<Void>> save(@AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long requirementId, @RequestBody SaveComplianceDocumentRequest request) {
        handler.execute(new SaveComplianceDocumentCommand(principal.activeOrganizationId(), requirementId,
                request.storageObjectKey(), request.textValue()));
        return done("Compliance document saved");
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
