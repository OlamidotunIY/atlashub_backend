package com.atlashub.commerce.storefront.application.queries.GetTillSummary;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.commerce.storefront.domain.exceptions.TillNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.TillRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetTillSummaryHandler extends Query<GetTillSummaryQuery, TillSummaryResult> {

    private final TillRepository tillRepository;

    public GetTillSummaryHandler(TillRepository tillRepository) {
        this.tillRepository = Objects.requireNonNull(tillRepository, "TillRepository must not be null");
    }

    @Override
    public TillSummaryResult execute(GetTillSummaryQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        Till till = tillRepository.findById(query.tillId())
                .orElseThrow(() -> new TillNotFoundException(query.tillId()));

        return new TillSummaryResult(
                till.getId(),
                till.getOrganizationId(),
                till.getOutletId(),
                till.getName(),
                till.getOpeningFloat(),
                till.getExpectedClosingBalance(),
                till.getActualClosingBalance(),
                till.getStatus(),
                till.getOpenedAt(),
                till.getClosedAt(),
                till.getOpenedBy(),
                till.getClosedBy()
        );
    }
}
