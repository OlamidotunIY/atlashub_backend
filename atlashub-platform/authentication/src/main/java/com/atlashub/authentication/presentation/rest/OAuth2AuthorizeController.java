package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.command.IssueAuthorizationCode.IssueAuthorizationCodeCommand;
import com.atlashub.authentication.application.command.IssueAuthorizationCode.IssueAuthorizationCodeHandler;
import com.atlashub.authentication.application.command.IssueAuthorizationCode.IssueAuthorizationCodeResult;
import com.atlashub.authentication.presentation.dto.AuthorizeRequest;
import com.atlashub.authentication.presentation.dto.AuthorizeResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/oauth2")
@Tag(name = "OAuth2 Authorize", description = "OAuth 2.0 / OIDC Authorization Server")
public class OAuth2AuthorizeController {

    private final IssueAuthorizationCodeHandler handler;

    public OAuth2AuthorizeController(IssueAuthorizationCodeHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/authorize")
    @Operation(summary = "Authorize OAuth2 client via GET (browser redirect flow)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AuthorizeResponse>> authorizeGet(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam("code_challenge") String codeChallenge,
            @RequestParam("code_challenge_method") String codeChallengeMethod,
            @RequestParam(value = "nonce", required = false) String nonce,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Authentication required", null, null));
        }

        IssueAuthorizationCodeResult result = handler.execute(new IssueAuthorizationCodeCommand(
                clientId, redirectUri, responseType, scope, state, codeChallenge, codeChallengeMethod, nonce, principal.userId()
        ));

        String targetUri = buildRedirectUri(result.redirectUri(), result.code(), result.state());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(targetUri))
                .body(new ApiResponse<>(true, "Authorization code issued",
                        new AuthorizeResponse(result.code(), result.state(), targetUri), null));
    }

    @PostMapping("/authorize")
    @Operation(summary = "Authorize OAuth2 client via POST (SPA / BFF API flow)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AuthorizeResponse>> authorizePost(
            @Valid @RequestBody AuthorizeRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Authentication required", null, null));
        }

        IssueAuthorizationCodeResult result = handler.execute(new IssueAuthorizationCodeCommand(
                request.clientId(), request.redirectUri(), request.responseType(),
                request.scope(), request.state(), request.codeChallenge(),
                request.codeChallengeMethod(), request.nonce(), principal.userId()
        ));

        String targetUri = buildRedirectUri(result.redirectUri(), result.code(), result.state());
        return ResponseEntity.ok(new ApiResponse<>(true, "Authorization code issued",
                new AuthorizeResponse(result.code(), result.state(), targetUri), null));
    }

    private String buildRedirectUri(String baseRedirectUri, String code, String state) {
        String delimiter = baseRedirectUri.contains("?") ? "&" : "?";
        StringBuilder sb = new StringBuilder(baseRedirectUri).append(delimiter)
                .append("code=").append(URLEncoder.encode(code, StandardCharsets.UTF_8));
        if (state != null && !state.isBlank()) {
            sb.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
