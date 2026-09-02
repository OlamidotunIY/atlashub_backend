package com.atlashub.billing.domain.repository;

import com.atlashub.billing.domain.model.OrganizationProduct;

public interface OrganizationProductRepository {
    Long nextIdentity();
    OrganizationProduct save(OrganizationProduct organizationProduct);
}
