package com.atlashub.compliance.application.commands.ProvisionTestAnchorCustomer;

import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProvisionTestAnchorCustomerHandlerTest {
    @Test
    void creates_and_persists_a_separate_sandbox_customer() {
        ComplianceRecordRepository repository = mock(ComplianceRecordRepository.class);
        AnchorCompliancePort anchor = mock(AnchorCompliancePort.class);
        ComplianceRecord record = mock(ComplianceRecord.class);
        when(record.getOrganizationId()).thenReturn(10L);
        when(record.getOfficers()).thenReturn(List.of());
        when(repository.findByOrganizationId(10L)).thenReturn(Optional.of(record));
        when(anchor.createBusinessCustomer(any(), eq(ApiEnvironment.TEST)))
                .thenReturn(new AnchorCompliancePort.BusinessCustomerResult("sandbox-customer", Map.of()));

        new ProvisionTestAnchorCustomerHandler(repository, anchor)
                .execute(new ProvisionTestAnchorCustomerCommand(10L));

        verify(record).recordSandboxAnchorCustomerCreated("sandbox-customer");
        verify(repository).save(record);
    }

    @Test
    void is_idempotent_when_sandbox_customer_already_exists() {
        ComplianceRecordRepository repository = mock(ComplianceRecordRepository.class);
        AnchorCompliancePort anchor = mock(AnchorCompliancePort.class);
        ComplianceRecord record = mock(ComplianceRecord.class);
        when(record.getSandboxAnchorBusinessCustomerId()).thenReturn("sandbox-customer");
        when(repository.findByOrganizationId(10L)).thenReturn(Optional.of(record));

        new ProvisionTestAnchorCustomerHandler(repository, anchor)
                .execute(new ProvisionTestAnchorCustomerCommand(10L));

        verifyNoInteractions(anchor);
        verify(repository, never()).save(any());
    }
}
