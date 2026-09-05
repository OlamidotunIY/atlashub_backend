package com.atlashub.catalog.adapter.in.web.controller;

import com.atlashub.catalog.adapter.in.web.request.CreateHubProductWebRequest;
import com.atlashub.catalog.adapter.in.web.request.SetProductPricingWebRequest;
import com.atlashub.catalog.adapter.in.web.request.UpdateHubProductWebRequest;
import com.atlashub.catalog.adapter.in.web.response.CreateHubProductResponse;
import com.atlashub.catalog.adapter.in.web.response.HubProductDetailsResponse;
import com.atlashub.catalog.adapter.in.web.response.HubProductResponse;
import com.atlashub.catalog.adapter.in.web.response.SetProductPricingResponse;
import com.atlashub.catalog.adapter.in.web.response.UpdateHubProductResponse;
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
import com.atlashub.catalog.application.port.HubProductQueryService;
import com.atlashub.catalog.application.usecase.ListHubProductUseCase;
import com.atlashub.catalog.application.usecase.SetProductPricingUseCase;
import com.atlashub.catalog.application.usecase.UpdateHubProductUseCase;
import com.atlashub.catalog.domain.valueobject.ProductStatus;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.domain.money.Money;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform/catalog/products")
public class HubProductController {

    private final CreateHubProductUseCase createHubProductUseCase;
    private final UpdateHubProductUseCase updateHubProductUseCase;
    private final SetProductPricingUseCase setProductPricingUseCase;
    private final ListHubProductUseCase listHubProductUseCase;
    private final HubProductQueryService queryService;

    public HubProductController(
            CreateHubProductUseCase createHubProductUseCase,
            UpdateHubProductUseCase updateHubProductUseCase,
            SetProductPricingUseCase setProductPricingUseCase,
            ListHubProductUseCase listHubProductUseCase,
            HubProductQueryService queryService) {
        this.createHubProductUseCase = createHubProductUseCase;
        this.updateHubProductUseCase = updateHubProductUseCase;
        this.setProductPricingUseCase = setProductPricingUseCase;
        this.listHubProductUseCase = listHubProductUseCase;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateHubProductResponse>> createProduct(
            @Valid @RequestBody CreateHubProductWebRequest request) {
        CreateHubProductCommand command = new CreateHubProductCommand(
                request.key(),
                request.name(),
                request.description()
        );
        CreateHubProductResult result = createHubProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Hub product created successfully", new CreateHubProductResponse(result.product().getId()), null));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<UpdateHubProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateHubProductWebRequest request) {
        UpdateHubProductCommand command = new UpdateHubProductCommand(
                productId,
                request.name(),
                request.description()
        );
        UpdateHubProductResult result = updateHubProductUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Hub product updated successfully", new UpdateHubProductResponse(result.id()), null));
    }

    @PutMapping("/{productId}/pricing")
    public ResponseEntity<ApiResponse<SetProductPricingResponse>> setPricing(
            @PathVariable Long productId,
            @Valid @RequestBody SetProductPricingWebRequest request) {
        SetProductPricingCommand command = new SetProductPricingCommand(
                productId,
                request.cycle(),
                new Money(request.amount(), request.currency())
        );
        SetProductPricingResult result = setProductPricingUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product pricing updated successfully", new SetProductPricingResponse(result.pricingId()), null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HubProductResponse>>> listProducts(
            @RequestParam(required = false) ProductStatus status) {
        ListHubProductsQuery query = new ListHubProductsQuery(status);
        List<HubProductResult> appResults = listHubProductUseCase.execute(query);
                
        List<HubProductResponse> webResponse = appResults.stream()
                .map(r -> new HubProductResponse(r.id(), r.key(), r.name(), r.description(), r.status()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Hub products retrieved successfully", webResponse, null));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<HubProductDetailsResponse>> getProductDetails(
            @PathVariable Long productId) {
        GetHubProductDetailsQuery query = new GetHubProductDetailsQuery(productId);
        HubProductDetailsResult appResult = queryService.getHubProductDetails(query.productId());
        
        HubProductDetailsResponse webResponse = new HubProductDetailsResponse(
                appResult.id(),
                appResult.key(),
                appResult.name(),
                appResult.description(),
                appResult.status(),
                appResult.pricing().stream().map(p -> new HubProductDetailsResponse.Pricing(
                        p.pricingId(), p.billingCycle(), p.amount().amount(), p.amount().currency()
                )).toList()
        );
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Product details retrieved successfully", webResponse, null));
    }
}
