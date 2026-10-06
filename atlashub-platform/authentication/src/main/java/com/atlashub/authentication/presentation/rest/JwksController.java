package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.infrastructure.security.RsaSigningKeyProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "JWKS", description = "Public JWT signing keys")
public class JwksController {
    private final RsaSigningKeyProvider signingKeyProvider;

    public JwksController(RsaSigningKeyProvider signingKeyProvider) {
        this.signingKeyProvider = signingKeyProvider;
    }

    @PublicEndpoint
    @GetMapping("/.well-known/jwks.json")
    @Operation(summary = "Get the JWT JSON Web Key Set")
    public Map<String, Object> jwks() {
        return signingKeyProvider.jwkSet();
    }
}
