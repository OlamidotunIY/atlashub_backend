package com.atlashub.hr.staff.infrastructure.persistence.adapters;

import com.atlashub.hr.staff.domain.entities.SalaryGrade;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.hr.staff.infrastructure.persistence.entities.SalaryGradeJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.SalaryGradeMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataSalaryGradeRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SalaryGradeRepositoryAdapter
        extends JpaBaseRepository<SalaryGrade, SalaryGradeJpaEntity>
        implements SalaryGradeRepository {

    private final SpringDataSalaryGradeRepository springDataRepo;

    public SalaryGradeRepositoryAdapter(
            SpringDataSalaryGradeRepository springDataRepo,
            SalaryGradeMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_salary_grade_seq";
    }

    @Override
    public List<SalaryGrade> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SalaryGrade> findByOrganizationIdAndName(Long organizationId, String name) {
        return springDataRepo.findByOrganizationIdAndName(organizationId, name)
                .map(mapper::toDomain);
    }
}
