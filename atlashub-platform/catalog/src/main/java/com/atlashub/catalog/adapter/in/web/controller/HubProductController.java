package com.atlashub.catalog.adapter.in.web.controller;

import com.atlashub.catalog.adapter.in.web.request.CreateHubProductWebRequest;
import com.atlashub.catalog.adapter.in.web.request.SetProductPricingWebRequest;
import com.atlashub.catalog.adapter.in.web.request.UpdateHubProductWebRequest;
import com.atlashub.catalog.application.command.CreateHubProductCommand;
import com.atlashub.catalog.application.command.SetProductPricingCommand;
import com.atlashub.catalog.application.command.UpdateHubProductCommand;
import com.atlashub.catalog.application.query.GetHubProductDetailsQuery;
import com.atlashub.catalog.application.query.ListHubProductsQuery;
import com.atlashub.catalog.application.result.CreateHubProductResult;
import com.atlashub.catalog.application.result.HubProductDetailsResult;
import com.atlashub.catalog.application.result.HubProductResult;
import com.atlashub.catalog.application.result.SetProductPricingResult;
import com.atlashub.catalog.application.result.UpdateHubProductResult;
import com.atlashub.catalog.application.usecase.CreateHubProductUseCase;
import com.atlashub.catalog.application.usecase.GetHubProductDetailsUseCase;
import com.atlashub.catalog.application.usecase.ListHubProductUseCase;
import com.atlashub.catalog.application.usecase.SetProductPricingUseCase;
import com.atlashub.catalog.application.usecase.UpdateHubProductUseCase;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.catalog.domain.valueobject.ProductStatus;
import com.atlashub.shared.dto.ApiResponse;
import com.atlashub.shared.money.CurrencyCode;
import com.atlashub.shared.money.Money;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform/catalog/products")
public class HubProductController {

    private final CreateHubProductUseCase createHubProductUseCase;
    private final UpdateHubProductUseCase updateHubProductUseCase;
    private final SetProductPricingUseCase setProductPricingUseCase;
    private final ListHubProductUseCase listHubProductUseCase;
    private final GetHubProductDetailsUseCase getHubProductDetailsUseCase;

    public HubProductController(
            CreateHubProductUseCase createHubProductUseCase,
            UpdateHubProductUseCase updateHubProductUseCase,
            SetProductPricingUseCase setProductPricingUseCase,
            ListHubProductUseCase listHubProductUseCase,
            GetHubProductDetailsUseCase getHubProductDetailsUseCase) {
        this.createHubProductUseCase = createHubProductUseCase;
        this.updateHubProductUseCase = updateHubProductUseCase;
        this.setProductPricingUseCase = setProductPricingUseCase;
        this.listHubProductUseCase = listHubProductUseCase;
        this.getHubProductDetailsUseCase = getHubProductDetailsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateHubProductResult> createProduct(
            @Valid @RequestBody CreateHubProductWebRequest request) {
        CreateHubProductCommand command = new CreateHubProductCommand(
                ProductKey.valueOf(request.key().toUpperCase()),
                request.name(),
                request.description()
        );
        CreateHubProductResult result = createHubProductUseCase.execute(command);
        return new ApiResponse<>(true, "Hub product created successfully", result, null);
    }

    @PutMapping("/{productId}")
    public ApiResponse<UpdateHubProductResult> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateHubProductWebRequest request) {
        UpdateHubProductCommand command = new UpdateHubProductCommand(
                productId,
                request.name(),
                request.description()
        );
        UpdateHubProductResult result = updateHubProductUseCase.execute(command);
        return new ApiResponse<>(true, "Hub product updated successfully", result, null);
    }

    @PutMapping("/{productId}/pricing")
    public ApiResponse<SetProductPricingResult> setPricing(
            @PathVariable Long productId,
            @Valid @RequestBody SetProductPricingWebRequest request) {
        SetProductPricingCommand command = new SetProductPricingCommand(
                productId,
                BillingCycle.valueOf(request.cycle().toUpperCase()),
                Money.of(request.amount(), CurrencyCode.valueOf(request.currency().toUpperCase()))
        );
        SetProductPricingResult result = setProductPricingUseCase.execute(command);
        return new ApiResponse<>(true, "Product pricing updated successfully", result, null);
    }

    @GetMapping
    public ApiResponse<List<HubProductResult>> listProducts(
            @RequestParam(required = false) String status) {
        ProductStatus productStatus = status != null ? ProductStatus.valueOf(status.toUpperCase()) : null;
        ListHubProductsQuery query = new ListHubProductsQuery(productStatus);
        List<HubProductResult> appResults = listHubProductUseCase.execute(query);
                
        return new ApiResponse<>(true, "Hub products retrieved successfully", appResults, null);
    }

    @GetMapping("/{productId}")
    public ApiResponse<HubProductDetailsResult> getProductDetails(
            @PathVariable Long productId) {
        GetHubProductDetailsQuery query = new GetHubProductDetailsQuery(productId);
        HubProductDetailsResult appResult = getHubProductDetailsUseCase.execute(query);
        return new ApiResponse<>(true, "Product details retrieved successfully", appResult, null);
    }
}
