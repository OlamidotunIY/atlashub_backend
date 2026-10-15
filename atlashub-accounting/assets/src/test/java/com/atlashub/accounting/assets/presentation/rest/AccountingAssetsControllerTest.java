package com.atlashub.accounting.assets.presentation.rest;

import com.atlashub.accounting.assets.application.command.DisposeAsset.DisposeAssetCommand;
import com.atlashub.accounting.assets.application.command.DisposeAsset.DisposeAssetHandler;
import com.atlashub.accounting.assets.application.command.RegisterAsset.RegisterAssetCommand;
import com.atlashub.accounting.assets.application.command.RegisterAsset.RegisterAssetHandler;
import com.atlashub.accounting.assets.application.command.RunDepreciation.RunDepreciationCommand;
import com.atlashub.accounting.assets.application.command.RunDepreciation.RunDepreciationHandler;
import com.atlashub.accounting.assets.application.query.GetAssetDetails.GetAssetDetailsHandler;
import com.atlashub.accounting.assets.application.query.GetAssetDetails.GetAssetDetailsQuery;
import com.atlashub.accounting.assets.application.query.ListAssets.AssetResult;
import com.atlashub.accounting.assets.application.query.ListAssets.ListAssetsHandler;
import com.atlashub.accounting.assets.application.query.ListAssets.ListAssetsQuery;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.accounting.assets.presentation.dto.AssetResponse;
import com.atlashub.accounting.assets.presentation.dto.DisposeAssetRequest;
import com.atlashub.accounting.assets.presentation.dto.RegisterAssetRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountingAssetsControllerTest {

    private RegisterAssetHandler registerAssetHandler;
    private RunDepreciationHandler runDepreciationHandler;
    private DisposeAssetHandler disposeAssetHandler;
    private ListAssetsHandler listAssetsHandler;
    private GetAssetDetailsHandler getAssetDetailsHandler;
    private AccountingAssetsController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        registerAssetHandler = mock(RegisterAssetHandler.class);
        runDepreciationHandler = mock(RunDepreciationHandler.class);
        disposeAssetHandler = mock(DisposeAssetHandler.class);
        listAssetsHandler = mock(ListAssetsHandler.class);
        getAssetDetailsHandler = mock(GetAssetDetailsHandler.class);

        controller = new AccountingAssetsController(
                registerAssetHandler,
                runDepreciationHandler,
                disposeAssetHandler,
                listAssetsHandler,
                getAssetDetailsHandler
        );

        principal = mock(AuthenticatedPrincipal.class);
        when(principal.activeOrganizationId()).thenReturn(10L);
    }

    @Test
    @DisplayName("Should register new asset and return 201 Created")
    void shouldRegisterAsset() {
        RegisterAssetRequest request = new RegisterAssetRequest(
                "MacBook Air M3",
                "AST-MBA-01",
                "Laptops",
                LocalDate.now(),
                new BigDecimal("1500000.00"),
                new BigDecimal("300000.00"),
                "NGN",
                36,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(registerAssetHandler.execute(any(RegisterAssetCommand.class))).thenReturn(101L);

        ResponseEntity<ApiResponse<Long>> response = controller.registerAsset(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().data());

        ArgumentCaptor<RegisterAssetCommand> captor = ArgumentCaptor.forClass(RegisterAssetCommand.class);
        verify(registerAssetHandler).execute(captor.capture());
        RegisterAssetCommand cmd = captor.getValue();
        assertEquals(10L, cmd.organizationId());
        assertEquals("AST-MBA-01", cmd.tagNumber());
    }

    @Test
    @DisplayName("Should list assets for organization")
    void shouldListAssets() {
        AssetResult result = new AssetResult(
                101L,
                10L,
                "MacBook Air M3",
                "AST-MBA-01",
                "Laptops",
                LocalDate.now(),
                new BigDecimal("1500000.00"),
                new BigDecimal("300000.00"),
                36,
                DepreciationMethod.STRAIGHT_LINE,
                BigDecimal.ZERO,
                new BigDecimal("1500000.00"),
                AssetStatus.ACTIVE,
                "NGN",
                null,
                null
        );

        when(listAssetsHandler.execute(any(ListAssetsQuery.class))).thenReturn(List.of(result));

        ResponseEntity<ApiResponse<?>> response = controller.getAssets(null, AssetStatus.ACTIVE, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<AssetResponse> data = (List<AssetResponse>) response.getBody().data();
        assertEquals(1, data.size());
        assertEquals("AST-MBA-01", data.getFirst().tagNumber());
    }

    @Test
    @DisplayName("Should get single asset by ID")
    void shouldGetAssetById() {
        AssetResult result = new AssetResult(
                101L,
                10L,
                "MacBook Air M3",
                "AST-MBA-01",
                "Laptops",
                LocalDate.now(),
                new BigDecimal("1500000.00"),
                new BigDecimal("300000.00"),
                36,
                DepreciationMethod.STRAIGHT_LINE,
                BigDecimal.ZERO,
                new BigDecimal("1500000.00"),
                AssetStatus.ACTIVE,
                "NGN",
                null,
                null
        );

        when(getAssetDetailsHandler.execute(any(GetAssetDetailsQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<?>> response = controller.getAssets(101L, null, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        AssetResponse data = (AssetResponse) response.getBody().data();
        assertEquals(101L, data.id());
        assertThat(data.purchaseValue()).isEqualByComparingTo(new BigDecimal("1500000.00"));
    }

    @Test
    @DisplayName("Should dispose asset")
    void shouldDisposeAsset() {
        AssetResult result = new AssetResult(
                101L,
                10L,
                "MacBook Air M3",
                "AST-MBA-01",
                "Laptops",
                LocalDate.now(),
                new BigDecimal("1500000.00"),
                new BigDecimal("300000.00"),
                36,
                DepreciationMethod.STRAIGHT_LINE,
                BigDecimal.ZERO,
                new BigDecimal("1500000.00"),
                AssetStatus.ACTIVE,
                "NGN",
                null,
                null
        );

        when(getAssetDetailsHandler.execute(any(GetAssetDetailsQuery.class))).thenReturn(result);

        DisposeAssetRequest request = new DisposeAssetRequest(
                new BigDecimal("800000.00"),
                LocalDate.now()
        );

        ResponseEntity<ApiResponse<Void>> response = controller.disposeAsset(101L, request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(disposeAssetHandler).execute(any(DisposeAssetCommand.class));
    }

    @Test
    @DisplayName("Should trigger depreciation run")
    void shouldTriggerDepreciationRun() {
        when(runDepreciationHandler.execute(any(RunDepreciationCommand.class))).thenReturn(5);

        ResponseEntity<ApiResponse<Integer>> response = controller.runDepreciation(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().data());
    }
}
