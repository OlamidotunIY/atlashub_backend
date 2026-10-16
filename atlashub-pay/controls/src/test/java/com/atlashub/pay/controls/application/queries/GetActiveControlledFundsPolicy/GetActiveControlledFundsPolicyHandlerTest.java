package com.atlashub.pay.controls.application.queries.GetActiveControlledFundsPolicy;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActiveControlledFundsPolicyHandlerTest {

    @Mock
    private ControlledFundsPolicyRepository policyRepository;

    private GetActiveControlledFundsPolicyHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetActiveControlledFundsPolicyHandler(policyRepository);
    }

    @Test
    @DisplayName("Should query active policy for facility")
    void shouldQueryActivePolicy() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.PURPOSE_RESTRICTED,
                Set.of(new PermittedCategory("RAW_MATERIALS", "Raw Materials")),
                null, null, null, null, null, null, null
        );
        policy.activate("LENDER_ADMIN");

        when(policyRepository.findActiveByFacilityId(100L)).thenReturn(Optional.of(policy));

        ControlledFundsPolicyResult result = handler.execute(new GetActiveControlledFundsPolicyQuery(100L));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(100L, result.facilityId());
        assertEquals("ACTIVE", result.status());
        assertEquals("v1.0", result.policyVersion());
    }
}
