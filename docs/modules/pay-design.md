# Pay Module Design (`atlashub-pay`) — Index

## Overview

The `atlashub-pay` module is the **financial rails** of AtlasHub. Every movement of money flows through this module's infrastructure. No other module holds, moves, or accounts for real money independently — they all delegate to `atlashub-pay`.

Pay is also **B2B2C**: organizations subscribing to Atlas Pay can expose the payment infrastructure to their own end-customers via API key + HMAC authentication.

The module is a parent Gradle project with ten independent submodules.

---

## Submodule Documentation

| Submodule | Gradle Path | Package | Design Doc |
|---|---|---|---|
| **Virtual Accounts** | `atlashub-pay:accounts` | `com.atlashub.pay.accounts` | [pay-accounts-design.md](pay-accounts-design.md) |
| **Double-Entry Ledger** | `atlashub-pay:ledger` | `com.atlashub.pay.ledger` | [pay-ledger-design.md](pay-ledger-design.md) |
| **Inbound Payments** | `atlashub-pay:charges` | `com.atlashub.pay.charges` | [pay-charges-design.md](pay-charges-design.md) |
| **Outbound Transfers** | `atlashub-pay:transfers` | `com.atlashub.pay.transfers` | [pay-transfers-design.md](pay-transfers-design.md) |
| **Revenue Splits** | `atlashub-pay:splits` | `com.atlashub.pay.splits` | [pay-splits-design.md](pay-splits-design.md) |
| **Recurring Mandates** | `atlashub-pay:mandates` | `com.atlashub.pay.mandates` | [pay-mandates-design.md](pay-mandates-design.md) |
| **Settlement Tracking** | `atlashub-pay:settlement` | `com.atlashub.pay.settlement` | [pay-settlement-design.md](pay-settlement-design.md) |
| **Transaction History** | `atlashub-pay:tx-query` | `com.atlashub.pay.txquery` | [pay-tx-query-design.md](pay-tx-query-design.md) |
| **Outbound Webhooks** | `atlashub-pay:webhooks` | `com.atlashub.pay.webhooks` | [pay-webhooks-design.md](pay-webhooks-design.md) |
| **Financing Payment Controls** | `atlashub-pay:controls` | `com.atlashub.pay.controls` | [pay-controls-design.md](pay-controls-design.md) |

---

## Key Cross-Cutting Concerns

### External Provider Integrations
- **Anchor** — Virtual account issuance (NUBANs), collection webhooks
- **Paystack** — Card, bank transfer, USSD, settlement; API + webhooks
- **Moniepoint** — Physical POS terminals; API + webhooks

### The Shadow Ledger Principle
`atlashub-pay:ledger` maintains an internal double-entry ledger that shadows real money held at Anchor/Paystack. Every external money event produces a `LedgerTransaction` — giving AtlasHub a real-time, independently auditable balance per organization.

### Internal Account Structure

Registration idempotently bootstraps `OPERATING`, `PROVIDER_CLEARING`, and `SUSPENSE` in both TEST and LIVE.
Businesses create optional payroll, tax, escrow, split, and custom accounts when needed; outlet and party accounts are
created lazily by their owning workflows.

### Critical Kafka Events
| Event | Consumers |
|---|---|
| `ChargeSuccessfulEvent` | `commerce`, `billing`, `ledger`, `webhooks`, `tx-query`, `notifications` |
| `PayoutCompletedEvent` | `hr`, `accounting`, `notifications`, `webhooks`, `tx-query` |
| `BulkPayoutCompletedEvent` | `hr` (mark payroll DISBURSED) |
| `LedgerTransactionPostedEvent` | `accounting:gl` (bridge listener), `pay:tx-query` |
| `ProviderSettlementReceivedEvent` | `pay:ledger`, `accounting`, `webhooks`, `pay:tx-query` |
| `PaymentHeld` / `PaymentBlocked` / `ControlledFundsFrozen` | `financing`, `notifications`, audit |

### Financing-Control Boundary

`pay:controls` enforces a Financing-owned facility policy only on AtlasHub-controlled ledger balances, virtual accounts, payment instructions, and provider rails whose capabilities explicitly support the requested action. It can automatically hold, block, or freeze those supported paths. It cannot freeze arbitrary external accounts and never performs debt recovery, GSI, collateral enforcement, or lender underwriting.

### Locking
- `LedgerAccount` — **Pessimistic Write** (ascending account ID order) — non-negotiable
- `Payout` — **Pessimistic Write** during approval
- `Charge`, `VirtualAccount`, `SplitRule`, `PaymentMandate` — Optimistic

---

## Gradle

```groovy
// settings.gradle
include 'atlashub-pay'
include 'atlashub-pay:accounts'
include 'atlashub-pay:ledger'
include 'atlashub-pay:charges'
include 'atlashub-pay:transfers'
include 'atlashub-pay:splits'
include 'atlashub-pay:mandates'
include 'atlashub-pay:settlement'
include 'atlashub-pay:tx-query'
include 'atlashub-pay:webhooks'
include 'atlashub-pay:controls'
```
