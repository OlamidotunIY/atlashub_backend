package com.atlashub.hr.staff.domain.repositories;

import com.atlashub.hr.staff.domain.entities.SalaryGrade;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface SalaryGradeRepository extends Repository<SalaryGrade> {

    List<SalaryGrade> findByOrganizationId(Long organizationId);

    Optional<SalaryGrade> findByOrganizationIdAndName(Long organizationId, String name);
}
