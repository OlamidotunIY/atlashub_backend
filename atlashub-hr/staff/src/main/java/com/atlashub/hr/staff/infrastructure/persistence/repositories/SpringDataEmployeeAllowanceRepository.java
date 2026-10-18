package com.atlashub.hr.staff.infrastructure.persistence.repositories;

import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeAllowanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataEmployeeAllowanceRepository extends JpaRepository<EmployeeAllowanceJpaEntity, Long> {

    List<EmployeeAllowanceJpaEntity> findByEmployeeId(Long employeeId);
}
