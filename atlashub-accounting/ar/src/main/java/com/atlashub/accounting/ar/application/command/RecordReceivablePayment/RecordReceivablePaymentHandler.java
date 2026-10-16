package com.atlashub.accounting.ar.application.command.RecordReceivablePayment;

import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.exceptions.ReceivableNotFoundException;
import com.atlashub.accounting.ar.domain.repositories.CustomerReceivableRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class RecordReceivablePaymentHandler extends Command<RecordReceivablePaymentCommand, Void> {

    private final CustomerReceivableRepository receivableRepository;

    public RecordReceivablePaymentHandler(CustomerReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    @Override
    public Void execute(RecordReceivablePaymentCommand command) {
        CustomerReceivable receivable = receivableRepository.findById(command.receivableId())
                .orElseThrow(() -> new ReceivableNotFoundException(command.receivableId()));

        receivable.recordPayment(command.amountPaid());
        receivableRepository.save(receivable);
        return null;
    }
}
