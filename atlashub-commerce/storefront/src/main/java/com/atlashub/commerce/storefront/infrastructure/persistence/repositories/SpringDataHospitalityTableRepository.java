package com.atlashub.commerce.storefront.infrastructure.persistence.repositories;

import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.HospitalityTableJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataHospitalityTableRepository extends JpaRepository<HospitalityTableJpa, Long> {

    Optional<HospitalityTableJpa> findByOutletIdAndTableNumber(Long outletId, String tableNumber);

    List<HospitalityTableJpa> findByOutletId(Long outletId);

    List<HospitalityTableJpa> findByOutletIdAndStatus(Long outletId, TableStatus status);
}
