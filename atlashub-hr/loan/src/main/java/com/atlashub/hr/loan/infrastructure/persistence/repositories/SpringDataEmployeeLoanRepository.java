package com.atlashub.hr.loan.infrastructure.persistence.repositories;

import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.hr.loan.infrastructure.persistence.entities.EmployeeLoanJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataEmployeeLoanRepository extends JpaRepository<EmployeeLoanJpaEntity, Long> {

    List<EmployeeLoanJpaEntity> findByEmployeeIdAndStatus(Long employeeId, LoanStatus status);

    List<EmployeeLoanJpaEntity> findByEmployeeIdAndStatusIn(Long employeeId, Collection<LoanStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmployeeLoanJpaEntity e WHERE e.id = :id")
    Optional<EmployeeLoanJpaEntity> findByIdWithLock(@Param("id") Long id);

    List<EmployeeLoanJpaEntity> findByOrganizationId(Long organizationId);

    List<EmployeeLoanJpaEntity> findByOrganizationIdAndStatus(Long organizationId, LoanStatus status);

    List<EmployeeLoanJpaEntity> findByOrganizationIdAndEmployeeIdAndStatus(Long organizationId, Long employeeId, LoanStatus status);

    List<EmployeeLoanJpaEntity> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId);
}
