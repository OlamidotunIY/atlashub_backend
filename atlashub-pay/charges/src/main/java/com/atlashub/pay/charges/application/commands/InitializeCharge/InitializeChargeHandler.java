package com.atlashub.pay.charges.application.commands.InitializeCharge;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.InvalidChargeException;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.port.ProviderConnectionQueryPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InitializeChargeHandler extends Command<InitializeChargeCommand, InitializeChargeResult> {

    private final ChargeRepository chargeRepository;
    private final ProviderConnectionQueryPort providerConnectionQueryPort;
    private final ChargeProviderPort chargeProviderPort;

    public InitializeChargeHandler(
            ChargeRepository chargeRepository,
            ProviderConnectionQueryPort providerConnectionQueryPort,
            ChargeProviderPort chargeProviderPort
    ) {
        this.chargeRepository = chargeRepository;
        this.providerConnectionQueryPort = providerConnectionQueryPort;
        this.chargeProviderPort = chargeProviderPort;
    }

    @Override
    public InitializeChargeResult execute(InitializeChargeCommand input) {
        Optional<Charge> existing = chargeRepository.findByOrganizationIdAndEnvironmentAndReference(
                input.organizationId(),
                input.environment(),
                input.reference()
        );

        if (existing.isPresent()) {
            return toResult(existing.get());
        }

        String capability = switch (input.channel()) {
            case CARD -> "CARD_COLLECTION";
            case USSD -> "USSD_COLLECTION";
        };

        List<ProviderConnectionQueryPort.ProviderConnection> eligible = providerConnectionQueryPort.findEligible(
                input.organizationId(), input.environment(), input.amount().currency().name(), capability);
        ProviderConnectionQueryPort.ProviderConnection connection;
        if (input.providerConnectionId() != null) {
            connection = eligible.stream().filter(candidate ->
                            candidate.connectionId().equals(input.providerConnectionId()))
                    .findFirst().orElseThrow(() -> new InvalidChargeException(
                            "Selected provider connection is not eligible for this payment"));
        } else if (eligible.size() == 1) {
            connection = eligible.getFirst();
        } else if (eligible.isEmpty()) {
            throw new InvalidChargeException("Capability not enabled for channel: " + input.channel());
        } else {
            throw new InvalidChargeException("Select a provider connection for this payment");
        }

        Long chargeId = chargeRepository.nextIdentity();

        Charge charge = Charge.initialize(
                chargeId,
                input.organizationId(),
                input.environment(),
                input.reference(),
                input.amount(),
                input.channel(),
                com.atlashub.pay.charges.domain.valueobject.PaymentProvider.valueOf(connection.provider()),
                connection.connectionId(),
                input.sourceSystem(),
                input.sourceReferenceId(),
                input.customerReferenceId()
        );

        chargeRepository.save(charge);
        Charge saved;
        try {
            ChargeProviderPort.InitializationResult providerResult = chargeProviderPort.initialize(
                    new ChargeProviderPort.InitializationRequest(
                            input.environment(), input.email(), input.amount().amount(), input.amount().currency(),
                            ChargeProviderPort.ChargeChannel.valueOf(input.channel().name()), input.reference(),
                            connection.externalMerchantId(), input.metadata(), connection.connectionId()));
            charge.markPending(providerResult.providerReference(), providerResult.authorizationUrl(),
                    providerResult.accessCode());
            saved = chargeRepository.save(charge);
        } catch (RuntimeException error) {
            charge.markSubmissionUnknown();
            chargeRepository.save(charge);
            throw error;
        }
        return toResult(saved);
    }

    private InitializeChargeResult toResult(Charge charge) {
        return new InitializeChargeResult(
                charge.getId(),
                charge.getOrganizationId(),
                charge.getEnvironment(),
                charge.getReference(),
                charge.getAmount(),
                charge.getChannel(),
                charge.getStatus(),
                charge.getProviderReference(),
                charge.getAuthorizationUrl(),
                charge.getAccessCode(),
                charge.getExpiresAt()
        );
    }
}
