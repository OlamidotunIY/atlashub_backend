package com.atlashub.accounting.gl.domain.services;

import com.atlashub.accounting.gl.domain.entities.LedgerAccountMapping;
import com.atlashub.accounting.gl.domain.exceptions.LedgerMappingNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.LedgerAccountMappingRepository;
import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerMappingServiceTest {

    @Mock
    private LedgerAccountMappingRepository repository;

    private LedgerMappingService service;

    @BeforeEach
    void setUp() {
        service = new LedgerMappingService(repository);
    }

    @Test
    @DisplayName("Should resolve mapping when found in repository")
    void shouldResolveMappingWhenFound() {
        LedgerAccountMapping mapping = LedgerAccountMapping.create(
                1L,
                10L,
                SourceSystem.COMMERCE_CHECKOUT,
                101L,
                401L,
                "Bridge mapping"
        );

        when(repository.findByOrganizationIdAndSourceSystem(10L, SourceSystem.COMMERCE_CHECKOUT))
                .thenReturn(Optional.of(mapping));

        LedgerAccountMapping resolved = service.resolveMapping(10L, SourceSystem.COMMERCE_CHECKOUT);

        assertNotNull(resolved);
        assertEquals(101L, resolved.getDebitAccountId());
        assertEquals(401L, resolved.getCreditAccountId());
    }

    @Test
    @DisplayName("Should throw LedgerMappingNotFoundException when mapping does not exist")
    void shouldThrowWhenMappingNotFound() {
        when(repository.findByOrganizationIdAndSourceSystem(10L, SourceSystem.PAYROLL))
                .thenReturn(Optional.empty());

        assertThrows(LedgerMappingNotFoundException.class, () ->
                service.resolveMapping(10L, SourceSystem.PAYROLL));
    }
}
