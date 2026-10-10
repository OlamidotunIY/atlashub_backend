# Paystack Integration

## Current role

Paystack provides AtlasHub's hosted card and USSD checkout, full-refund transport, live merchant settlement-route
configuration, and settlement-batch evidence. It does not own AtlasHub charges, business balances, ledger entries,
or final settlement confirmation.

| Module | Responsibility |
|---|---|
| `atlashub-infrastructure:paystack` | Credentials, typed clients, provider DTOs, HMAC verification, webhook ingestion, shared-port adapters |
| `atlashub-pay:accounts` | Organization provider profiles, capabilities, live settlement-route state |
| `atlashub-platform:compliance` | `ProviderOnboardingCase` lifecycle |
| `atlashub-pay:charges` | AtlasHub charge and refund lifecycle |
| `atlashub-pay:settlement` | Paystack payout evidence plus Anchor credit reconciliation |
| `atlashub-pay:ledger` | Idempotent charge-receipt and final-settlement postings |

No business module imports Paystack clients or DTOs. The infrastructure module implements provider-neutral contracts
from `atlashub-shared`.

## Environment behavior

- TEST uses AtlasHub's shared Paystack test credentials and an organization-scoped active test profile.
- TEST never provisions Anchor resources and never falls back to LIVE credentials.
- LIVE collection is disabled until the Anchor operating account and Paystack settlement route are confirmed and the
  provider-onboarding approval event activates the profile.
- Paystack keys and webhook secrets are configured independently for TEST and LIVE.

## Configuration

```yaml
atlashub:
  integrations:
    paystack:
      enabled: ${ATLASHUB_PAYSTACK_ENABLED:false}
      test-merchant-id: ${ATLASHUB_PAYSTACK_TEST_MERCHANT_ID:atlashub-paystack-test}
      test:
        base-url: ${ATLASHUB_PAYSTACK_TEST_BASE_URL:https://api.paystack.co}
        public-key: ${ATLASHUB_PAYSTACK_TEST_PUBLIC_KEY:}
        secret-key: ${ATLASHUB_PAYSTACK_TEST_SECRET_KEY:}
        webhook-secret: ${ATLASHUB_PAYSTACK_TEST_WEBHOOK_SECRET:}
        connect-timeout: ${ATLASHUB_PAYSTACK_TEST_CONNECT_TIMEOUT:2s}
        read-timeout: ${ATLASHUB_PAYSTACK_TEST_READ_TIMEOUT:10s}
      live:
        base-url: ${ATLASHUB_PAYSTACK_LIVE_BASE_URL:https://api.paystack.co}
        public-key: ${ATLASHUB_PAYSTACK_LIVE_PUBLIC_KEY:}
        secret-key: ${ATLASHUB_PAYSTACK_LIVE_SECRET_KEY:}
        webhook-secret: ${ATLASHUB_PAYSTACK_LIVE_WEBHOOK_SECRET:}
        connect-timeout: ${ATLASHUB_PAYSTACK_LIVE_CONNECT_TIMEOUT:2s}
        read-timeout: ${ATLASHUB_PAYSTACK_LIVE_READ_TIMEOUT:10s}
```

When enabled, every required environment property must be valid. Base URLs must use HTTPS. Secrets must come from
deployment configuration and must never be committed.

## Checkout flow

1. A business module publishes `CheckoutPaymentRequestedEvent` or an authenticated caller uses
   `POST /api/v1/pay/charges`.
2. `pay:charges` resolves an active `CARD_COLLECTION` or `USSD_COLLECTION` profile.
3. It persists local intent, then calls `ChargeProviderPort.initialize` outside the transaction.
4. Paystack receives only card/USSD channels, AtlasHub metadata, and the LIVE subaccount settlement route.
5. AtlasHub returns the hosted-checkout authorization URL/access code.
6. Browser callbacks are advisory. Only a signature-verified webhook followed by server-side verification finalizes
   a charge.
7. Provider-neutral events update the source workflow and ledger idempotently.

## Webhook endpoint

```http
POST /api/v1/webhooks/paystack/{environment}
X-Paystack-Signature: <HMAC-SHA512 hex digest>
Content-Type: application/json
```

`environment` is `test` or `live`. The controller verifies the unmodified body with that environment's webhook
secret before parsing or publishing. The charges consumer handles charge success/failure and refund lifecycle events.
Duplicate provider events and transaction references cannot post duplicate ledger balances. Unrelated transfer events
are ignored.

## Refunds

`POST /api/v1/pay/charges/{chargeId}/refunds` creates a full refund for a successful charge. AtlasHub persists intent
first, then calls Paystack with the original transaction reference, exact amount/currency, reason, and a stable
idempotency key. Initiation returns `REFUND_PENDING`; only verified `refund.processed` changes it to `REFUNDED`.
`refund.failed` restores the charge to `SUCCESSFUL` with an auditable failure reason.

## Live settlement routing

After Anchor activates the organization's `CURRENT` operating deposit account, `pay:accounts` asks Paystack to resolve
the bank code/account number and creates or updates one Paystack subaccount. A confirmed route advances the existing
`ProviderOnboardingCase`; `OrganizationProviderProfile` remains provisioning until its approval event activates card
and USSD capabilities.

The Paystack subaccount code is `externalMerchantId`. The local Anchor deposit-account ID is
`settlementAccountReference`. Anchor subaccounts and reserved accounts are not involved in card/USSD settlement.

## Settlement reconciliation

`pay:settlement` polls the Paystack Settlement API per active LIVE subaccount and records each batch plus transaction
references. Paystack success is payout evidence, not proof of bank receipt. Final settlement requires a verified Anchor
credit matching the operating account, environment, currency, and net amount.

Unmatched batches remain pending for three business days, then move to operations reconciliation. The final ledger
movement is keyed by provider settlement ID and emitted only after the two-sided match. `transfer.success` is not used
as settlement confirmation.

See [Paystack infrastructure design](../modules/paystack-design.md),
[charges design](../modules/pay-charges-design.md), and
[settlement design](../modules/pay-settlement-design.md).
