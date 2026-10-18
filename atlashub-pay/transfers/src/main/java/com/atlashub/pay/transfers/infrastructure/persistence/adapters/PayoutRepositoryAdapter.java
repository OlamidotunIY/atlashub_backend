package com.atlashub.pay.transfers.infrastructure.persistence.adapters;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.pay.transfers.infrastructure.persistence.entities.PayoutJpa;
import com.atlashub.pay.transfers.infrastructure.persistence.mappers.PayoutMapper;
import com.atlashub.pay.transfers.infrastructure.persistence.repositories.SpringDataPayoutRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PayoutRepositoryAdapter extends JpaBaseRepository<Payout, PayoutJpa> implements PayoutRepository {

    private final SpringDataPayoutRepository springDataRepo;

    public PayoutRepositoryAdapter(SpringDataPayoutRepository springDataRepo,
                                 PayoutMapper mapper,
                                 DomainSequenceGenerator sequenceGenerator,
                                 DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "payout_id_seq";
    }

    @Override
    public Optional<Payout> findByReference(String reference) {
        return springDataRepo.findByReference(reference)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Payout> findByIdWithPessimisticLock(Long id) {
        return springDataRepo.findByIdWithPessimisticLock(id)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<Payout> findByOrganizationIdAndStatus(Long organizationId, PayoutStatus status, int page, int size) {
        Page<PayoutJpa> jpaPage = springDataRepo.findByOrganizationIdAndStatus(
                organizationId, status, PageRequest.of(Math.max(0, page - 1), size)
        );

        List<Payout> items = jpaPage.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageResult<>(items, page, size, jpaPage.getTotalElements(), jpaPage.getTotalPages());
    }

    @Override
    public PageResult<Payout> findByOrganizationId(Long organizationId, int page, int size) {
        Page<PayoutJpa> jpaPage = springDataRepo.findByOrganizationId(
                organizationId, PageRequest.of(Math.max(0, page - 1), size)
        );

        List<Payout> items = jpaPage.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageResult<>(items, page, size, jpaPage.getTotalElements(), jpaPage.getTotalPages());
    }

    @Override
    public List<Payout> findBySourceSystemAndSourceReferenceId(SourceSystem sourceSystem, String sourceReferenceId) {
        return springDataRepo.findBySourceSystemAndSourceReferenceId(sourceSystem, sourceReferenceId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
