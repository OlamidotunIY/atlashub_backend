package com.atlashub.accounting.assets.application.command;

import com.atlashub.accounting.assets.application.command.RunDepreciation.RunDepreciationCommand;
import com.atlashub.accounting.assets.application.command.RunDepreciation.RunDepreciationHandler;
import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.accounting.assets.domain.services.DepreciationService;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RunDepreciationHandlerTest {

    private AssetRepository assetRepository;
    private DepreciationService depreciationService;
    private RunDepreciationHandler handler;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        depreciationService = mock(DepreciationService.class);
        handler = new RunDepreciationHandler(assetRepository, depreciationService);
    }

    @Test
    @DisplayName("Should run depreciation across active assets")
    void shouldRunDepreciation() {
        Asset asset1 = Asset.create(
                1L,
                10L,
                "Laptop 1",
                "AST-01",
                "IT",
                LocalDate.now(),
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                10,
                DepreciationMethod.STRAIGHT_LINE
        );
        Asset asset2 = Asset.create(
                2L,
                10L,
                "Laptop 2",
                "AST-02",
                "IT",
                LocalDate.now(),
                Money.of(new BigDecimal("200000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                10,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(assetRepository.findByOrganizationIdAndStatus(10L, AssetStatus.ACTIVE))
                .thenReturn(List.of(asset1, asset2));
        when(depreciationService.calculateMonthlyDepreciation(asset1))
                .thenReturn(Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN));
        when(depreciationService.calculateMonthlyDepreciation(asset2))
                .thenReturn(Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN));

        RunDepreciationCommand command = new RunDepreciationCommand(10L, LocalDate.now());
        int processed = handler.execute(command);

        assertEquals(2, processed);
        verify(assetRepository, times(2)).save(any(Asset.class));
    }
}
