package com.atlashub.pay.charges.application.commands.ApplyChargeProviderStatus;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.ChargeNotFoundException;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplyChargeProviderStatusHandler extends Command<ApplyChargeProviderStatusCommand, Void> {

    private final ChargeRepository chargeRepository;
    private final ChargeProviderPort providerPort;

    public ApplyChargeProviderStatusHandler(ChargeRepository chargeRepository, ChargeProviderPort providerPort) {
        this.chargeRepository = chargeRepository;
        this.providerPort = providerPort;
    }

    @Override
    public Void execute(ApplyChargeProviderStatusCommand input) {
        Charge charge = null;

        if (input.providerReference() != null && !input.providerReference().isBlank()) {
            Optional<Charge> found = chargeRepository.findByProviderReferenceAndEnvironment(
                    input.providerReference(),
                    input.environment()
            );
            if (found.isPresent()) {
                charge = found.get();
            }
        }

        if (charge == null) {
            String refIdentifier = input.providerReference() != null ? input.providerReference() : input.reference();
            throw new ChargeNotFoundException(refIdentifier != null ? refIdentifier : "unknown");
        }

        ChargeProviderPort.ProviderStatus verified = providerPort.fetchStatus(charge.getProviderReference(),
                input.environment());
        if ("success".equalsIgnoreCase(verified.status())) {
            charge.succeed(verified.providerReference(),
                    new com.atlashub.shared.domain.valueobject.Money(verified.amount(), verified.currency()),
                    verified.currency(), new com.atlashub.shared.domain.valueobject.Money(
                            verified.providerFee(), verified.currency()));
        } else if (!input.successful() && ("failed".equalsIgnoreCase(verified.status()) ||
                "abandoned".equalsIgnoreCase(verified.status()))) {
            String failureReason = input.failureReason() != null && !input.failureReason().isBlank()
                    ? input.failureReason()
                    : "Payment failed at provider";
            charge.fail(failureReason);
        } else {
            return null;
        }

        chargeRepository.save(charge);
        return null;
    }
}
