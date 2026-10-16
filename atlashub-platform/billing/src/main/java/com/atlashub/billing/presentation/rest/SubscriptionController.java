package com.atlashub.billing.presentation.rest;

import com.atlashub.billing.application.command.CancelSubscription.CancelSubscriptionCommand;
import com.atlashub.billing.application.command.CancelSubscription.CancelSubscriptionHandler;
import com.atlashub.billing.application.command.CancelSubscription.CancelSubscriptionResult;
import com.atlashub.billing.application.command.ChangePlan.ChangePlanCommand;
import com.atlashub.billing.application.command.ChangePlan.ChangePlanHandler;
import com.atlashub.billing.application.command.ChangePlan.ChangePlanResult;
import com.atlashub.billing.application.query.GetCurrentBillingPeriod.BillingPeriodResult;
import com.atlashub.billing.application.query.GetCurrentBillingPeriod.GetCurrentBillingPeriodHandler;
import com.atlashub.billing.application.query.GetCurrentBillingPeriod.GetCurrentBillingPeriodQuery;
import com.atlashub.billing.application.query.GetSubscription.GetSubscriptionHandler;
import com.atlashub.billing.application.query.GetSubscription.GetSubscriptionQuery;
import com.atlashub.billing.application.query.GetSubscription.SubscriptionResult;
import com.atlashub.billing.presentation.dto.BillingPeriodResponse;
import com.atlashub.billing.presentation.dto.CancelSubscriptionRequest;
import com.atlashub.billing.presentation.dto.ChangePlanRequest;
import com.atlashub.billing.presentation.dto.SubscriptionResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Billing Subscriptions", description = "Universal organization subscription management")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final GetSubscriptionHandler getSubscriptionHandler;
    private final GetCurrentBillingPeriodHandler getCurrentBillingPeriodHandler;
    private final ChangePlanHandler changePlanHandler;
    private final CancelSubscriptionHandler cancelSubscriptionHandler;

    public SubscriptionController(
            GetSubscriptionHandler getSubscriptionHandler,
            GetCurrentBillingPeriodHandler getCurrentBillingPeriodHandler,
            ChangePlanHandler changePlanHandler,
            CancelSubscriptionHandler cancelSubscriptionHandler
    ) {
        this.getSubscriptionHandler = getSubscriptionHandler;
        this.getCurrentBillingPeriodHandler = getCurrentBillingPeriodHandler;
        this.changePlanHandler = changePlanHandler;
        this.cancelSubscriptionHandler = cancelSubscriptionHandler;
    }

    @GetMapping
    @Operation(summary = "Get current organization subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        SubscriptionResult result = getSubscriptionHandler.execute(
                new GetSubscriptionQuery(principal.activeOrganizationId())
        );

        SubscriptionResponse response = new SubscriptionResponse(
                result.id(),
                result.organizationId(),
                result.pricingPlanId(),
                result.status(),
                result.currentPeriodStart(),
                result.currentPeriodEnd(),
                result.nextBillingDate(),
                result.billingCurrency(),
                result.trialEndsAt(),
                result.cancelledAt(),
                result.cancelReason(),
                result.createdAt(),
                result.updatedAt()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription fetched successfully", response, null));
    }

    @GetMapping("/billing-period")
    @Operation(summary = "Get current billing period for a subscription")
    public ResponseEntity<ApiResponse<BillingPeriodResponse>> getCurrentBillingPeriod(
            @RequestParam(name = "subscriptionId") Long subscriptionId
    ) {
        BillingPeriodResult result = getCurrentBillingPeriodHandler.execute(
                new GetCurrentBillingPeriodQuery(subscriptionId)
        );

        BillingPeriodResponse response = new BillingPeriodResponse(
                result.subscriptionId(),
                result.currentPeriodStart(),
                result.currentPeriodEnd(),
                result.nextBillingDate(),
                result.status()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Billing period fetched successfully", response, null));
    }

    @PutMapping("/plan")
    @Operation(summary = "Change pricing plan tier")
    public ResponseEntity<ApiResponse<ChangePlanResult>> changePlan(
            @RequestParam(name = "subscriptionId") Long subscriptionId,
            @Valid @RequestBody ChangePlanRequest request
    ) {
        ChangePlanResult result = changePlanHandler.execute(
                new ChangePlanCommand(subscriptionId, request.newPricingPlanId())
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription plan updated successfully", result, null));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel subscription at period end")
    public ResponseEntity<ApiResponse<CancelSubscriptionResult>> cancelSubscription(
            @RequestParam(name = "subscriptionId") Long subscriptionId,
            @Valid @RequestBody CancelSubscriptionRequest request
    ) {
        CancelSubscriptionResult result = cancelSubscriptionHandler.execute(
                new CancelSubscriptionCommand(subscriptionId, request.reason())
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription cancelled successfully", result, null));
    }
}
