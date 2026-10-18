package com.atlashub.hr.staff.infrastructure.persistence.repositories;

import com.atlashub.hr.staff.infrastructure.persistence.entities.GradeAllowanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataGradeAllowanceRepository extends JpaRepository<GradeAllowanceJpaEntity, Long> {

    List<GradeAllowanceJpaEntity> findByGradeId(Long gradeId);
}
