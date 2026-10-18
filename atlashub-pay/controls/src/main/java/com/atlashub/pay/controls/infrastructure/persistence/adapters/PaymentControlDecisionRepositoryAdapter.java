package com.atlashub.pay.controls.infrastructure.persistence.adapters;

import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.pay.controls.infrastructure.persistence.entities.PaymentControlDecisionJpa;
import com.atlashub.pay.controls.infrastructure.persistence.mappers.PaymentControlDecisionMapper;
import com.atlashub.pay.controls.infrastructure.persistence.repositories.SpringDataPaymentControlDecisionRepository;
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
public class PaymentControlDecisionRepositoryAdapter
        extends JpaBaseRepository<PaymentControlDecision, PaymentControlDecisionJpa>
        implements PaymentControlDecisionRepository {

    private final SpringDataPaymentControlDecisionRepository springDataRepo;

    public PaymentControlDecisionRepositoryAdapter(
            SpringDataPaymentControlDecisionRepository springDataRepo,
            PaymentControlDecisionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "pay_control_decision_seq";
    }

    @Override
    public Optional<PaymentControlDecision> findByPaymentInstructionReferenceAndPolicyId(String instructionRef, Long policyId) {
        return springDataRepo.findByPaymentInstructionReferenceAndPolicyId(instructionRef, policyId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<PaymentControlDecision> findByPaymentInstructionReference(String instructionRef) {
        return springDataRepo.findByPaymentInstructionReference(instructionRef)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<PaymentControlDecision> findAllByOrganizationId(Long organizationId, int pageNumber, int pageSize) {
        Page<PaymentControlDecisionJpa> page = springDataRepo.findAllByOrganizationId(
                organizationId,
                PageRequest.of(pageNumber, pageSize)
        );

        List<PaymentControlDecision> content = page.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageResult<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
