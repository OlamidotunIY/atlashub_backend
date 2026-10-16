package com.atlashub.pay.controls.infrastructure.persistence.adapters;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PolicyStatus;
import com.atlashub.pay.controls.infrastructure.persistence.entities.ControlledFundsPolicyJpa;
import com.atlashub.pay.controls.infrastructure.persistence.mappers.ControlledFundsPolicyMapper;
import com.atlashub.pay.controls.infrastructure.persistence.repositories.SpringDataControlledFundsPolicyRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControlledFundsPolicyRepositoryAdapterTest {

    @Mock
    private SpringDataControlledFundsPolicyRepository springDataRepo;

    @Mock
    private ControlledFundsPolicyMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private ControlledFundsPolicyRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ControlledFundsPolicyRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find active policy by facility ID")
    void shouldFindActivePolicyByFacilityId() {
        ControlledFundsPolicyJpa jpa = new ControlledFundsPolicyJpa(
                1L, 100L, 10L, Environment.LIVE, "v1.0", ControlMode.PURPOSE_RESTRICTED,
                null, null, null, null, null, null,
                PolicyStatus.ACTIVE, ZonedDateTime.now(), null, "ADMIN", 0L
        );
        ControlledFundsPolicy domain = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0", ControlMode.PURPOSE_RESTRICTED,
                null, null, null, null, null, null, null, null
        );

        when(springDataRepo.findByFacilityIdAndStatus(100L, PolicyStatus.ACTIVE)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<ControlledFundsPolicy> result = adapter.findActiveByFacilityId(100L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }
}
