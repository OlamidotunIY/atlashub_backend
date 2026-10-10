package com.atlashub.pay.charges.application.commands.ReconcilePendingCharges;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReconcilePendingChargesHandler extends Command<ReconcilePendingChargesCommand, ReconcilePendingChargesResult> {

    private static final Logger log = LoggerFactory.getLogger(ReconcilePendingChargesHandler.class);

    private final ChargeRepository chargeRepository;
    private final ChargeProviderPort chargeProviderPort;

    public ReconcilePendingChargesHandler(
            ChargeRepository chargeRepository,
            ChargeProviderPort chargeProviderPort
    ) {
        this.chargeRepository = chargeRepository;
        this.chargeProviderPort = chargeProviderPort;
    }

    @Override
    public ReconcilePendingChargesResult execute(ReconcilePendingChargesCommand input) {
        List<Charge> pendingCharges = chargeRepository.findPendingCharges(input.createdBefore());
        int reconciledCount = 0;
        int failureCount = 0;

        for (Charge charge : pendingCharges) {
            String providerRef = charge.getProviderReference();
            if (providerRef == null || providerRef.isBlank()) {
                continue;
            }

            try {
                ChargeProviderPort.ProviderStatus status = chargeProviderPort.fetchStatus(
                        providerRef,
                        charge.getEnvironment()
                );

                if ("success".equalsIgnoreCase(status.status())) {
                    charge.succeed(
                            status.providerReference(),
                            Money.of(status.amount(), status.currency()),
                            status.currency()
                    );
                    chargeRepository.save(charge);
                    reconciledCount++;
                } else if ("failed".equalsIgnoreCase(status.status()) || "abandoned".equalsIgnoreCase(status.status())) {
                    charge.fail("Provider reported status: " + status.status());
                    chargeRepository.save(charge);
                    reconciledCount++;
                }
            } catch (Exception ex) {
                log.warn("Failed to reconcile pending charge id {}: {}", charge.getId(), ex.getMessage());
                failureCount++;
            }
        }

        return new ReconcilePendingChargesResult(reconciledCount, failureCount);
    }
}
