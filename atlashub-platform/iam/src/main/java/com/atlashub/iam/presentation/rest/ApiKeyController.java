package com.atlashub.iam.presentation.rest;

import com.atlashub.iam.application.commands.IssueApiKey.IssueApiKeyCommand;
import com.atlashub.iam.application.commands.IssueApiKey.IssueApiKeyHandler;
import com.atlashub.iam.application.commands.IssueApiKey.IssuedApiKeyResult;
import com.atlashub.iam.application.commands.RevokeApiKey.RevokeApiKeyCommand;
import com.atlashub.iam.application.commands.RevokeApiKey.RevokeApiKeyHandler;
import com.atlashub.iam.application.queries.ListApiKeys.ApiKeyResult;
import com.atlashub.iam.application.queries.ListApiKeys.ListApiKeysHandler;
import com.atlashub.iam.application.queries.ListApiKeys.ListApiKeysQuery;
import com.atlashub.iam.presentation.dto.IssueApiKeyRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/iam/api-keys")
public class ApiKeyController {
    private final IssueApiKeyHandler issueHandler;
    private final RevokeApiKeyHandler revokeHandler;
    private final ListApiKeysHandler listHandler;

    public ApiKeyController(IssueApiKeyHandler issueHandler, RevokeApiKeyHandler revokeHandler,
                            ListApiKeysHandler listHandler) {
        this.issueHandler = issueHandler;
        this.revokeHandler = revokeHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssuedApiKeyResult>> issue(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody IssueApiKeyRequest request) {
        var result = issueHandler.execute(new IssueApiKeyCommand(principal.activeOrganizationId(), request.name(),
                request.environment(), principal.userId(), request.boundRoleId()));
        return ok(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApiKeyResult>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String environment) {
        return ok(listHandler.execute(new ListApiKeysQuery(principal.activeOrganizationId(), environment)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> revoke(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                                    @PathVariable Long id) {
        revokeHandler.execute(new RevokeApiKeyCommand(id, principal.activeOrganizationId(), principal.userId()));
        return done("API key revoked");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
