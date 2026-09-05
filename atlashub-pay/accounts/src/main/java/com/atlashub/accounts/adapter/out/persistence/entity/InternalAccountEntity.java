package com.atlashub.accounts.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "internal_accounts", 
    indexes = {
        @Index(name = "idx_ia_organization", columnList = "organization_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ia_org_type", columnNames = {"organization_id", "type"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternalAccountEntity {

    @Id
    private Long id;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(nullable = false, length = 30)
    private String type;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Setter
    @Column(nullable = false, length = 30)
    private String status;
    
    @Setter
    @Version
    @Column(nullable = false)
    private Integer version;
    
    @Column(nullable = false)
    private ZonedDateTime createdAt;
    
    @Setter
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    public InternalAccountEntity(Long id, Long organizationId, String type, String currency, String status, Integer version, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.type = type;
        this.currency = currency;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
