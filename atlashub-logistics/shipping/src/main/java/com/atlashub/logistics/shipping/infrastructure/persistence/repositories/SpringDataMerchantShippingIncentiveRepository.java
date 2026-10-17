package com.atlashub.logistics.shipping.infrastructure.persistence.repositories;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.MerchantShippingIncentiveJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMerchantShippingIncentiveRepository extends JpaRepository<MerchantShippingIncentiveJpa, Long> {

    Optional<MerchantShippingIncentiveJpa> findByOrganizationIdAndBillingYearMonth(Long organizationId, String billingYearMonth);

    List<MerchantShippingIncentiveJpa> findByOrganizationId(Long organizationId);
}
