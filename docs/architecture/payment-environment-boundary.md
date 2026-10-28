# Payment TEST/LIVE environment boundary

**Status:** Target architecture decision. Migration from the current global session environment is required.

## Decision

`TEST` and `LIVE` are payment execution environments. They are not global Atlas Hub application environments.

An Atlas Hub user signs in once, selects an optional active organization and receives permissions. Authentication sessions, access tokens and `AuthenticatedPrincipal` do not select a payment environment. Commerce, inventory, HR, accounting, logistics, support and general analytics use ordinary organization data without TEST/LIVE duplication.

The Payment workspace may present a TEST/LIVE switch. That choice is payment UI state and is supplied explicitly to payment APIs; changing it does not rotate the identity session or alter access to non-payment modules.

## Payment-owned scope

Environment remains mandatory for:

- provider authorization and credential references;
- charges, refunds, disputes and payment attempts;
- transfers, payouts, mandates, splits and settlement;
- payment accounts, balances, ledger postings and reconciliation;
- payment transaction projections, payment analytics and payment webhooks;
- idempotency keys, caches and events that represent payment operations.

TEST data must never select LIVE credentials, balances, provider references or webhook secrets. An unknown provider result remains pending in its original environment.

## Authentication and IAM boundary

Remove payment environment from User, authentication Session, JWT claims, user-info responses and the global session-switch endpoint. Organization switching remains an authenticated IAM operation.

Machine credentials follow their purpose:

- a payment API key is permanently bound to TEST or LIVE and determines the payment environment for its requests;
- a general platform API key carries organization, role and scopes without a payment environment;
- storefront publishable keys identify a store and public capability scope and are not payment provider credentials.

Provider verification and production activation belong to the selected provider connection. They do not gate login, organization creation or unrelated business modules.

## API selection

Authenticated dashboard payment endpoints require an explicit payment mode, initially through `X-Atlas-Payment-Mode: TEST|LIVE` or an equivalently explicit versioned payment path. Missing or invalid mode fails closed; it never defaults to LIVE. Payment API key requests derive the mode from the key and reject a conflicting header. Webhooks derive it from the verified endpoint/secret and matched provider authorization.

Application handlers receive a payment-specific context rather than reading environment from the global principal:

```java
public record PaymentExecutionContext(
        Long organizationId,
        Long actorUserId,
        PaymentEnvironment environment
) {}
```

The final HTTP representation may evolve, but explicit selection and server-side enforcement are invariant.

## Events and projections

Payment events retain environment so payment consumers cannot mix TEST and LIVE. Consumers outside Payment must not apply TEST events to production orders, inventory, accounting or general business analytics. If a sandbox commerce simulation is later needed, it must be an explicit isolated feature with its own lifecycle rather than a side effect of the user's authentication session.

General analytics reads organization business data. Payment analytics may filter by payment environment and must receive that filter from the Payment workspace/request context.

## Migration outline

1. Introduce `PaymentEnvironment` and `PaymentExecutionContext` at payment boundaries.
2. Update payment controllers, provider ports, events and projections to use the payment context.
3. Bind payment API keys and provider authorizations to one environment.
4. Stop non-payment controllers and analytics from reading `principal.environment()`.
5. Remove environment from JWT generation, Session, User active state, user info and the authentication switch endpoint.
6. Remove environment from general API keys; retain it only for payment-scoped credentials.
7. Recreate the empty database and verify strict TEST/LIVE isolation throughout Payment.

## Acceptance conditions

- Switching Payment to TEST does not change identity, organization selection or non-payment screens.
- The same authenticated session can make explicit TEST and LIVE payment queries when authorized.
- A TEST key or provider authorization cannot access or create LIVE records.
- TEST payment activity cannot change production orders, stock, accounting or general analytics.
- No payment mutation proceeds with an omitted, inferred or conflicting environment.

