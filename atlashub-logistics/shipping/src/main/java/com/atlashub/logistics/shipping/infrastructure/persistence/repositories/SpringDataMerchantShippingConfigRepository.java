package com.atlashub.logistics.shipping.infrastructure.persistence.repositories;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.MerchantShippingConfigJpa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMerchantShippingConfigRepository extends JpaRepository<MerchantShippingConfigJpa, Long> {

    Optional<MerchantShippingConfigJpa> findByOrganizationId(Long organizationId);
}
