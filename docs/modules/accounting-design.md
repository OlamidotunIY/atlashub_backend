# Accounting Module Design (`atlashub-accounting`) — Index

## Overview

The `atlashub-accounting` module is the **financial record and reporting engine** of AtlasHub. It is a single Gradle module with six internal subpackages, each documented separately.

All subpackages share the root package `com.atlashub.accounting`.

---

## Subpackage Documentation

| Subpackage | Package | Design Doc |
|---|---|---|
| **General Ledger** | `com.atlashub.accounting.gl` | [accounting-gl-design.md](accounting-gl-design.md) |
| **Accounts Payable & Receivable** | `com.atlashub.accounting.ap` + `com.atlashub.accounting.ar` | [accounting-ap-ar-design.md](accounting-ap-ar-design.md) |
| **Fixed Assets** | `com.atlashub.accounting.assets` | [accounting-assets-design.md](accounting-assets-design.md) |
| **Cash & Bank Reconciliation** | `com.atlashub.accounting.cash` | [accounting-cash-design.md](accounting-cash-design.md) |
| **Budget Management** | `com.atlashub.accounting.budget` | [accounting-budget-design.md](accounting-budget-design.md) |

---

## Key Cross-Cutting Concerns

### Two-Ledger Architecture
`atlashub-pay:ledger` (real-time cash tracking) ← bridged by → `accounting:gl` (financial reporting). The `LedgerBridgeListener` maps Pay `SourceSystem` codes to GL account pairs via `LedgerAccountMapping`.

### Maker-Checker
Applied to: **Manual Journal Entries** (>₦500k threshold), **Budgets**. Same domain-level enforcement: `approve()` rejects self-approval. See [maker-checker.md](../architecture/maker-checker.md).

### Inbox Idempotency (CRITICAL)
Every accounting listener uses `EventDeliveryTracker` with `(eventId, groupId)` as the idempotency key. Duplicate journal entries corrupt the books permanently — this is the highest-risk module for replay attacks.

### Module Dependencies
- **Reads from shared:** `UserQueryPort`, `OrganizationQueryPort`
- **Consumes events from:** `commerce-events`, `hr-events`, `pay-events`, `logistics-events`, `compliance-events`
- **Publishes to:** `accounting-events` topic consumed by `analytics`, `notifications`

### Intelligence and Financing Data Contract

Accounting is an essential input to Atlas Intelligence and AtlasScore. It remains the authoritative owner of journal-backed profit/loss, gross profit and margin, operating-expense growth, payroll burden, receivable/payable exposure, reconciled cash-flow, and financial-close corrections.

It must publish normalized `FinancialPeriodClosed` and `FinancialHealthAdjusted` events for `atlashub-analytics` to materialize MySQL `FinancialHealthProjection` rows. It must not expose GL tables or aggregate internals to Analytics, Intelligence, or Financing. Historical score/application decisions reference the resulting immutable Analytics feature snapshot, not mutable current accounting rows.

Accounting does not decide financing eligibility, lender underwriting, fund-use policy, or recovery. It supplies reproducible financial facts and audit evidence only.

---

## Gradle

```groovy
// settings.gradle
include 'atlashub-accounting'
```
