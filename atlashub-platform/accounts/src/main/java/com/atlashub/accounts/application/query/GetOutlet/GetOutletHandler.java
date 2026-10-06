package com.atlashub.accounts.application.query.GetOutlet;

import com.atlashub.accounts.domain.exceptions.OutletNotFoundException;
import com.atlashub.accounts.domain.repositories.OutletRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class GetOutletHandler extends Query<GetOutletQuery, OutletResult> {

    private final OutletRepository outletRepository;

    public GetOutletHandler(OutletRepository outletRepository) {
        this.outletRepository = outletRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounts:outlets:read')")
    public OutletResult execute(GetOutletQuery query) {
        var outlet = outletRepository.findById(query.outletId()).filter(found -> found.getOrganizationId().equals(query.organizationId())).orElseThrow(() -> new OutletNotFoundException("Outlet not found: " + query.outletId()));

        return new OutletResult(outlet.getId(), outlet.getOrganizationId(), outlet.getName(), outlet.getAddress(), outlet.getCity(), outlet.getState(), outlet.getCountry().code(), outlet.getCurrency().name(), outlet.getManagerId(), outlet.getStatus(), outlet.getCreatedAt(), outlet.getUpdatedAt());
    }
}
