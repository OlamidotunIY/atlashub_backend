package com.atlashub.audit.adapter.out.persistence.repository;

import com.atlashub.audit.adapter.out.persistence.entity.ActivityLogJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringDataActivityLogRepository extends JpaRepository<ActivityLogJpaEntity, UUID> {
    List<ActivityLogJpaEntity> findByOrganizationIdOrderByOccurredAtDesc(Long organizationId, Pageable pageable);
}
