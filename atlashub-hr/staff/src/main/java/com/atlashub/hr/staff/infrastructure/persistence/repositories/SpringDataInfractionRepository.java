package com.atlashub.hr.staff.infrastructure.persistence.repositories;

import com.atlashub.hr.staff.infrastructure.persistence.entities.InfractionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataInfractionRepository extends JpaRepository<InfractionJpaEntity, Long> {

    List<InfractionJpaEntity> findByOrganizationId(Long organizationId);

    List<InfractionJpaEntity> findByEmployeeId(Long employeeId);
}
