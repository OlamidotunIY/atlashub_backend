package com.atlashub.billing.adapter.in.web.controller;

import com.atlashub.billing.adapter.in.web.request.CheckoutSubscriptionRequest;
import com.atlashub.billing.adapter.in.web.response.CheckoutSubscriptionResponse;
import com.atlashub.billing.application.command.CheckoutSubscriptionCommand;
import com.atlashub.billing.application.usecase.CheckoutSubscriptionUseCase;
import com.atlashub.shared.application.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final CheckoutSubscriptionUseCase checkoutUseCase;

    public BillingController(CheckoutSubscriptionUseCase checkoutUseCase) {
        this.checkoutUseCase = checkoutUseCase;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutSubscriptionResponse>> checkout(
            @RequestBody @Valid CheckoutSubscriptionRequest request,
            Principal principal) {

        CheckoutSubscriptionCommand command = new CheckoutSubscriptionCommand(
                Long.valueOf(principal.getName()),
                request.productId(),
                request.paymentMethod()
        );

        checkoutUseCase.execute(command);
        return ResponseEntity.accepted().body(new ApiResponse<>(true, "Checkout initiated", new CheckoutSubscriptionResponse("Checkout initiated"), null));
    }
}
