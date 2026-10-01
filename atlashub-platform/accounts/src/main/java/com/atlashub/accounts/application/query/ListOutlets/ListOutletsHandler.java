package com.atlashub.accounts.application.query.ListOutlets;

import com.atlashub.accounts.application.query.GetOutlet.OutletResult;
import com.atlashub.accounts.domain.repositories.OutletRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListOutletsHandler extends Query<ListOutletsQuery, List<OutletResult>> {

    private final OutletRepository outletRepository;

    public ListOutletsHandler(OutletRepository outletRepository) {
        this.outletRepository = outletRepository;
    }

    @Override
    public List<OutletResult> execute(ListOutletsQuery query) {
        return outletRepository.findAllByOrganizationId(query.organizationId()).stream()
                .map(o -> new OutletResult(
                        o.getId(),
                        o.getOrganizationId(),
                        o.getName(),
                        o.getAddress(),
                        o.getCity(),
                        o.getState(),
                        o.getCountry().code(),
                        o.getCurrency().name(),
                        o.getManagerId(),
                        o.getStatus(),
                        o.getCreatedAt(),
                        o.getUpdatedAt()
                ))
                .toList();
    }
}
