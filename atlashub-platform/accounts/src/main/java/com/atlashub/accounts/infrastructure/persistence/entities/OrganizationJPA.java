package com.atlashub.accounts.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrganizationJPA implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String registrationType;

    @Column(nullable = false)
    private String industry;

    @Column(nullable = false)
    private LocalDate registrationDate;

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
