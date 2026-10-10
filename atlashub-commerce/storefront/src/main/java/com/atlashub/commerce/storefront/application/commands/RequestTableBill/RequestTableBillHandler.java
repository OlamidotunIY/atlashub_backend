package com.atlashub.commerce.storefront.application.commands.RequestTableBill;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.domain.exceptions.TableNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.HospitalityTableRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RequestTableBillHandler extends Command<RequestTableBillCommand, Void> {

    private final HospitalityTableRepository tableRepository;

    public RequestTableBillHandler(HospitalityTableRepository tableRepository) {
        this.tableRepository = Objects.requireNonNull(tableRepository, "HospitalityTableRepository must not be null");
    }

    @Override
    public Void execute(RequestTableBillCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        HospitalityTable table = tableRepository.findById(command.tableId())
                .orElseThrow(() -> new TableNotFoundException(command.tableId()));

        table.requestBill();
        tableRepository.save(table);
        return null;
    }
}
