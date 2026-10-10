package com.atlashub.commerce.storefront.application.commands.OccupyTable;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.domain.exceptions.TableNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.HospitalityTableRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OccupyTableHandler extends Command<OccupyTableCommand, Void> {

    private final HospitalityTableRepository tableRepository;

    public OccupyTableHandler(HospitalityTableRepository tableRepository) {
        this.tableRepository = Objects.requireNonNull(tableRepository, "HospitalityTableRepository must not be null");
    }

    @Override
    public Void execute(OccupyTableCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        HospitalityTable table = tableRepository.findById(command.tableId())
                .orElseThrow(() -> new TableNotFoundException(command.tableId()));

        table.occupy(command.salesOrderId(), command.covers());
        tableRepository.save(table);
        return null;
    }
}
