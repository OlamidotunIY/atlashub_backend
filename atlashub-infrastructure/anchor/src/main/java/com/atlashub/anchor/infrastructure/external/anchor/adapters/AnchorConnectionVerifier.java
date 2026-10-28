package com.atlashub.anchor.infrastructure.external.anchor.adapters;

import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorWebhookClient;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorResponse;
import com.atlashub.anchor.infrastructure.external.anchor.dto.webhook.AnchorWebhookResource;
import com.atlashub.integrations.application.port.ConnectionVerifier;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/** Verifies an optional merchant Anchor API key without provisioning an account. */
@Component
public class AnchorConnectionVerifier implements ConnectionVerifier {
    @Override public Provider provider() { return Provider.ANCHOR; }

    @Override
    public Verification verify(ApiEnvironment environment, String merchantCountry, String credential,
                               Set<Capability> documentedCapabilities) {
        if (!"NG".equals(merchantCountry))
            throw new IllegalArgumentException("Anchor connections are currently available only to NG merchants");
        AnchorResponse<List<AnchorWebhookResource>> response = client(environment, credential).listWebhooks();
        if (response == null || response.data() == null)
            throw new IllegalStateException("Anchor did not confirm the merchant credential");
        return new Verification("anchor:" + fingerprint(credential), documentedCapabilities);
    }

    private AnchorWebhookClient client(ApiEnvironment environment, String credential) {
        if (credential == null || credential.isBlank())
            throw new IllegalArgumentException("Anchor credential is required");
        String baseUrl = environment == ApiEnvironment.LIVE
                ? "https://api.getanchor.co" : "https://api.sandbox.getanchor.co";
        RestClient rest = RestClient.builder().baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-anchor-key", credential).build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rest)).build()
                .createClient(AnchorWebhookClient.class);
    }

    private String fingerprint(String credential) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(credential.getBytes(StandardCharsets.UTF_8))).substring(0, 16);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
