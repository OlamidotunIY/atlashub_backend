# Paystack Infrastructure Design (`atlashub-infrastructure:paystack`)

## Purpose

This module is AtlasHub's only Paystack transport boundary. It owns credentials, HTTP clients, provider DTOs,
signature verification, webhook ingestion, timeout configuration, and mapping to provider-neutral shared ports.
Business modules never import `com.atlashub.paystack.*`.

## Boundaries

- `domain`: not used; Paystack is not an AtlasHub business domain.
- `application`: webhook ingestion command and validation orchestration.
- `infrastructure/external/paystack`: configuration, typed clients, provider DTOs, signatures, publishers, and shared-port adapters.
- `presentation`: the raw-body webhook endpoint and its error mapping.

The module implements these provider-neutral contracts from `atlashub-shared`:

- `ChargeProviderPort` for transaction initialization and server-side verification;
- `PaystackSettlementOnboardingPort` for account resolution and subaccount create/update;
- `SettlementProviderPort` for successful settlement batches and their transactions.

## Environments and security

TEST and LIVE have independent secret keys, webhook secrets, timeouts, and clients. A request selects exactly one
environment and never falls back to the other. Webhook signatures are HMAC-SHA512 over the unmodified request body.
Only a verified webhook is published to `paystack-events`; browser redirects are never authoritative.

## Collection

Card and USSD initialization accepts an AtlasHub reference and idempotency key. LIVE includes the organization's
Paystack subaccount and assigns provider fees to that subaccount. Final charge state is determined only after the
charges module asks this module to verify the transaction with Paystack.

Full refunds are created through the same provider-neutral charge port using the transaction reference, exact amount,
currency, reason, and a stable AtlasHub idempotency key. Paystack refund DTOs remain in this module. Verified
`refund.pending`, `refund.processing`, `refund.needs-attention`, `refund.failed`, and `refund.processed` webhooks are
published for the charges module to apply without trusting a browser callback.

## Settlement onboarding

For LIVE, the adapter resolves the confirmed Anchor operating account through Paystack, then creates or updates one
Paystack subaccount with automatic settlement. The returned subaccount code is the external merchant identifier.
TEST registration does not call this flow.

## Settlement evidence

The settlement adapter lists successful Paystack Settlement API batches per subaccount and lists each batch's
transactions for audit. It does not consume `transfer.success` and does not claim that money reached Anchor.
The settlement domain performs that second-side match.

## Failure and idempotency rules

- Provider calls use bounded connect/read timeouts.
- Create/update/initialize calls carry stable AtlasHub idempotency references.
- Missing or contradictory provider data fails closed.
- Provider DTOs never leave this module; only shared records are returned.
- Webhook consumer delivery tracking and downstream aggregate uniqueness make retries safe.
