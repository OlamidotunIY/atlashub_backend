package com.atlashub.hr.staff.application.query.ListSalaryGrades;

import com.atlashub.hr.staff.domain.entities.SalaryGrade;
import com.atlashub.hr.staff.domain.repositories.GradeAllowanceRepository;
import com.atlashub.hr.staff.domain.repositories.GradeDeductionRepository;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSalaryGradesHandler extends Query<ListSalaryGradesQuery, List<SalaryGradeResult>> {

    private final SalaryGradeRepository salaryGradeRepository;
    private final GradeAllowanceRepository gradeAllowanceRepository;
    private final GradeDeductionRepository gradeDeductionRepository;

    public ListSalaryGradesHandler(
            SalaryGradeRepository salaryGradeRepository,
            GradeAllowanceRepository gradeAllowanceRepository,
            GradeDeductionRepository gradeDeductionRepository
    ) {
        this.salaryGradeRepository = salaryGradeRepository;
        this.gradeAllowanceRepository = gradeAllowanceRepository;
        this.gradeDeductionRepository = gradeDeductionRepository;
    }

    @Override
    public List<SalaryGradeResult> execute(ListSalaryGradesQuery query) {
        List<SalaryGrade> grades = salaryGradeRepository.findByOrganizationId(query.organizationId());

        return grades.stream().map(grade -> {
            List<SalaryGradeResult.GradeAllowanceItem> allowances = gradeAllowanceRepository.findByGradeId(grade.getId())
                    .stream()
                    .map(a -> new SalaryGradeResult.GradeAllowanceItem(a.getId(), a.getName(), a.getType(), a.getValue()))
                    .toList();

            List<SalaryGradeResult.GradeDeductionItem> deductions = gradeDeductionRepository.findByGradeId(grade.getId())
                    .stream()
                    .map(d -> new SalaryGradeResult.GradeDeductionItem(d.getId(), d.getName(), d.getType(), d.getValue(), d.isStatutory()))
                    .toList();

            return new SalaryGradeResult(
                    grade.getId(),
                    grade.getOrganizationId(),
                    grade.getName(),
                    grade.getBaseSalary().amount(),
                    grade.getBaseSalary().currency().name(),
                    allowances,
                    deductions
            );
        }).toList();
    }
}
