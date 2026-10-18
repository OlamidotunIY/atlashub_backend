package com.atlashub.hr.leave.infrastructure.persistence.repositories;

import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveTypeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataLeaveTypeRepository extends JpaRepository<LeaveTypeJpaEntity, Long> {

    List<LeaveTypeJpaEntity> findByOrganizationId(Long organizationId);
}
