package com.atlashub.audit.application.usecase;

import com.atlashub.audit.application.command.LogActivityCommand;
import com.atlashub.audit.domain.model.ActivityLog;
import com.atlashub.audit.domain.repository.ActivityLogRepository;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogActivityUseCase extends BaseUseCase<LogActivityCommand, Void> {

    private final ActivityLogRepository activityLogRepository;

    public LogActivityUseCase(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    @Transactional
    public Void execute(LogActivityCommand command) {
        ActivityLog log = new ActivityLog(
            command.organizationId(),
            command.actorUserId(),
            command.action(),
            command.entityType(),
            command.entityId(),
            command.metadata()
        );
        activityLogRepository.save(log);
        return null;
    }
}
