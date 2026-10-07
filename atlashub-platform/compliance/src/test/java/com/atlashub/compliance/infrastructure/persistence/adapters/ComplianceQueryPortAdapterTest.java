package com.atlashub.compliance.infrastructure.persistence.adapters;

import com.atlashub.compliance.infrastructure.persistence.mappers.ComplianceRecordMapper;
import com.atlashub.compliance.infrastructure.persistence.repositories.SpringDataComplianceRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComplianceQueryPortAdapterTest {

    @Test
    void reports_not_approved_when_the_organization_has_no_compliance_record() {
        SpringDataComplianceRecordRepository records = mock(SpringDataComplianceRecordRepository.class);
        when(records.findByOrganizationId(7L)).thenReturn(Optional.empty());

        ComplianceQueryPortAdapter adapter = new ComplianceQueryPortAdapter(records, mock(ComplianceRecordMapper.class));

        assertFalse(adapter.isApproved(7L));
    }
}
