package com.atlashub.logistics.shipping.application.queries.ListMerchantShippingIncentives;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingIncentive;
import com.atlashub.logistics.shipping.domain.repositories.MerchantShippingIncentiveRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListMerchantShippingIncentivesHandler extends Query<ListMerchantShippingIncentivesQuery, List<MerchantShippingIncentiveSummary>> {

    private final MerchantShippingIncentiveRepository incentiveRepository;

    public ListMerchantShippingIncentivesHandler(MerchantShippingIncentiveRepository incentiveRepository) {
        this.incentiveRepository = incentiveRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantShippingIncentiveSummary> execute(ListMerchantShippingIncentivesQuery input) {
        List<MerchantShippingIncentive> incentives = incentiveRepository.findByOrganizationId(input.organizationId());

        return incentives.stream()
                .map(i -> new MerchantShippingIncentiveSummary(
                        i.getId(),
                        i.getBillingYearMonth(),
                        i.getEligibleShipmentCount(),
                        i.getRebateAmount(),
                        i.getStatus().name(),
                        i.getCreditedAt()
                ))
                .toList();
    }
}
