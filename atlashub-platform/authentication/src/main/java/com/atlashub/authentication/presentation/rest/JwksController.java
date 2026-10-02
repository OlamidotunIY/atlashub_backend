package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.infrastructure.security.RsaSigningKeyProvider;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {
    private final RsaSigningKeyProvider signingKeyProvider;

    public JwksController(RsaSigningKeyProvider signingKeyProvider) {
        this.signingKeyProvider = signingKeyProvider;
    }

    @PublicEndpoint
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return signingKeyProvider.jwkSet();
    }
}
