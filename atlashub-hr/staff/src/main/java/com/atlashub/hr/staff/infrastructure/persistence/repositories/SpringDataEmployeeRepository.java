package com.atlashub.hr.staff.infrastructure.persistence.repositories;

import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataEmployeeRepository extends JpaRepository<EmployeeJpaEntity, Long> {

    List<EmployeeJpaEntity> findByOrganizationId(Long organizationId);

    List<EmployeeJpaEntity> findByOrganizationIdAndStatus(Long organizationId, EmployeeStatus status);

    List<EmployeeJpaEntity> findByOrganizationIdAndDepartment(Long organizationId, String department);

    List<EmployeeJpaEntity> findByOrganizationIdAndStatusAndDepartment(Long organizationId, EmployeeStatus status, String department);

    Optional<EmployeeJpaEntity> findByOrganizationIdAndUserId(Long organizationId, Long userId);

    Optional<EmployeeJpaEntity> findByOrganizationIdAndEmployeeNumber(Long organizationId, String employeeNumber);

    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);

    Page<EmployeeJpaEntity> findByOrganizationId(Long organizationId, Pageable pageable);
}
