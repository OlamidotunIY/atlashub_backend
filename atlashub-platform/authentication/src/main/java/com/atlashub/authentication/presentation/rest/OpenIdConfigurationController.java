package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.query.GetOpenIdConfiguration.GetOpenIdConfigurationHandler;
import com.atlashub.authentication.application.query.GetOpenIdConfiguration.GetOpenIdConfigurationQuery;
import com.atlashub.authentication.application.query.GetOpenIdConfiguration.GetOpenIdConfigurationResult;
import com.atlashub.authentication.presentation.dto.OpenIdConfigurationResponse;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "OIDC Discovery", description = "OpenID Connect Provider Configuration")
public class OpenIdConfigurationController {

    private final GetOpenIdConfigurationHandler handler;

    public OpenIdConfigurationController(GetOpenIdConfigurationHandler handler) {
        this.handler = handler;
    }

    @PublicEndpoint
    @GetMapping("/.well-known/openid-configuration")
    @Operation(summary = "Get OpenID Connect Provider Configuration metadata")
    public ResponseEntity<ApiResponse<OpenIdConfigurationResponse>> getConfiguration(HttpServletRequest request) {
        String baseUrl = resolveBaseUrl(request);
        GetOpenIdConfigurationResult result = handler.execute(new GetOpenIdConfigurationQuery(baseUrl));

        OpenIdConfigurationResponse response = new OpenIdConfigurationResponse(
                result.issuer(),
                result.authorizationEndpoint(),
                result.tokenEndpoint(),
                result.userinfoEndpoint(),
                result.jwksUri(),
                result.responseTypesSupported(),
                result.subjectTypesSupported(),
                result.idTokenSigningAlgValuesSupported(),
                result.codeChallengeMethodsSupported(),
                result.scopesSupported(),
                result.tokenEndpointAuthMethodsSupported(),
                result.grantTypesSupported()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "OpenID configuration retrieved", response, null));
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto == null || proto.isBlank()) {
            proto = request.getScheme();
        }
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getHeader("Host");
        }
        if (host == null || host.isBlank()) {
            host = request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
        }
        return proto + "://" + host + request.getContextPath();
    }
}
