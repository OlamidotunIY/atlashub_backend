package com.atlashub.authentication.infrastructure.persistence.entities;

import com.atlashub.authentication.domain.valueobject.OtpStatus;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "otp_verifications",
        indexes = {
                @Index(name = "Idx_otp_auth_account_id", columnList = "auth_account_id", unique = true),
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OtpVerificationJpa implements BaseJpaEntity {
    @Id
    private Long id;

    @Column(name = "auth_account_id")
    private Long authAccountId;

    @Column()
    private String codeHash;

    @Column()
    private OtpType type;

    @Column()
    private OtpStatus status;

    @Column()
    private ZonedDateTime expiresAt;

    @Column()
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
