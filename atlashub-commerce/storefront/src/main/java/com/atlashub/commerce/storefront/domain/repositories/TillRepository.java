package com.atlashub.commerce.storefront.domain.repositories;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TillRepository extends Repository<Till> {

    Optional<Till> findActiveTillByOutletId(Long outletId);

    List<Till> findByOutletId(Long outletId);
}
