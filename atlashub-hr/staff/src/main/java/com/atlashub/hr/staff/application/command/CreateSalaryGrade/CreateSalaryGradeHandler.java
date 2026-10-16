package com.atlashub.hr.staff.application.command.CreateSalaryGrade;

import com.atlashub.hr.staff.domain.entities.SalaryGrade;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class CreateSalaryGradeHandler extends Command<CreateSalaryGradeCommand, CreateSalaryGradeResponse> {

    private final SalaryGradeRepository salaryGradeRepository;

    public CreateSalaryGradeHandler(SalaryGradeRepository salaryGradeRepository) {
        this.salaryGradeRepository = salaryGradeRepository;
    }

    @Override
    public CreateSalaryGradeResponse execute(CreateSalaryGradeCommand input) {
        Long gradeId = salaryGradeRepository.nextIdentity();
        SalaryGrade grade = SalaryGrade.create(
                gradeId,
                input.organizationId(),
                input.name(),
                input.baseSalary()
        );

        salaryGradeRepository.save(grade);
        return new CreateSalaryGradeResponse(gradeId);
    }
}
