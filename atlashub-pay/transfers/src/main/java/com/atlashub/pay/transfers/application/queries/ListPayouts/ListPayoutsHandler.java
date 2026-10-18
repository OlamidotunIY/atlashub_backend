package com.atlashub.pay.transfers.application.queries.ListPayouts;

import com.atlashub.pay.transfers.application.queries.GetPayout.GetPayoutHandler;
import com.atlashub.pay.transfers.application.queries.GetPayout.PayoutResult;
import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListPayoutsHandler extends Query<ListPayoutsQuery, PageResult<PayoutResult>> {

    private final PayoutRepository payoutRepository;

    public ListPayoutsHandler(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<PayoutResult> execute(ListPayoutsQuery query) {
        PageResult<Payout> pageResult;
        if (query.status() != null) {
            pageResult = payoutRepository.findByOrganizationIdAndStatus(
                    query.organizationId(), query.status(), query.page(), query.size()
            );
        } else {
            pageResult = payoutRepository.findByOrganizationId(
                    query.organizationId(), query.page(), query.size()
            );
        }

        List<PayoutResult> items = pageResult.content().stream()
                .map(GetPayoutHandler::toResult)
                .collect(Collectors.toList());

        return new PageResult<>(
                items,
                pageResult.pageNumber(),
                pageResult.pageSize(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
