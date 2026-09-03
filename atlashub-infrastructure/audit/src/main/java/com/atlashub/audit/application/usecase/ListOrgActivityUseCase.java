package com.atlashub.audit.application.usecase;

import com.atlashub.audit.application.query.ListOrgActivityQuery;
import com.atlashub.audit.application.result.ActivityLogDto;
import com.atlashub.audit.domain.model.ActivityLog;
import com.atlashub.audit.domain.repository.ActivityLogRepository;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListOrgActivityUseCase extends BaseUseCase<ListOrgActivityQuery, List<ActivityLogDto>> {

    private final ActivityLogRepository activityLogRepository;

    public ListOrgActivityUseCase(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    public List<ActivityLogDto> execute(ListOrgActivityQuery query) {
        List<ActivityLog> logs = activityLogRepository.findByOrganizationId(query.organizationId(), query.page(), query.size());
        
        return logs.stream().map(log -> new ActivityLogDto(
            log.getId(),
            log.getOrganizationId(),
            log.getActorUserId(),
            log.getAction(),
            log.getEntityType(),
            log.getEntityId(),
            log.getMetadata(),
            log.getOccurredAt()
        )).collect(Collectors.toList());
    }
}
