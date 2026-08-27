package com.atlashub.identity.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "users",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_active_organization_id", columnList = "active_organization_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = {"email"})
    }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Setter
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Setter
    @Column(name = "phone")
    private String phone;

    @Column(name = "country", nullable = false)
    private String country;

    @Setter
    @Column(name = "active_organization_id")
    private Long activeOrganizationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Setter
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
