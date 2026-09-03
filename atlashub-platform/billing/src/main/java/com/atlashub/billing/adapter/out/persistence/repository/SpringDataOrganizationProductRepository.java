package com.atlashub.billing.adapter.out.persistence.repository;

import com.atlashub.billing.adapter.out.persistence.entity.OrganizationProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataOrganizationProductRepository extends JpaRepository<OrganizationProductJpaEntity, Long> {
    List<OrganizationProductJpaEntity> findByOrganizationId(Long organizationId);
}
