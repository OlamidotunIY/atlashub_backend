package com.atlashub.pay.settlement.infrastructure.persistence.entities;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "pay_settlement_poll_cursors", uniqueConstraints = @UniqueConstraint(name = "uk_settlement_cursor_route", columnNames = {"api_environment", "provider", "subaccount_code"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SettlementPollCursorJpa implements BaseJpaEntity {
    @Id
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "api_environment", nullable = false)
    private ApiEnvironment environment;
    @Column(nullable = false)
    private String provider;
    @Column(name = "subaccount_code", nullable = false)
    private String subaccountCode;
    @Column(name = "last_provider_settlement_id")
    private String lastProviderSettlementId;
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    @Version
    private Long version;
}
