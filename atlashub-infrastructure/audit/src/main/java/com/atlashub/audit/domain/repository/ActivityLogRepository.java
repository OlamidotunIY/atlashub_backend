package com.atlashub.audit.domain.repository;

import com.atlashub.audit.domain.model.ActivityLog;
import java.util.List;

public interface ActivityLogRepository {
    ActivityLog save(ActivityLog activityLog);
    List<ActivityLog> findByOrganizationId(Long organizationId, int page, int size);
}
