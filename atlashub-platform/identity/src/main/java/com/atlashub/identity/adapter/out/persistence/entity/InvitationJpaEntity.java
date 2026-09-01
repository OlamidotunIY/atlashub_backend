package com.atlashub.identity.adapter.out.persistence.entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvitationJpaEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "invited_email", nullable = false)
    private String invitedEmail;
    
    @Column(name = "invited_by_user_id")
    private Long invitedByUserId;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = false)
    private String token;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}



