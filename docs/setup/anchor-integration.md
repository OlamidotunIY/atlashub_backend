# Anchor Integration

## Boundary

Anchor is AtlasHub's banking system; AtlasHub remains the source of truth for ownership, intent, environment, capability, routing, and local lifecycle.

AtlasHub uses Anchor for business customers, business deposit accounts, FBO-parented business subaccounts, customer/vendor reserved accounts, inbound transfers, and Anchor transfers. Anchor is not a card or universal charge gateway. Card and USSD route through Paystack initially; POS routes through the business's selected Paystack, Moniepoint, or OPay terminal.

## Shared infrastructure module

`atlashub-infrastructure:anchor` owns transport configuration and typed HTTP clients reused by consuming modules:

```text
configuration/
  AnchorProperties
  AnchorClientConfiguration
  AnchorEnvironment
client/
  AnchorDepositAccountClient
  AnchorSubAccountClient
  AnchorReservedAccountClient
  AnchorWebhookClient
infrastructure/webhook/
  AnchorWebhookSignatureVerifier
  AnchorWebhookSubscriptionProvisioner
presentation/rest/
  AnchorWebhookController
```

Consuming modules define provider-neutral ports and adapters that map their domain requests to these clients. Provider DTOs do not enter domain/application contracts.

## Environments and programme capabilities

AtlasHub `TEST` maps to Anchor Sandbox (`https://api.sandbox.getanchor.co`); `LIVE` maps to Anchor Live (`https://api.getanchor.co`). Each has separate API keys, webhook tokens/subscriptions, timeouts, capability flags, and FBO account ID.

Sandbox supports customers, deposit accounts, and transfers. Subaccounts and reserved accounts are enabled only when the Sandbox programme and configured Sandbox FBO account support them. An unavailable TEST capability fails explicitly; code must never fall back to LIVE.

The application configuration uses:

```text
atlashub.integrations.anchor.sandbox.*
atlashub.integrations.anchor.live.*
```

When subaccounts or reserved accounts are enabled, the matching environment's FBO account ID is mandatory. It is never accepted from an organization request or hard-coded in business logic.

## Account model

- Every approved AtlasHub business receives an Anchor `CURRENT` DepositAccount.
- Every business receives an Anchor SubAccount parented by AtlasHub's environment-specific FBO account when that capability is available.
- ReservedAccounts are issued only to the business's customers or marketplace vendors and route to the business subaccount.
- AtlasHub ledger accounts are internal and are never modeled as Anchor subaccounts.

Provider calls are dispatched from persisted local intent. Remote HTTP calls do not run inside the transaction that creates the aggregate.

## Webhook subscriptions

AtlasHub creates separate Anchor subscriptions per environment and consuming module (currently compliance and pay-accounts). Subscription creation uses Anchor's webhook API, `AtLeastOnce` delivery, and included resources. A label collision with a different callback contract is treated as a configuration conflict rather than silently overwritten.

Callbacks are environment- and consumer-specific:

```text
POST /api/v1/webhooks/anchor/{environment}/{consumer}
```

The controller preserves the raw body. `AnchorWebhookSignatureVerifier` validates Anchor's signature with the token for that exact environment/consumer before JSON parsing.

After verification, the Anchor module publishes one `AnchorWebhookReceivedEvent` containing:

- provider event ID;
- Anchor environment;
- intended consumer;
- event type and occurrence time;
- event attributes/relationships;
- included provider resources.

Consumers use this same verified event and translate it into their own commands. They do not define or publish a second duplicate Anchor event. Listeners invoke commands only and never repositories.

## Idempotency and reconciliation

- Webhook delivery is deduplicated by provider event ID plus consumer group.
- Provider requests use AtlasHub references unique with environment.
- Unknown resources, conflicting ownership, and amount/currency conflicts go to reconciliation/DLQ; they do not create local ownership.
- Timeouts retain local intent and are reconciled before retrying resource creation.
- Logs and errors never expose API keys, webhook tokens, BVNs, full account numbers, or raw provider secrets.

## Endpoint ownership

Typed paths currently used are:

| Resource | Path |
|---|---|
| Deposit accounts | `/api/v1/accounts` |
| Subaccounts | `/api/v1/sub-accounts` |
| Reserved accounts | `/pay/reserved-account` |
| Webhook subscriptions | `/api/v1/webhooks` |

Paths live only in the typed Anchor clients. Domain adapters call client methods and do not concatenate URLs.
