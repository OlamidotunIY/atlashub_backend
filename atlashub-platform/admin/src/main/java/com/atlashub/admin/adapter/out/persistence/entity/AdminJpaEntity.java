package com.atlashub.admin.adapter.out.persistence.entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.atlashub.admin.domain.valueobject.AdminRole;
import com.atlashub.admin.domain.valueobject.AdminStatus;
import com.atlashub.admin.domain.valueobject.AdminPermission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminStatus status;

    @Column(name = "created_by")
    private Long createdBy;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<AdminPermission> permissions = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

