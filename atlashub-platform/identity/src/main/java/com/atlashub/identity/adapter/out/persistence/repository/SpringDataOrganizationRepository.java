package com.atlashub.identity.adapter.out.persistence.repository;

import com.atlashub.identity.adapter.out.persistence.entity.OrganizationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataOrganizationRepository extends JpaRepository<OrganizationJpaEntity, Long> {
}
