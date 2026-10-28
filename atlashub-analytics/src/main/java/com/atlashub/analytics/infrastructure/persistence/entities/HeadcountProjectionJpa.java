package com.atlashub.analytics.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "analytics_headcount",
        indexes = {
                @Index(name = "idx_headcount_unique", columnList = "organization_id, environment", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HeadcountProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "total", nullable = false)
    private int total;

    @Column(name = "active", nullable = false)
    private int active;

    @Column(name = "on_leave", nullable = false)
    private int onLeave;

    @Column(name = "suspended", nullable = false)
    private int suspended;

    @Column(name = "terminated_count", nullable = false)
    private int terminated;

    @Column(name = "new_hires_this_month", nullable = false)
    private int newHiresThisMonth;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
