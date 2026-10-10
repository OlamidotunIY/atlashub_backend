package com.atlashub.pay.settlement.application.queries.ListSettlements;

import com.atlashub.pay.settlement.application.queries.GetSettlementDetails.SettlementResult;
import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListSettlementsHandler extends Query<ListSettlementsQuery, PageResult<SettlementResult>> {

    private final SettlementRepository settlementRepository;

    public ListSettlementsHandler(SettlementRepository settlementRepository) {
        this.settlementRepository =
                Objects.requireNonNull(settlementRepository, "SettlementRepository must not be null");
    }

    @PreAuthorize("hasAuthority('pay:settlement:manage')")
    @Override
    public PageResult<SettlementResult> execute(ListSettlementsQuery query) {
        PageResult<Settlement> pageResult =
                settlementRepository.findByOrganizationId(query.organizationId(), query.environment(), query.status(), query.dateFrom(),
                        query.dateTo(), query.page(), query.size());

        List<SettlementResult> mappedContent = pageResult.content().stream()
                .map(settlement -> new SettlementResult(settlement.getId(), settlement.getOrganizationId(),
                        settlement.getProvider(), settlement.getProviderSettlementId(), settlement.getNetAmount(),
                        settlement.getSettledAt(), settlement.getStatus(), settlement.getDescription())).toList();

        return new PageResult<>(mappedContent, pageResult.pageNumber(), pageResult.pageSize(),
                pageResult.totalElements(), pageResult.totalPages());
    }
}
