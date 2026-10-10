package com.atlashub.pay.charges.application.commands.InitializeCharge;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.InvalidChargeException;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.port.PaymentProviderProfileQueryPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InitializeChargeHandler extends Command<InitializeChargeCommand, InitializeChargeResult> {

    private final ChargeRepository chargeRepository;
    private final PaymentProviderProfileQueryPort providerProfileQueryPort;
    private final ChargeProviderPort chargeProviderPort;

    public InitializeChargeHandler(
            ChargeRepository chargeRepository,
            PaymentProviderProfileQueryPort providerProfileQueryPort,
            ChargeProviderPort chargeProviderPort
    ) {
        this.chargeRepository = chargeRepository;
        this.providerProfileQueryPort = providerProfileQueryPort;
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

        PaymentProviderProfileQueryPort.ProviderProfile profile = providerProfileQueryPort
                .findActiveProfile(input.organizationId(), input.environment(), capability)
                .orElseThrow(() -> new InvalidChargeException("Capability not enabled for channel: " + input.channel()));

        Long chargeId = chargeRepository.nextIdentity();

        Charge charge = Charge.initialize(
                chargeId,
                input.organizationId(),
                input.environment(),
                input.reference(),
                input.amount(),
                input.channel(),
                profile.profileId(),
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
                            profile.externalMerchantId(), input.metadata()));
            charge.markPending(providerResult.providerReference(), providerResult.authorizationUrl(),
                    providerResult.accessCode());
            saved = chargeRepository.save(charge);
        } catch (RuntimeException error) {
            charge.fail("Provider initialization failed");
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
