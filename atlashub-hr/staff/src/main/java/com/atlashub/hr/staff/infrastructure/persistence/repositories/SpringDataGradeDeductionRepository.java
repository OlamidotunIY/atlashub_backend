package com.atlashub.hr.staff.infrastructure.persistence.repositories;

import com.atlashub.hr.staff.infrastructure.persistence.entities.GradeDeductionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataGradeDeductionRepository extends JpaRepository<GradeDeductionJpaEntity, Long> {

    List<GradeDeductionJpaEntity> findByGradeId(Long gradeId);
}
