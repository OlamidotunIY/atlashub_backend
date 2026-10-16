package com.atlashub.pay.mandates.application.commands.CreateMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateMandateHandler extends Command<CreateMandateCommand, CreateMandateResponse> {

    private final PaymentMandateRepository mandateRepository;

    public CreateMandateHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional
    public CreateMandateResponse execute(CreateMandateCommand input) {
        Long id = mandateRepository.nextIdentity();
        String currency = input.currency() != null ? input.currency() : "NGN";
        Money money = Money.of(input.amount(), CurrencyCode.valueOf(currency));
        EmailAddress email = new EmailAddress(input.email());

        PaymentMandate mandate = PaymentMandate.create(
                id,
                input.organizationId(),
                input.customerId(),
                email,
                money,
                input.frequency(),
                input.authorizationCode(),
                input.firstChargeDate()
        );

        mandateRepository.save(mandate);

        return new CreateMandateResponse(
                mandate.getId(),
                mandate.getStatus().name(),
                mandate.getNextChargeDate()
        );
    }
}
