package com.atlashub.commerce.inventory.domain.repositories;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.PageResult;

import java.util.List;

public interface StockTransferRepository extends Repository<StockTransfer> {

    List<StockTransfer> findByOrganizationIdAndStatus(Long orgId, TransferStatus status);

    PageResult<StockTransfer> findByOrganizationId(Long orgId, TransferStatus status, int page, int size);
}
