package com.atlashub.authentication.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = 'auth_accounts',
        indexes = {

        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuthAccountJpa {

        @Id
        private Long id;

        @Column()
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
