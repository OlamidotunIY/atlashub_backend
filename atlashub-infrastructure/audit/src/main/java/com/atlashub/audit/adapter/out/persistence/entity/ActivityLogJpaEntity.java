package com.atlashub.audit.adapter.out.persistence.entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_activity_org_id", columnList = "organization_id"),
    @Index(name = "idx_activity_actor_id", columnList = "actor_user_id"),
    @Index(name = "idx_activity_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityLogJpaEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "organization_id")
    private Long organizationId;
    
    @Column(name = "actor_user_id")
    private Long actorUserId;
    
    @Column(nullable = false)
    private String action;
    
    @Column(name = "entity_type")
    private String entityType;
    
    @Column(name = "entity_id")
    private String entityId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> metadata;
    
    @Column(name = "occurred_at", nullable = false)
    private ZonedDateTime occurredAt;
}



