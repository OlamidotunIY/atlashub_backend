package com.atlashub.analytics.application.command;

import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionCommand;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionHandler;
import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateDailySalesProjectionHandlerTest {

    private DailySalesProjectionRepository repository;
    private UpdateDailySalesProjectionHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(DailySalesProjectionRepository.class);
        handler = new UpdateDailySalesProjectionHandler(repository);
    }

    @Test
    @DisplayName("Should create new DailySalesProjection if not found and save")
    void shouldCreateAndSaveWhenNotFound() {
        when(repository.findByOrgEnvOutletAndDate(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        UpdateDailySalesProjectionCommand command = new UpdateDailySalesProjectionCommand(
                100L,
                "live",
                200L,
                LocalDate.of(2026, 10, 17),
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(1050),
                "CARD",
                false
        );

        handler.execute(command);

        ArgumentCaptor<DailySalesProjection> captor = ArgumentCaptor.forClass(DailySalesProjection.class);
        verify(repository).save(captor.capture());
        DailySalesProjection saved = captor.getValue();

        assertNotNull(saved);
        assertEquals(100L, saved.getOrganizationId());
        assertEquals(1, saved.getTotalTransactions());
        assertEquals(BigDecimal.valueOf(1000), saved.getTotalGross());
        assertEquals(BigDecimal.valueOf(1050), saved.getCardSales());
    }

    @Test
    @DisplayName("Should update existing DailySalesProjection on sale event")
    void shouldUpdateExistingOnSale() {
        DailySalesProjection existing = DailySalesProjection.create(
                1L, 100L, "live", 200L, LocalDate.of(2026, 10, 17)
        );
        existing.applySale(BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(500), "CASH");

        when(repository.findByOrgEnvOutletAndDate(eq(100L), eq("live"), eq(200L), any()))
                .thenReturn(Optional.of(existing));

        UpdateDailySalesProjectionCommand command = new UpdateDailySalesProjectionCommand(
                100L,
                "live",
                200L,
                LocalDate.of(2026, 10, 17),
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                "CASH",
                false
        );

        handler.execute(command);

        verify(repository).save(existing);
        assertEquals(2, existing.getTotalTransactions());
        assertEquals(BigDecimal.valueOf(1500), existing.getTotalGross());
        assertEquals(BigDecimal.valueOf(1500), existing.getCashSales());
    }
}
