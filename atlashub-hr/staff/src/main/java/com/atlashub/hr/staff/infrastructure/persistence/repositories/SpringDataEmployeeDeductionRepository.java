package com.atlashub.hr.staff.infrastructure.persistence.repositories;

import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeDeductionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataEmployeeDeductionRepository extends JpaRepository<EmployeeDeductionJpaEntity, Long> {

    List<EmployeeDeductionJpaEntity> findByEmployeeId(Long employeeId);
}
