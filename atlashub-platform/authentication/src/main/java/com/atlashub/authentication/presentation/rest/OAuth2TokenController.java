package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.command.ExchangeToken.ExchangeTokenCommand;
import com.atlashub.authentication.application.command.ExchangeToken.ExchangeTokenHandler;
import com.atlashub.authentication.application.command.ExchangeToken.ExchangeTokenResult;
import com.atlashub.authentication.presentation.dto.TokenRequest;
import com.atlashub.authentication.presentation.dto.TokenResponse;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth2")
@Tag(name = "OAuth2 Token", description = "OAuth 2.0 / OIDC Token Endpoint")
public class OAuth2TokenController {

    private final ExchangeTokenHandler handler;

    public OAuth2TokenController(ExchangeTokenHandler handler) {
        this.handler = handler;
    }

    @PublicEndpoint
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Exchange authorization code or refresh token (x-www-form-urlencoded)")
    public ResponseEntity<ApiResponse<TokenResponse>> tokenForm(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "device_fingerprint", required = false) String deviceFingerprint,
            HttpServletRequest httpRequest) {
        ExchangeTokenResult result = handler.execute(new ExchangeTokenCommand(
                grantType, code, redirectUri, clientId, clientSecret, codeVerifier,
                refreshToken, deviceFingerprint, clientIp(httpRequest), httpRequest.getHeader("User-Agent")
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Token issued successfully", toResponse(result), null));
    }

    @PublicEndpoint
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Exchange authorization code or refresh token (application/json)")
    public ResponseEntity<ApiResponse<TokenResponse>> tokenJson(
            @Valid @RequestBody TokenRequest request,
            HttpServletRequest httpRequest) {
        ExchangeTokenResult result = handler.execute(new ExchangeTokenCommand(
                request.grantType(), request.code(), request.redirectUri(),
                request.clientId(), request.clientSecret(), request.codeVerifier(),
                request.refreshToken(), null, clientIp(httpRequest), httpRequest.getHeader("User-Agent")
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Token issued successfully", toResponse(result), null));
    }

    private TokenResponse toResponse(ExchangeTokenResult result) {
        return new TokenResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn(),
                result.refreshToken(),
                result.idToken(),
                result.scope()
        );
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
