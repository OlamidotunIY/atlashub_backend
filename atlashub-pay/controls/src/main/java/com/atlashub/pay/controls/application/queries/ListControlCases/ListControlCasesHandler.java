package com.atlashub.pay.controls.application.queries.ListControlCases;

import com.atlashub.pay.controls.domain.entities.ControlCase;
import com.atlashub.pay.controls.domain.repositories.ControlCaseRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListControlCasesHandler extends Query<ListControlCasesQuery, PageResult<ControlCaseResult>> {

    private final ControlCaseRepository caseRepository;

    public ListControlCasesHandler(ControlCaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ControlCaseResult> execute(ListControlCasesQuery input) {
        PageResult<ControlCase> page = caseRepository.findAllByOrganizationId(
                input.organizationId(),
                input.pageNumber(),
                input.pageSize()
        );

        List<ControlCaseResult> results = page.content().stream()
                .map(c -> new ControlCaseResult(
                        c.getId(),
                        c.getDecisionId(),
                        c.getOrganizationId(),
                        c.getStatus().name(),
                        c.getSubmittedEvidenceRefs(),
                        c.getOpenedAt(),
                        c.getResolvedBy(),
                        c.getResolvedAt(),
                        c.getResolutionReason()
                ))
                .collect(Collectors.toList());

        return new PageResult<>(
                results,
                page.pageNumber(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
