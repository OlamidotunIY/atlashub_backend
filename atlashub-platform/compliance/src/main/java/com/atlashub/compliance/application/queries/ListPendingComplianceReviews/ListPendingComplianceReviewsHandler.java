package com.atlashub.compliance.application.queries.ListPendingComplianceReviews;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListPendingComplianceReviewsHandler extends Query<ListPendingComplianceReviewsQuery, PageResult<ComplianceReviewResult>> {

    private static final Logger log = LoggerFactory.getLogger(ListPendingComplianceReviewsHandler.class);

    private final ComplianceRecordRepository repository;

    public ListPendingComplianceReviewsHandler(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<ComplianceReviewResult> execute(ListPendingComplianceReviewsQuery query) {
        log.info("Executing ListPendingComplianceReviewsQuery for status: {}", query.status());

        PageResult<ComplianceRecord> pageResult = repository.findAllByStatus(query.status(), query.page(), query.size());

        List<ComplianceReviewResult> dtos = pageResult.content().stream()
            .map(record -> new ComplianceReviewResult(
                record.getOrganizationId(),
                record.getStatus(),
                record.getSubmittedAt()
            ))
            .collect(Collectors.toList());

        return new PageResult<>(
            dtos,
            pageResult.pageNumber(),
            pageResult.pageSize(),
            pageResult.totalElements(),
            pageResult.totalPages()
        );
    }
}
