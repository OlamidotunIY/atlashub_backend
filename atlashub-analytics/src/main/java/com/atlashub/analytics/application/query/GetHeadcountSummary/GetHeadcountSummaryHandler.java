package com.atlashub.analytics.application.query.GetHeadcountSummary;

import com.atlashub.analytics.domain.entities.HeadcountProjection;
import com.atlashub.analytics.domain.repositories.HeadcountProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetHeadcountSummaryHandler extends Query<GetHeadcountSummaryQuery, HeadcountResult> {

    private final HeadcountProjectionRepository repository;

    public GetHeadcountSummaryHandler(HeadcountProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public HeadcountResult execute(GetHeadcountSummaryQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        HeadcountProjection projection = repository.findByOrgEnv(
                query.organizationId(),
                env
        ).orElseGet(() -> HeadcountProjection.create(
                null,
                query.organizationId(),
                env
        ));

        return new HeadcountResult(
                projection.getTotal(),
                projection.getActive(),
                projection.getOnLeave(),
                projection.getSuspended(),
                projection.getTerminated(),
                projection.getNewHiresThisMonth()
        );
    }
}
