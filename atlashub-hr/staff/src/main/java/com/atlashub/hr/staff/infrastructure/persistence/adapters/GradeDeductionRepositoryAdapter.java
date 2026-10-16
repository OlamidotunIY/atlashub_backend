package com.atlashub.hr.staff.infrastructure.persistence.adapters;

import com.atlashub.hr.staff.domain.entities.GradeDeduction;
import com.atlashub.hr.staff.domain.repositories.GradeDeductionRepository;
import com.atlashub.hr.staff.infrastructure.persistence.entities.GradeDeductionJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.GradeDeductionMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataGradeDeductionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GradeDeductionRepositoryAdapter
        extends JpaBaseRepository<GradeDeduction, GradeDeductionJpaEntity>
        implements GradeDeductionRepository {

    private final SpringDataGradeDeductionRepository springDataRepo;

    public GradeDeductionRepositoryAdapter(
            SpringDataGradeDeductionRepository springDataRepo,
            GradeDeductionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_grade_deduction_seq";
    }

    @Override
    public List<GradeDeduction> findByGradeId(Long gradeId) {
        return springDataRepo.findByGradeId(gradeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
