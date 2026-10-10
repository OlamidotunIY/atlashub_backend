# Pay Accounts Design (`atlashub-pay:accounts`)

## Purpose

`pay:accounts` owns organization banking/provider profiles and Anchor banking resources. It does not own ledger
balances or transaction history.

AtlasHub is the source of truth for provisioning intent, ownership, lifecycle, restrictions, idempotency, and local
references. Anchor and Paystack remain execution systems whose external identifiers and confirmed statuses are
recorded locally.

## Registration and environment behavior

On `OrganizationRegistered`:

- TEST receives one active shared Paystack `OrganizationProviderProfile` with `CARD_COLLECTION` and
  `USSD_COLLECTION`.
- No Anchor sandbox customer, deposit account, subaccount, or reserved account is created.
- LIVE collection remains unavailable until compliance approval, Anchor operating-account activation, and Paystack
  settlement-route onboarding all succeed.

TEST charges therefore exercise the real Paystack test API/webhook path and AtlasHub ledger, but do not pretend that
test money was deposited into Anchor.

## Core aggregates

### `OrganizationBankingProfile`

Environment-scoped banking lifecycle and independent restrictions. A LIVE banking profile becomes active when its
Anchor `BusinessDepositAccount` is confirmed active. `businessSubAccountId` is optional and is not a prerequisite for
card/USSD collection.

### `BusinessDepositAccount`

The organization's Anchor `CURRENT` operating account. It stores the local aggregate ID, safe external Anchor
reference, encrypted account number, bank code/name, account name, currency, lifecycle status, and timestamps.

The deposit account is the settlement destination for the organization's Paystack collection route.

### `OrganizationProviderProfile`

The capability source of truth per organization/environment/provider. It stores requested and active capabilities,
onboarding lifecycle, Paystack merchant/subaccount code in `externalMerchantId`, and the local Anchor deposit-account
ID in `settlementAccountReference`.

- TEST: activated from AtlasHub's shared Paystack test configuration.
- LIVE: pending until Paystack verifies the confirmed Anchor bank destination and activates the route.

### `ProviderOnboardingCase`

Records pending, information-required, rejected, failed/retrying, and approved provider onboarding state. Provider
errors do not fabricate an active capability and retain retry-safe local/external references.

### Optional transfer collection

`BusinessSubAccount`, `SUB_ACCOUNT` provider requests, and `ReservedAccount` remain available only behind the
explicit transfer-collection enablement workflow. They are not used for Paystack card/USSD settlement.

Transfer-collection state distinguishes:

- not requested
- provisioning requested/pending
- active
- failed/retryable

Reserved-account issuance is rejected unless transfer collection is active and the organization subaccount is
confirmed active. Existing external subaccounts/reserved accounts are preserved.

## LIVE onboarding flow

1. AtlasHub compliance is approved and supplies the real Anchor business-customer reference.
2. `ProvisionOrganizationBankingHandler` creates only the local deposit-account request and `DEPOSIT` provider
   request.
3. The Anchor adapter creates a `CURRENT` deposit account.
4. Provider response/webhook/reconciliation confirms status and complete account details.
5. `ApplyAnchorAccountStatusHandler` activates the deposit account and banking profile; it does not create an FBO
   subaccount.
6. `BusinessDepositAccountActivatedEvent` triggers Paystack settlement-route configuration.
7. The Paystack adapter resolves the account using bank code/account number and creates or updates exactly one
   Paystack subaccount/merchant settlement destination.
8. `ProviderOnboardingCase` and `OrganizationProviderProfile` reflect the Paystack result. Card/USSD capabilities
   become active only after Paystack confirms the route.

Anchor or Paystack failure leaves LIVE collection pending/failed with retry-safe references. It never falls back to
TEST credentials or a different organization's route.

## Funding and settlement events

Verified Anchor credits to an organization operating account publish `OrganizationAccountFundedEvent`. Verified
reserved-account credits publish `ReservedAccountFundedEvent` with customer/vendor attribution. Ledger and
`pay:tx-query` consume compatible event contracts idempotently.

An operating-account credit does not by itself mean a Paystack settlement is reconciled. `pay:settlement` matches it
against Paystack settlement evidence using destination, currency, and net amount before publishing
`ProviderSettlementReceivedEvent`.

## Shared read contract

`BusinessBankingQueryAdapter` implements shared `BusinessBankingQueryPort` from accounts-owned persistence. It returns
only the active operating account's safe display data: local ID, account name, masked account number, bank name/code,
currency, status, and activation time. Ledger uses this to enrich the organization operating-account response without
importing accounts internals.

## Security and persistence

- Every aggregate and provider request is scoped to `ApiEnvironment`.
- Full account numbers are encrypted at rest; ordinary APIs use masked values.
- Provider IDs and idempotency references are unique with environment.
- Organization and environment come from authenticated context/events, never trusted request bodies.
- No destructive migration removes existing deposit/subaccount/reserved-account rows or external resources.
