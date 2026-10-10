package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.infrastructure.persistence.entities.ReturnItemJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataReturnItemRepository extends JpaRepository<ReturnItemJpa, Long> {

    List<ReturnItemJpa> findByReturnId(Long returnId);

    List<ReturnItemJpa> findByReturnIdIn(List<Long> returnIds);

    void deleteByReturnId(Long returnId);
}
