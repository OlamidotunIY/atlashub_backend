package com.atlashub.billing.adapter.out.persistence.repository;

import com.atlashub.billing.adapter.out.persistence.entity.BillingInvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataBillingInvoiceRepository extends JpaRepository<BillingInvoiceJpaEntity, Long> {
    List<BillingInvoiceJpaEntity> findByOrganizationProductId(Long organizationProductId);
}
