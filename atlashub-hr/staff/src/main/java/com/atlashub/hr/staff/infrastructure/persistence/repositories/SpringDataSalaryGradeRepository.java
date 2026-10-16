package com.atlashub.hr.staff.infrastructure.persistence.repositories;

import com.atlashub.hr.staff.infrastructure.persistence.entities.SalaryGradeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSalaryGradeRepository extends JpaRepository<SalaryGradeJpaEntity, Long> {

    List<SalaryGradeJpaEntity> findByOrganizationId(Long organizationId);

    Optional<SalaryGradeJpaEntity> findByOrganizationIdAndName(Long organizationId, String name);
}
