package com.atlashub.pay.transfers.infrastructure.persistence.adapters;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.pay.transfers.infrastructure.persistence.entities.PayoutJpa;
import com.atlashub.pay.transfers.infrastructure.persistence.mappers.PayoutMapper;
import com.atlashub.pay.transfers.infrastructure.persistence.repositories.SpringDataPayoutRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayoutRepositoryAdapterTest {

    @Mock
    private SpringDataPayoutRepository springDataRepo;

    @Mock
    private PayoutMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private PayoutRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PayoutRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find payout by reference")
    void shouldFindByReference() {
        PayoutJpa jpa = new PayoutJpa(
                1L, 10L, new BigDecimal("50000.00"), "NGN", "044", "0123456789",
                "John", "Test", PayoutStatus.APPROVED, "REF-001", PaymentProvider.PAYSTACK,
                null, SourceSystem.MANUAL, "SRC", 100L, 100L, ZonedDateTime.now(), null,
                ZonedDateTime.now(), null, 1L
        );
        Payout domain = org.mockito.Mockito.mock(Payout.class);

        when(springDataRepo.findByReference("REF-001")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Payout> result = adapter.findByReference("REF-001");

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find by org and status with pagination")
    void shouldFindByOrgAndStatus() {
        PayoutJpa jpa = new PayoutJpa(
                2L, 10L, new BigDecimal("50000.00"), "NGN", "044", "0123456789",
                "John", "Test", PayoutStatus.APPROVED, "REF-002", PaymentProvider.PAYSTACK,
                null, SourceSystem.MANUAL, "SRC", 100L, 100L, ZonedDateTime.now(), null,
                ZonedDateTime.now(), null, 1L
        );
        Payout domain = org.mockito.Mockito.mock(Payout.class);

        when(springDataRepo.findByOrganizationIdAndStatus(eq(10L), eq(PayoutStatus.APPROVED), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(jpa), PageRequest.of(0, 10), 1L));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        PageResult<Payout> result = adapter.findByOrganizationIdAndStatus(10L, PayoutStatus.APPROVED, 1, 10);

        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
    }
}
