package com.atlashub.accounts.infrastructure.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "organizations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrganizationJPA implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String businessType;

    @Column(nullable = false)
    private String businessSize;

    @Column
    private String industry;

    @Column
    private String description;

    @Column(nullable = false)
    private String baseCurrency;

    @Column
    private String logoUrl;

    @Column
    private String websiteUrl;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
