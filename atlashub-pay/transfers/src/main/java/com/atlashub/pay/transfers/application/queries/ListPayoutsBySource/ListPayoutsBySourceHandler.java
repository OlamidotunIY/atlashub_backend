package com.atlashub.pay.transfers.application.queries.ListPayoutsBySource;

import com.atlashub.pay.transfers.application.queries.GetPayout.GetPayoutHandler;
import com.atlashub.pay.transfers.application.queries.GetPayout.PayoutResult;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListPayoutsBySourceHandler extends Query<ListPayoutsBySourceQuery, List<PayoutResult>> {

    private final PayoutRepository payoutRepository;

    public ListPayoutsBySourceHandler(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayoutResult> execute(ListPayoutsBySourceQuery query) {
        return payoutRepository.findBySourceSystemAndSourceReferenceId(query.sourceSystem(), query.sourceReferenceId())
                .stream()
                .map(GetPayoutHandler::toResult)
                .collect(Collectors.toList());
    }
}
