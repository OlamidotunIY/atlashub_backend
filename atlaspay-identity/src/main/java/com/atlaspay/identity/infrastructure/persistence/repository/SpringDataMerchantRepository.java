package com.atlaspay.identity.infrastructure.persistence.repository;

import com.atlaspay.identity.infrastructure.persistence.entity.MerchantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataMerchantRepository extends JpaRepository<MerchantJpaEntity, Long> {
    Optional<MerchantJpaEntity> findByEmail(String email);
}
