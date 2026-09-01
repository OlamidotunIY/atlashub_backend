package com.atlashub.identity.adapter.out.persistence.repository;

import com.atlashub.identity.adapter.out.persistence.entity.SplitRecipientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataSplitRecipientRepository extends JpaRepository<SplitRecipientJpaEntity, Long> {
    Optional<SplitRecipientJpaEntity> findByIntegrationAndBankCodeAndAccountNumber(Long OrganizationId, String bankCode, String accountNumber);
}
