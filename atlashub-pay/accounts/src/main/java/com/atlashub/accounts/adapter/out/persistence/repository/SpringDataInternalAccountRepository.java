package com.atlashub.accounts.adapter.out.persistence.repository;

import com.atlashub.accounts.adapter.out.persistence.entity.InternalAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataInternalAccountRepository extends JpaRepository<InternalAccountEntity, Long> {
    List<InternalAccountEntity> findByOrganizationId(Long organizationId);
    Optional<InternalAccountEntity> findByOrganizationIdAndType(Long organizationId, String type);
    boolean existsByOrganizationIdAndType(Long organizationId, String type);
}
