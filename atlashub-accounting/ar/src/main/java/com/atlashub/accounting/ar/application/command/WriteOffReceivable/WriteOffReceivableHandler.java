package com.atlashub.accounting.ar.application.command.WriteOffReceivable;

import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.exceptions.ReceivableNotFoundException;
import com.atlashub.accounting.ar.domain.repositories.CustomerReceivableRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class WriteOffReceivableHandler extends Command<WriteOffReceivableCommand, Void> {

    private final CustomerReceivableRepository receivableRepository;

    public WriteOffReceivableHandler(CustomerReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    @Override
    public Void execute(WriteOffReceivableCommand command) {
        CustomerReceivable receivable = receivableRepository.findById(command.receivableId())
                .orElseThrow(() -> new ReceivableNotFoundException(command.receivableId()));

        receivable.writeOff(command.reason());
        receivableRepository.save(receivable);
        return null;
    }
}
