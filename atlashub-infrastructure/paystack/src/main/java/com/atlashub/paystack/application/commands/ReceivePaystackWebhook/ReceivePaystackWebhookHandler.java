package com.atlashub.paystack.application.commands.ReceivePaystackWebhook;

import com.atlashub.paystack.infrastructure.external.paystack.messaging.PaystackWebhookEventPublisher;
import com.atlashub.paystack.infrastructure.external.paystack.messaging.events.PaystackWebhookReceivedEvent;
import com.atlashub.paystack.infrastructure.external.paystack.dto.webhook.PaystackWebhookPayload;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import com.atlashub.paystack.infrastructure.external.paystack.webhook.PaystackWebhookSignatureVerifier;
import com.atlashub.paystack.exception.MalformedPaystackWebhookException;
import com.atlashub.shared.application.usecase.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlashub.integrations.domain.repositories.ConnectionRepository;
import com.atlashub.integrations.application.port.CredentialVaultPort;
import com.atlashub.integrations.application.port.WebhookInbox;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.io.IOException;
import java.util.Map;

@Component
public class ReceivePaystackWebhookHandler extends Command<ReceivePaystackWebhookCommand, Void> {
    private final PaystackWebhookSignatureVerifier verifier;
    private final ObjectMapper mapper;
    private final PaystackWebhookEventPublisher publisher;
    private final ConnectionRepository connections;
    private final CredentialVaultPort vault;
    private final WebhookInbox inbox;

    public ReceivePaystackWebhookHandler(PaystackWebhookSignatureVerifier verifier, ObjectMapper mapper,
                                         PaystackWebhookEventPublisher publisher) {
        this(verifier, mapper, publisher, null, null, null);
    }

    @Autowired
    public ReceivePaystackWebhookHandler(PaystackWebhookSignatureVerifier verifier, ObjectMapper mapper,
                                         PaystackWebhookEventPublisher publisher, ConnectionRepository connections,
                                         CredentialVaultPort vault, WebhookInbox inbox) {
        this.verifier = verifier;
        this.mapper = mapper;
        this.publisher = publisher;
        this.connections = connections;
        this.vault = vault;
        this.inbox = inbox;
    }

    @Override
    public Void execute(ReceivePaystackWebhookCommand command) {
        PaystackEnvironment environment = command.environment();
        if (command.providerConnectionId() != null) {
            var connection = connections.findAuthorization(command.providerConnectionId())
                    .orElseThrow(() -> new IllegalArgumentException("Paystack authorization not found"));
            var owner = connections.findConnection(connection.connectionId())
                    .filter(candidate -> candidate.provider() == Provider.PAYSTACK)
                    .orElseThrow(() -> new IllegalArgumentException("Paystack connection not found"));
            verifier.verify(command.rawBody(), command.signature(), vault.read(connection.credentialReference()));
            environment = connection.environment() == ApiEnvironment.LIVE
                    ? PaystackEnvironment.LIVE : PaystackEnvironment.TEST;
        } else {
            verifier.verify(command.rawBody(), command.signature(), environment);
        }
        try {
            PaystackWebhookPayload payload = mapper.readValue(command.rawBody(), PaystackWebhookPayload.class);
            if (payload.event() == null || payload.event().isBlank() || payload.data() == null || !payload.data().isObject())
                throw new MalformedPaystackWebhookException("Paystack webhook is missing event type or data");
            JsonNode idNode = payload.data().get("id");
            JsonNode referenceNode = payload.data().get("reference");
            String id = idNode != null && !idNode.isNull() ? idNode.asText() :
                    referenceNode == null || referenceNode.isNull() ? null : referenceNode.asText();
            if (id == null || id.isBlank())
                throw new MalformedPaystackWebhookException("Paystack webhook is missing event identity");
            id = payload.event() + ":" + id;
            Map<String,Object> data = mapper.convertValue(payload.data(),
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            if (command.providerConnectionId() != null
                    && !inbox.begin("PAYSTACK", command.providerConnectionId(), id, command.rawBody())) return null;
            publisher.publish(new PaystackWebhookReceivedEvent(id, environment, payload.event(), data,
                    command.providerConnectionId()));
            if (command.providerConnectionId() != null)
                inbox.markPublished("PAYSTACK", command.providerConnectionId(), id);
            return null;
        } catch (MalformedPaystackWebhookException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new MalformedPaystackWebhookException("Paystack webhook payload is not valid JSON", ex);
        }
    }
}
