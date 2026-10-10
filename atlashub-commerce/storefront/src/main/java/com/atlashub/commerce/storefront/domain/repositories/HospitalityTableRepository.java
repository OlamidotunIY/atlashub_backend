package com.atlashub.commerce.storefront.domain.repositories;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface HospitalityTableRepository extends Repository<HospitalityTable> {

    Optional<HospitalityTable> findByOutletIdAndTableNumber(Long outletId, String tableNumber);

    List<HospitalityTable> findByOutletId(Long outletId);

    List<HospitalityTable> findByOutletIdAndStatus(Long outletId, TableStatus status);
}
