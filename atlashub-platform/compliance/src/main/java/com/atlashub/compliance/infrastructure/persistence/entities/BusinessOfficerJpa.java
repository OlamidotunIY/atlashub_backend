package com.atlashub.compliance.infrastructure.persistence.entities;

import com.atlashub.compliance.domain.valueobject.*;
import com.atlashub.compliance.infrastructure.persistence.adapters.ComplianceSensitiveDataConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "compliance_business_officers", indexes = {
        @Index(name = "idx_compliance_officer_record", columnList = "compliance_record_id"),
        @Index(name = "idx_compliance_officer_anchor", columnList = "anchor_officer_id", unique = true)})
@Data @Builder @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
public class BusinessOfficerJpa {
    @Id private Long id;
    @Column(name = "compliance_record_id", nullable = false, insertable = false, updatable = false) private Long complianceRecordId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private OfficerRole role;
    @Column(nullable = false) private String firstName;
    private String middleName;
    @Column(nullable = false) private String lastName;
    private String maidenName;
    @Column(nullable = false) private String nationality;
    @Column(nullable = false) private LocalDate dateOfBirth;
    @Column(nullable = false) private String email;
    @Column(nullable = false) private String phoneNumber;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "json") private AddressData residentialAddress;
    @Convert(converter = ComplianceSensitiveDataConverter.class) @Column(nullable = false, length = 1024) private String bvn;
    @Column(nullable = false) private String title;
    private BigDecimal percentageOwned;
    @Column(name = "anchor_officer_id", unique = true) private String anchorOfficerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private OfficerVerificationStatus verificationStatus;
}
