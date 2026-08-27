package com.atlashub.identity.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organization_members",
    indexes = {
        @Index(name = "idx_org_member_user_id", columnList = "user_id"),
        @Index(name = "idx_org_member_organization_id", columnList = "organization_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_org_member_user_org", columnNames = {"user_id", "organization_id"})
    }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String role;

    @Setter
    @Column(nullable = false)
    private String status;

    @Column(name = "joined_at", nullable = false)
    private ZonedDateTime joinedAt;
}
