package com.atlashub.pay.charges.application.commands.RefundCharge;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.ChargeNotFoundException;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.application.port.ChargeProviderPort;
import org.springframework.stereotype.Component;

@Component
public class RefundChargeHandler extends Command<RefundChargeCommand, RefundChargeResult> {

    private final ChargeRepository chargeRepository;
    private final ChargeProviderPort provider;

    public RefundChargeHandler(ChargeRepository chargeRepository, ChargeProviderPort provider) {
        this.chargeRepository = chargeRepository;
        this.provider = provider;
    }

    @Override
    public RefundChargeResult execute(RefundChargeCommand input) {
        Charge charge = chargeRepository.findById(input.chargeId())
                .orElseThrow(() -> new ChargeNotFoundException(String.valueOf(input.chargeId())));

        if (!charge.getOrganizationId().equals(input.organizationId()) || charge.getEnvironment() != input.environment()) {
            throw new ChargeNotFoundException(String.valueOf(input.chargeId()));
        }

        charge.initiateRefund(input.reason());
        chargeRepository.save(charge);

        try {
            ChargeProviderPort.RefundResult providerResult = provider.refund(new ChargeProviderPort.RefundRequest(
                    charge.getEnvironment(), charge.getProviderReference(), charge.getAmount().amount(),
                    charge.getAmount().currency(), input.reason(), "refund-" + charge.getId()
            ));
            charge.markRefundSubmitted(providerResult.refundReference());
        } catch (RuntimeException error) {
            charge.failRefund(error.getMessage());
            chargeRepository.save(charge);
            throw error;
        }
        Charge saved = chargeRepository.save(charge);

        return new RefundChargeResult(
                saved.getId(),
                saved.getReference(),
                saved.getStatus(),
                input.reason()
        );
    }
}
