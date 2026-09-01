package com.atlashub.audit.adapter.out.persistence;

import com.atlashub.audit.adapter.out.persistence.mapper.ActivityLogMapper;
import com.atlashub.audit.adapter.out.persistence.repository.SpringDataActivityLogRepository;
import com.atlashub.audit.domain.model.ActivityLog;
import com.atlashub.audit.domain.repository.ActivityLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActivityLogPersistenceAdapter implements ActivityLogRepository {

    private final SpringDataActivityLogRepository repository;
    private final ActivityLogMapper mapper;

    public ActivityLogPersistenceAdapter(SpringDataActivityLogRepository repository, ActivityLogMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ActivityLog save(ActivityLog activityLog) {
        return mapper.toDomain(repository.save(mapper.toEntity(activityLog)));
    }

    @Override
    public List<ActivityLog> findByOrganizationId(Long organizationId, int page, int size) {
        return repository.findByOrganizationIdOrderByOccurredAtDesc(organizationId, PageRequest.of(page - 1, size))
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
