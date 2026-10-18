package com.atlashub.hr.staff.domain.repositories;

import com.atlashub.hr.staff.domain.entities.GradeDeduction;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface GradeDeductionRepository extends Repository<GradeDeduction> {

    List<GradeDeduction> findByGradeId(Long gradeId);
}
