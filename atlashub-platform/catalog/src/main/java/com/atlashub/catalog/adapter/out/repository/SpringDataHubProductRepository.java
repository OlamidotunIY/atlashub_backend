package com.atlashub.catalog.adapter.out.repository;

import com.atlashub.catalog.adapter.out.entity.HubProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataHubProductRepository extends JpaRepository<HubProductJpaEntity, Long> {
    List<HubProductJpaEntity> findByStatus(String status);
}
