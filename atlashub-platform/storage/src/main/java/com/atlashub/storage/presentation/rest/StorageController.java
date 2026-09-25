package com.atlashub.storage.presentation.rest;

import com.atlashub.storage.application.query.GetSignedUrl.GetSignedUrlHandler;
import com.atlashub.storage.application.query.GetSignedUrl.GetSignedUrlQuery;
import com.atlashub.storage.application.query.GetSignedUrl.GetSignedUrlResult;
import com.atlashub.storage.presentation.dto.SignedUrlRequest;
import com.atlashub.storage.presentation.dto.SignedUrlResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Storage", description = "File storage — pre-signed upload URLs for direct frontend-to-Firebase uploads")
public class StorageController {

    private final GetSignedUrlHandler getSignedUrlHandler;

    public StorageController(GetSignedUrlHandler getSignedUrlHandler) {
        this.getSignedUrlHandler = getSignedUrlHandler;
    }

    @GetMapping("/storage/signed-url")
    @Operation(
            summary = "Request a pre-signed Firebase Storage upload URL",
            description = "Returns a short-lived PUT URL. The frontend should upload the file " +
                          "directly to this URL using HTTP PUT with the matching Content-Type header.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<SignedUrlResponse>> getSignedUrl(
            @AuthenticationPrincipal Long userId,
            @Valid @ModelAttribute SignedUrlRequest request) {

        GetSignedUrlResult result = getSignedUrlHandler.execute(
                new GetSignedUrlQuery(
                        userId,
                        request.objectPath(),
                        request.contentType(),
                        request.ttlMinutes()
                )
        );

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Signed upload URL generated",
                new SignedUrlResponse(result.signedUrl(), result.objectPath()),
                null
        ));
    }
}
