package com.atlashub.integrations.application.command.ReceiveStripeWebhook;

import com.atlashub.integrations.application.port.CredentialVaultPort;
import com.atlashub.integrations.application.port.StripeWebhookVerifierPort;
import com.atlashub.integrations.application.port.WebhookInbox;
import com.atlashub.integrations.domain.repositories.ConnectionRepository;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReceiveStripeWebhookHandler extends Command<ReceiveStripeWebhookCommand, Void> {
    private final StripeWebhookVerifierPort verifier;
    private final ConnectionRepository connections;
    private final WebhookInbox inbox;
    private final CredentialVaultPort vault;

    public ReceiveStripeWebhookHandler(StripeWebhookVerifierPort verifier, ConnectionRepository connections,
                                       WebhookInbox inbox, CredentialVaultPort vault) {
        this.verifier = verifier;
        this.connections = connections;
        this.inbox = inbox;
        this.vault = vault;
    }

    @Override
    @Transactional
    public Void execute(ReceiveStripeWebhookCommand command) {
        var event = verifier.verify(command.payload(), command.signature());
        if (!"account.application.deauthorized".equals(event.eventType())) return null;
        if (event.externalAccountId() == null || event.externalAccountId().isBlank()) {
            throw new IllegalArgumentException("Stripe account is missing");
        }
        var authorization = connections.findAuthorization(event.environment(), event.externalAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Stripe authorization not found"));
        connections.findConnection(authorization.connectionId())
                .filter(connection -> connection.provider() == Provider.STRIPE).orElseThrow();
        if (!inbox.begin("STRIPE", authorization.id(), event.eventId(), command.payload())) return null;
        vault.delete(authorization.credentialReference());
        connections.saveAuthorization(authorization.revoked());
        inbox.markPublished("STRIPE", authorization.id(), event.eventId());
        return null;
    }
}
