package com.atlashub.logistics.shipping.domain.repositories;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingConfig;
import com.atlashub.shared.domain.repository.Repository;
import java.util.Optional;

public interface MerchantShippingConfigRepository extends Repository<MerchantShippingConfig> {

    Optional<MerchantShippingConfig> findByOrganizationId(Long organizationId);
}
