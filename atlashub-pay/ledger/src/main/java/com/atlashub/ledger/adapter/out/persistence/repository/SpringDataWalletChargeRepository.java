package com.atlashub.ledger.adapter.out.persistence.repository;

import com.atlashub.ledger.adapter.out.persistence.entity.WalletChargeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataWalletChargeRepository extends JpaRepository<WalletChargeJpaEntity, Long> {
}
