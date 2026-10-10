package com.atlashub.commerce.catalog.domain.repositories;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.PageResult;

public interface PurchaseOrderRepository extends Repository<PurchaseOrder> {

    PageResult<PurchaseOrder> findByOrganizationId(Long organizationId, PurchaseOrderStatus status, int page, int size);
}
