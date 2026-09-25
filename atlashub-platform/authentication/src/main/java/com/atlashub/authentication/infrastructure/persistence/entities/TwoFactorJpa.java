package com.atlashub.authentication.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "two_factors",
        indexes = {
                @Index(name = "Idx_two_factor_user_id", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TwoFactorJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String secret;

    @Column(nullable = false)
    private String backupCodes;

    @Column(name = "user_id", nullable = false)
    private String userId;
}
