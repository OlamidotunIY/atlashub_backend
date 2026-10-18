package com.atlashub.logistics.shipping.domain.repositories;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingIncentive;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface MerchantShippingIncentiveRepository extends Repository<MerchantShippingIncentive> {

    Optional<MerchantShippingIncentive> findByOrganizationIdAndBillingYearMonth(Long organizationId, String billingYearMonth);

    List<MerchantShippingIncentive> findByOrganizationId(Long organizationId);
}
