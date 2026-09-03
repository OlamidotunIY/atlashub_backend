package com.atlashub.billing.domain.repository;

import com.atlashub.billing.domain.model.OrganizationProduct;
import com.atlashub.shared.domain.repository.Repository;

public interface OrganizationProductRepository extends Repository<OrganizationProduct> {
    Long nextIdentity();
}