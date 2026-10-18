package com.atlashub.hr.staff.infrastructure.persistence.repositories;

import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeBankJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataEmployeeBankRepository extends JpaRepository<EmployeeBankJpaEntity, Long> {

    List<EmployeeBankJpaEntity> findByEmployeeId(Long employeeId);

    Optional<EmployeeBankJpaEntity> findByEmployeeIdAndIsPrimaryTrue(Long employeeId);
}
