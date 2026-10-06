package com.atlashub.compliance.infrastructure.persistence.entities;

import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.compliance.domain.valueobject.ProviderOnboardingStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "compliance_provider_onboarding_cases", uniqueConstraints = {
        @UniqueConstraint(name = "uk_compliance_provider_case_org_env_provider",
                columnNames = {"organization_id", "api_environment", "provider"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProviderOnboardingCaseJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Enumerated(EnumType.STRING) @Column(name = "api_environment", nullable = false) private ApiEnvironment environment;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ComplianceProvider provider;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "requested_capabilities", columnDefinition = "json", nullable = false)
    private Set<String> requestedCapabilities;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ProviderOnboardingStatus status;
    @Column(name = "external_application_id") private String externalApplicationId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "outstanding_requirements", columnDefinition = "json")
    private Map<String, String> outstandingRequirements;
    @Column(name = "failure_code") private String failureCode;
    @Column(name = "failure_message") private String failureMessage;
    @Column(name = "submitted_at") private ZonedDateTime submittedAt;
    @Column(name = "approved_at") private ZonedDateTime approvedAt;
    @Column(name = "created_at", nullable = false) private ZonedDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private ZonedDateTime updatedAt;
    @Version private Long version;
}
