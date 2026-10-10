package com.atlashub.commerce.storefront.infrastructure.persistence.repositories;

import com.atlashub.commerce.storefront.infrastructure.persistence.entities.KotItemJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataKotItemRepository extends JpaRepository<KotItemJpa, Long> {

    List<KotItemJpa> findByKotId(Long kotId);

    void deleteByKotId(Long kotId);
}
