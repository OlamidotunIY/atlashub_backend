package com.atlashub.hr.staff.application.command.AddGradeDeduction;

import com.atlashub.hr.staff.domain.entities.GradeDeduction;
import com.atlashub.hr.staff.domain.exceptions.SalaryGradeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.GradeDeductionRepository;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class AddGradeDeductionHandler extends Command<AddGradeDeductionCommand, Void> {

    private final SalaryGradeRepository salaryGradeRepository;
    private final GradeDeductionRepository gradeDeductionRepository;

    public AddGradeDeductionHandler(
            SalaryGradeRepository salaryGradeRepository,
            GradeDeductionRepository gradeDeductionRepository
    ) {
        this.salaryGradeRepository = salaryGradeRepository;
        this.gradeDeductionRepository = gradeDeductionRepository;
    }

    @Override
    public Void execute(AddGradeDeductionCommand input) {
        if (salaryGradeRepository.findById(input.gradeId()).isEmpty()) {
            throw new SalaryGradeNotFoundException(input.gradeId());
        }

        Long deductionId = gradeDeductionRepository.nextIdentity();
        GradeDeduction deduction = GradeDeduction.create(
                deductionId,
                input.gradeId(),
                input.name(),
                input.type(),
                input.value(),
                input.isStatutory()
        );

        gradeDeductionRepository.save(deduction);
        return null;
    }
}
