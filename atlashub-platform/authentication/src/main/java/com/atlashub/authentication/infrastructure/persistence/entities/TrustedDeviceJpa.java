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
        name = "trusted_device",
        indexes = {
                @Index(name = "Idx_device_user_fingerprint", columnList = "user_id, device_fingerprint", unique = true),
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TrustedDeviceJpa implements BaseJpaEntity {
    @Id
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_fingerprint", nullable = false)
    private String deviceFingerprint;

    @Column()
    private String deviceName;

    @Column()
    private String lastSeenIp;

    @Column()
    private ZonedDateTime trustedAt;

    @Column()
    private ZonedDateTime expiresAt;

    @Version
    private Long version;
}
