package com.atlashub.integrations.infrastructure.external.stripe.adapters;

import com.atlashub.integrations.application.port.ConnectedChargeAdapter;
import com.atlashub.integrations.application.port.ConnectionVerifier;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class StripeConnectedChargeAdapter implements ConnectedChargeAdapter, ConnectionVerifier {
    private static final String API = "https://api.stripe.com";

    @Override public Provider provider() { return Provider.STRIPE; }

    @Override
    public Verification verify(ApiEnvironment environment, String merchantCountry, String credential,
                               Set<Capability> documentedCapabilities) {
        JsonNode account = client(credential).get().uri("/v1/account").retrieve().body(JsonNode.class);
        if (account == null || blank(account.path("id").asText()))
            throw new IllegalStateException("Stripe did not return an account identity");
        Set<Capability> confirmed = new HashSet<>();
        JsonNode capabilities = account.path("capabilities");
        if ("active".equals(capabilities.path("card_payments").asText())) {
            if (documentedCapabilities.contains(Capability.CARD_COLLECTION)) confirmed.add(Capability.CARD_COLLECTION);
            if (documentedCapabilities.contains(Capability.REFUND)) confirmed.add(Capability.REFUND);
        }
        if ("active".equals(capabilities.path("transfers").asText())
                && documentedCapabilities.contains(Capability.TRANSFER)) confirmed.add(Capability.TRANSFER);
        return new Verification(account.path("id").asText(), confirmed);
    }

    @Override
    public ChargeProviderPort.InitializationResult initialize(ChargeProviderPort.InitializationRequest request,
                                                               String credential) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("amount", minorUnits(request.amount()).toPlainString());
        form.add("currency", request.currency().name().toLowerCase(Locale.ROOT));
        form.add("receipt_email", request.email());
        form.add("payment_method_types[]", "card");
        form.add("metadata[atlashub_reference]", request.reference());
        JsonNode intent = client(credential).post().uri("/v1/payment_intents")
                .header("Idempotency-Key", request.reference()).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().body(JsonNode.class);
        String id = required(intent, "id");
        String clientSecret = required(intent, "client_secret");
        return new ChargeProviderPort.InitializationResult(id, "https://js.stripe.com/v3/", clientSecret);
    }

    @Override
    public ChargeProviderPort.RefundResult refund(ChargeProviderPort.RefundRequest request, String credential) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("payment_intent", request.providerReference());
        form.add("amount", minorUnits(request.amount()).toPlainString());
        JsonNode refund = client(credential).post().uri("/v1/refunds")
                .header("Idempotency-Key", request.idempotencyKey())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(JsonNode.class);
        return new ChargeProviderPort.RefundResult(required(refund, "id"), required(refund, "status"));
    }

    @Override
    public ChargeProviderPort.ProviderStatus fetchStatus(String providerReference, String credential) {
        JsonNode intent = client(credential).get().uri("/v1/payment_intents/{id}", providerReference)
                .retrieve().body(JsonNode.class);
        String status = switch (required(intent, "status")) {
            case "succeeded" -> "success";
            case "canceled" -> "failed";
            default -> "pending";
        };
        CurrencyCode currency = CurrencyCode.valueOf(required(intent, "currency").toUpperCase(Locale.ROOT));
        return new ChargeProviderPort.ProviderStatus(required(intent, "id"), status,
                new BigDecimal(required(intent, "amount_received")).movePointLeft(2), currency, "card",
                BigDecimal.ZERO);
    }

    private RestClient client(String credential) {
        if (blank(credential)) throw new IllegalArgumentException("Stripe credential is required");
        return RestClient.builder().baseUrl(API)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + credential)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();
    }

    private BigDecimal minorUnits(BigDecimal amount) { return amount.movePointRight(2).setScale(0); }
    private String required(JsonNode node, String field) {
        if (node == null || blank(node.path(field).asText()))
            throw new IllegalStateException("Stripe response is missing " + field);
        return node.path(field).asText();
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
