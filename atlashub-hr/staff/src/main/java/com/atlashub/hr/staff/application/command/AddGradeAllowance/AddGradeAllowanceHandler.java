package com.atlashub.hr.staff.application.command.AddGradeAllowance;

import com.atlashub.hr.staff.domain.entities.GradeAllowance;
import com.atlashub.hr.staff.domain.exceptions.SalaryGradeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.GradeAllowanceRepository;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class AddGradeAllowanceHandler extends Command<AddGradeAllowanceCommand, Void> {

    private final SalaryGradeRepository salaryGradeRepository;
    private final GradeAllowanceRepository gradeAllowanceRepository;

    public AddGradeAllowanceHandler(
            SalaryGradeRepository salaryGradeRepository,
            GradeAllowanceRepository gradeAllowanceRepository
    ) {
        this.salaryGradeRepository = salaryGradeRepository;
        this.gradeAllowanceRepository = gradeAllowanceRepository;
    }

    @Override
    public Void execute(AddGradeAllowanceCommand input) {
        if (salaryGradeRepository.findById(input.gradeId()).isEmpty()) {
            throw new SalaryGradeNotFoundException(input.gradeId());
        }

        Long allowanceId = gradeAllowanceRepository.nextIdentity();
        GradeAllowance allowance = GradeAllowance.create(
                allowanceId,
                input.gradeId(),
                input.name(),
                input.type(),
                input.value()
        );

        gradeAllowanceRepository.save(allowance);
        return null;
    }
}
