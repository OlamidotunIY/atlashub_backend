package com.atlashub.hr.staff.infrastructure.persistence;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.hr.staff.infrastructure.persistence.adapters.EmployeeRepositoryAdapter;
import com.atlashub.hr.staff.infrastructure.persistence.entities.EmployeeJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.EmployeeMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataEmployeeRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeRepositoryAdapterTest {

    private SpringDataEmployeeRepository springDataRepo;
    private EmployeeMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private EmployeeRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataEmployeeRepository.class);
        mapper = mock(EmployeeMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new EmployeeRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    private Employee createSampleEmployee() {
        return Employee.create(
                1L,
                10L,
                100L,
                "EMP-001",
                "John",
                "Doe",
                new EmailAddress("john@atlashub.com"),
                new PhoneNumber("+2348012345678"),
                LocalDate.of(1990, 1, 1),
                "Lagos",
                "Nigerian",
                LocalDate.of(2023, 1, 1),
                "Dev",
                "Engineering",
                null,
                1L,
                Money.of(new BigDecimal("600000.00"), CurrencyCode.NGN)
        );
    }

    @Test
    @DisplayName("Should find employees by organization and status")
    void shouldFindByOrgAndStatus() {
        EmployeeJpaEntity jpa = mock(EmployeeJpaEntity.class);
        Employee domain = createSampleEmployee();

        when(springDataRepo.findByOrganizationIdAndStatus(10L, EmployeeStatus.PROBATION)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Employee> results = adapter.findByOrganizationIdAndStatus(10L, EmployeeStatus.PROBATION);

        assertEquals(1, results.size());
        assertEquals("EMP-001", results.getFirst().getEmployeeNumber());
    }

    @Test
    @DisplayName("Should find employee by employee number")
    void shouldFindByEmployeeNumber() {
        EmployeeJpaEntity jpa = mock(EmployeeJpaEntity.class);
        Employee domain = createSampleEmployee();

        when(springDataRepo.findByOrganizationIdAndEmployeeNumber(10L, "EMP-001")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Employee> result = adapter.findByOrganizationIdAndEmployeeNumber(10L, "EMP-001");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Should check if user already employed")
    void shouldCheckIfUserEmployed() {
        when(springDataRepo.existsByOrganizationIdAndUserId(10L, 100L)).thenReturn(true);

        boolean exists = adapter.existsByOrganizationIdAndUserId(10L, 100L);

        assertTrue(exists);
    }
}
