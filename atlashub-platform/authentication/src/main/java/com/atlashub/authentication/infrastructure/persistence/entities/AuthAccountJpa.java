package com.atlashub.authentication.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "auth_accounts",
        indexes = {
                @Index(name = "Idx_auth_user_id", columnList = "user_id", unique = true),
                @Index(name = "Idx_auth_email", columnList = "email", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuthAccountJpa implements BaseJpaEntity {

        @Id
        private Long id;

        @Column(name = "user_id")
        private Long userId;

        @Column()
        private String email;

        @Column()
        private String passwordHash;

        @Column()
        private Boolean emailVerified;

        @Column()
        private int failedLoginAttempts;

        @Column(nullable = false)
        private ZonedDateTime lockedUntil;

        @Column(nullable = false)
        private ZonedDateTime lastLoginAt;

        @Column(nullable = false)
        private String lastLoginIp;

        @Column()
        private ZonedDateTime createdAt;

        @Version
        private Long version;
}
