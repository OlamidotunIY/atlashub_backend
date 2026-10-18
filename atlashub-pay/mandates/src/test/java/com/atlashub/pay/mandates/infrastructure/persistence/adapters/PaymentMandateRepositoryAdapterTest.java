package com.atlashub.pay.mandates.infrastructure.persistence.adapters;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.pay.mandates.infrastructure.persistence.entities.PaymentMandateJpa;
import com.atlashub.pay.mandates.infrastructure.persistence.mappers.PaymentMandateMapper;
import com.atlashub.pay.mandates.infrastructure.persistence.repositories.SpringDataPaymentMandateRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMandateRepositoryAdapterTest {

    @Mock
    private SpringDataPaymentMandateRepository springDataRepo;

    @Mock
    private PaymentMandateMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private PaymentMandateRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PaymentMandateRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find by id and organizationId")
    void shouldFindByIdAndOrg() {
        PaymentMandateJpa jpa = new PaymentMandateJpa(
                1L, 10L, "CUST-1", "user@example.com", new BigDecimal("5000.00"),
                "NGN", MandateFrequency.MONTHLY, "AUTH", MandateStatus.ACTIVE,
                LocalDate.now(), ZonedDateTime.now(), null, 1L
        );
        PaymentMandate domain = Mockito.mock(PaymentMandate.class);

        when(springDataRepo.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<PaymentMandate> result = adapter.findByIdAndOrganizationId(1L, 10L);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find active mandates due for charge")
    void shouldFindDueMandates() {
        LocalDate today = LocalDate.now();
        PaymentMandateJpa jpa = new PaymentMandateJpa(
                1L, 10L, "CUST-1", "user@example.com", new BigDecimal("5000.00"),
                "NGN", MandateFrequency.MONTHLY, "AUTH", MandateStatus.ACTIVE,
                today, ZonedDateTime.now(), null, 1L
        );
        PaymentMandate domain = Mockito.mock(PaymentMandate.class);

        when(springDataRepo.findActiveMandatesDueForCharge(today)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<PaymentMandate> result = adapter.findActiveMandatesDueForCharge(today);

        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }
}
