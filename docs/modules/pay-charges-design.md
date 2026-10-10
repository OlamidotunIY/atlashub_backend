# Pay Charges Design (`atlashub-pay:charges`)

## Status and purpose

This module is active and included in `settings.gradle`.

`pay:charges` owns AtlasHub universal checkout and inbound payment attempts. Businesses request a channel, not an internal online gateway:

| Channel | AtlasHub routing |
|---|---|
| Card | Paystack initially |
| USSD | Paystack initially |

Provider names and merchant identifiers are infrastructure details. Public APIs expose only AtlasHub card/USSD channels, capability state, and checkout instructions.

The module never registers a business with a provider. It reads an active profile through `PaymentProviderProfileQueryPort`; `pay:accounts` owns capability intent and `platform:compliance` owns provider onboarding.

## TEST and LIVE

Every charge has `ApiEnvironment TEST | LIVE`.

- Environment comes from the authenticated principal/API key, never request JSON.
- Idempotency is unique by `(organizationId, environment, reference)`.
- TEST uses sandbox resources only and never falls back to LIVE.
- LIVE requires an active provider profile for the requested capability.
- TEST uses AtlasHub's shared Paystack test profile and real Paystack sandbox verification; it never calls Anchor.
- Commerce TEST checkout creates and consumes TEST charges only.

## `Charge` aggregate

```text
id, organizationId, environment, reference
amount, currency, channel
provider, providerProfileId, providerReference?, providerFee?
sourceSystem, sourceReferenceId, customerReferenceId?
terminalAssignmentId?
status: INITIALIZED | PENDING | SUCCESSFUL | FAILED | EXPIRED
        | REFUND_PENDING | PARTIALLY_REFUNDED | REFUNDED
checkoutInstructions?, failureCode?, failureMessage?
providerRefundReference?, refundReason?, refundedAt?
disputeReference?, disputeStatus?, disputeReason?
successfulAt?, expiresAt, createdAt, updatedAt, version
```

Rules:

- Organization, environment, reference, source, amount, and currency are immutable.
- Provider callbacks cannot change local ownership, amount, currency, or source.
- Successful/final transitions are idempotent and cannot regress.
- Total confirmed refunds cannot exceed the successful charge amount.
- Sensitive payment data and provider secrets are never stored in the aggregate.

## Capability resolution

Before initialization, resolve an active profile using `organizationId + environment + capability`:

| Channel | Capability |
|---|---|
| Card | `CARD_COLLECTION` |
| USSD | `USSD_COLLECTION` |
| Bank transfer | `BANK_TRANSFER_COLLECTION` |
| POS | `POS_TERMINAL` for the assigned terminal provider |

No active profile means capability-not-enabled. The error may direct the business to enable the feature but must not reveal the internal card/USSD provider.

## Commands

`InitializeChargeCommand` contains organization and environment from trusted context plus reference, money, channel, source, optional customer, and optional terminal assignment. It:

1. returns the existing organization/environment/reference result;
2. resolves the active capability/provider profile;
3. persists local intent before the remote call;
4. calls the provider outside a database transaction;
5. stores safe checkout instructions;
6. waits for a signature-verified webhook and server-side transaction verification for final state.

`ApplyChargeProviderStatusCommand` is called only by provider webhook/reconciliation listeners. It validates environment, provider profile, reference, amount, and currency before applying an idempotent transition.

`RefundChargeCommand` persists refund intent before calling the provider, creates a full Paystack refund with a stable
idempotency key, and stores the returned refund reference. Completion remains asynchronous; only verified
`refund.processed` changes the charge to `REFUNDED`. `refund.failed` restores the charge to `SUCCESSFUL` with an
auditable failure reason so the operation can be retried.

## Provider port and adapters

The shared application port is provider-neutral:

```java
public interface ChargeProviderPort {
    ChargeInitializationResult initialize(ChargeProviderRequest request);
    RefundResult refund(RefundRequest request);
    ChargeProviderStatus fetchStatus(String providerReference, ApiEnvironment environment);
}
```

Provider modules own credentials, endpoints, signatures, DTOs, timeouts, and retry classification:

- Paystack: card and USSD.

## Webhooks and reconciliation

A provider-specific public endpoint verifies the signature against the raw body before parsing and publishes one verified provider event containing provider event ID and environment. The charge listener deduplicates `(provider, environment, providerEventId, consumer)` and invokes a command only; it never uses repositories. It handles `charge.success`, charge failure, and Paystack's refund lifecycle events; unrelated transfer events are ignored.

Unknown references, amount/currency conflicts, and state regressions are quarantined for reconciliation. Remote calls never run inside database transactions.

## Events and ledger meaning

All events include `organizationId` and `environment`:

- `ChargeInitializedEvent`
- `ChargeSuccessfulEvent`
- `ChargeFailedEvent`
- `ChargeExpiredEvent`
- `ChargeRefundInitiatedEvent`
- `ChargeRefundedEvent`
- `ChargeDisputedEvent`
- `ChargeDisputeResolvedEvent`
- `ProviderSettlementReceivedEvent`

Charge success is not provider settlement. `pay:ledger` posts a successful Paystack/terminal collection through `PROVIDER_CLEARING`. A later `ProviderSettlementReceivedEvent` clears that receivable into confirmed bank/settlement funds without recognizing the sale twice.

## Commerce contract

Commerce publishes `CheckoutPaymentRequestedEvent`; charges later publishes success/failure. There is no synchronous cross-module write. Commerce never sees provider credentials or merchant IDs. TEST events can complete TEST orders only, and LIVE events can complete LIVE orders only.

## API and persistence

Controllers use `AuthenticatedPrincipal.activeOrganizationId()` and `.environment()`:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/pay/charges` | Initialize universal checkout |
| `POST` | `/api/v1/pay/charges/{chargeId}/refunds` | Request refund |

Request DTOs never contain organization ID, environment, or internal provider for card/USSD. POS uses an AtlasHub terminal-assignment ID.

Persistence requirements:

- unique `(organization_id, api_environment, reference)`;
- unique provider reference within `(provider, api_environment)`;
- optimistic aggregate locking;
- persisted provider dispatch/outbox;
- webhook inbox/delivery tracker;
- strict TEST/LIVE separation in records, cache keys, events, ledger postings, and reconciliation.

## Runtime boundary

The module depends only on provider-neutral shared ports. `atlashub-infrastructure:paystack` implements those ports;
no charges application or domain code imports Paystack infrastructure or provider DTOs.
