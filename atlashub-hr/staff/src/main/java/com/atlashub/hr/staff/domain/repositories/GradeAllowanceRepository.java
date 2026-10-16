package com.atlashub.hr.staff.domain.repositories;

import com.atlashub.hr.staff.domain.entities.GradeAllowance;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface GradeAllowanceRepository extends Repository<GradeAllowance> {

    List<GradeAllowance> findByGradeId(Long gradeId);
}
