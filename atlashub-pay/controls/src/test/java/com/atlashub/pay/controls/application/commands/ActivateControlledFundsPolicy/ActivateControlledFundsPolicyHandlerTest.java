package com.atlashub.pay.controls.application.commands.ActivateControlledFundsPolicy;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PolicyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateControlledFundsPolicyHandlerTest {

    @Mock
    private ControlledFundsPolicyRepository policyRepository;

    private ActivateControlledFundsPolicyHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ActivateControlledFundsPolicyHandler(policyRepository);
    }

    @Test
    @DisplayName("Should activate and save controlled funds policy")
    void shouldActivatePolicy() {
        when(policyRepository.nextIdentity()).thenReturn(1L);

        ActivateControlledFundsPolicyCommand command = new ActivateControlledFundsPolicyCommand(
                100L,
                10L,
                Environment.LIVE,
                "v1.0",
                ControlMode.PURPOSE_RESTRICTED,
                Collections.emptySet(),
                Collections.emptySet(),
                null,
                null,
                null,
                Collections.emptySet(),
                ZonedDateTime.now(),
                null,
                "LENDER_USER"
        );

        handler.execute(command);

        ArgumentCaptor<ControlledFundsPolicy> captor = ArgumentCaptor.forClass(ControlledFundsPolicy.class);
        verify(policyRepository).save(captor.capture());

        ControlledFundsPolicy saved = captor.getValue();
        assertEquals(1L, saved.getId());
        assertEquals(PolicyStatus.ACTIVE, saved.getStatus());
        assertEquals("LENDER_USER", saved.getActivatedBy());
    }
}
