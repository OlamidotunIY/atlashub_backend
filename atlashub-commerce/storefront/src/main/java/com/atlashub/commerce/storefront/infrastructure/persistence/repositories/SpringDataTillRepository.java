package com.atlashub.commerce.storefront.infrastructure.persistence.repositories;

import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.TillJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataTillRepository extends JpaRepository<TillJpa, Long> {

    Optional<TillJpa> findByOutletIdAndStatus(Long outletId, TillStatus status);

    List<TillJpa> findByOutletId(Long outletId);
}
