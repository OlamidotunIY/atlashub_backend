package com.atlashub.hr.payroll.infrastructure.persistence.repositories;

import com.atlashub.hr.payroll.infrastructure.persistence.entities.PayslipLineItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataPayslipLineItemRepository extends JpaRepository<PayslipLineItemJpaEntity, Long> {

    List<PayslipLineItemJpaEntity> findByPayslipId(Long payslipId);
}
