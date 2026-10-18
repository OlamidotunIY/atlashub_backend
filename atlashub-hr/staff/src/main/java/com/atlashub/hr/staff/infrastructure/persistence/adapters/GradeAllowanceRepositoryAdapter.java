package com.atlashub.hr.staff.infrastructure.persistence.adapters;

import com.atlashub.hr.staff.domain.entities.GradeAllowance;
import com.atlashub.hr.staff.domain.repositories.GradeAllowanceRepository;
import com.atlashub.hr.staff.infrastructure.persistence.entities.GradeAllowanceJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.GradeAllowanceMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataGradeAllowanceRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GradeAllowanceRepositoryAdapter
        extends JpaBaseRepository<GradeAllowance, GradeAllowanceJpaEntity>
        implements GradeAllowanceRepository {

    private final SpringDataGradeAllowanceRepository springDataRepo;

    public GradeAllowanceRepositoryAdapter(
            SpringDataGradeAllowanceRepository springDataRepo,
            GradeAllowanceMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_grade_allowance_seq";
    }

    @Override
    public List<GradeAllowance> findByGradeId(Long gradeId) {
        return springDataRepo.findByGradeId(gradeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
