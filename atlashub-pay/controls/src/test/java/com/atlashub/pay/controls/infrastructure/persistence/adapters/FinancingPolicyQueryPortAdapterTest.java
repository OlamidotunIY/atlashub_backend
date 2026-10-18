package com.atlashub.pay.controls.infrastructure.persistence.adapters;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.shared.application.port.FinancingPolicyQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancingPolicyQueryPortAdapterTest {

    @Mock
    private ControlledFundsPolicyRepository policyRepository;

    private FinancingPolicyQueryPortAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FinancingPolicyQueryPortAdapter(policyRepository);
    }

    @Test
    @DisplayName("Should find active policy through shared query port")
    void shouldFindActivePolicyThroughPort() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0", ControlMode.PURPOSE_RESTRICTED,
                Set.of(new PermittedCategory("RAW_MATERIALS", "Raw Materials")),
                null, null, null, null, null, null, null
        );
        policy.activate("ADMIN");

        when(policyRepository.findActiveByFacilityId(100L)).thenReturn(Optional.of(policy));

        Optional<FinancingPolicyQueryPort.FinancingPolicyDto> result = adapter.findActivePolicyByFacilityId(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().facilityId());
        assertEquals("PURPOSE_RESTRICTED", result.get().mode());
        assertTrue(result.get().permittedCategories().contains("RAW_MATERIALS"));
    }
}
