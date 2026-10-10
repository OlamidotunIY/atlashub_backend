package com.atlashub.commerce.storefront.application.queries.ListActiveTables;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.domain.repositories.HospitalityTableRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListActiveTablesHandler extends Query<ListActiveTablesQuery, List<TableResult>> {

    private final HospitalityTableRepository tableRepository;

    public ListActiveTablesHandler(HospitalityTableRepository tableRepository) {
        this.tableRepository = Objects.requireNonNull(tableRepository, "HospitalityTableRepository must not be null");
    }

    @Override
    public List<TableResult> execute(ListActiveTablesQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        List<HospitalityTable> tables = tableRepository.findByOutletId(query.outletId());

        return tables.stream()
                .map(this::toResult)
                .toList();
    }

    private TableResult toResult(HospitalityTable table) {
        return new TableResult(
                table.getId(),
                table.getOrganizationId(),
                table.getOutletId(),
                table.getTableNumber(),
                table.getCovers(),
                table.getStatus(),
                table.getCurrentOrderId()
        );
    }
}
