package com.atlashub.accounting.ar.application.query.GetOpenReceivables;

import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.repositories.CustomerReceivableRepository;
import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetOpenReceivablesHandler extends Query<GetOpenReceivablesQuery, List<CustomerReceivableResult>> {

    private final CustomerReceivableRepository receivableRepository;

    public GetOpenReceivablesHandler(CustomerReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    @Override
    public List<CustomerReceivableResult> execute(GetOpenReceivablesQuery query) {
        List<CustomerReceivable> allReceivables = receivableRepository.findByOrganizationId(query.organizationId());

        return allReceivables.stream()
                .filter(r -> r.getStatus() == ArStatus.OUTSTANDING || r.getStatus() == ArStatus.PARTIALLY_COLLECTED)
                .map(this::toResult)
                .toList();
    }

    private CustomerReceivableResult toResult(CustomerReceivable receivable) {
        return new CustomerReceivableResult(
                receivable.getId(),
                receivable.getOrganizationId(),
                receivable.getCustomerId(),
                receivable.getSalesOrderId(),
                receivable.getAmount(),
                receivable.getPaidAmount(),
                receivable.getOutstandingBalance(),
                receivable.getDueDate(),
                receivable.getStatus(),
                receivable.getCreatedAt()
        );
    }
}
