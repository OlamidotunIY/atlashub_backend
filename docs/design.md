# AtlasHub Platform — System Design Overview

## What Is AtlasHub?

AtlasHub is an **all-in-one business operating platform** for African businesses. It gives organizations a single place to manage their payments, commerce operations, logistics, HR, and accounting — with all modules communicating seamlessly through a shared event infrastructure.

Unlike point solutions (e.g., "just a POS app" or "just an HR tool"), AtlasHub's modules are deeply integrated. A sale made at a POS terminal automatically updates stock, triggers an accounting entry, and if payment was via card, reconciles to the organization's virtual account — all without the organization configuring any integration.

---

## Platform Architecture

AtlasHub is a **Modular Monolith** following Hexagonal Architecture (Ports and Adapters) and Domain-Driven Design (DDD) with CQRS. All modules share a single deployment unit but maintain strict domain boundaries — no module directly calls another module's database or internal classes.

Cross-module communication happens exclusively through:
1. **Domain Events** (async, via Outbox/Inbox pattern)
2. **Shared Kernel Interfaces** (sync, via Spring dependency injection for same-JVM calls — e.g., `AccountQueryPort`)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         atlashub-platform                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │ identity │  │   auth   │  │  admin   │  │ catalog  │  │ billing  │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
┌──────────────┐  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐
│ atlashub-pay │  │atlashub-commerce │  │atlashub-     │  │ atlashub-hr  │
│              │  │                  │  │ logistics    │  │              │
│ accounts     │  │ catalog          │  │ shipping     │  │ staff        │
│ ledger       │  │ storefront (POS) │  │ warehousing  │  │ payroll      │
│ charges      │  │ inventory        │  │ returns      │  │ leave        │
│ transfers    │  │                  │  │              │  │ attendance   │
│ splits       │  │                  │  │              │  │ loans        │
│ subscriptions│  │                  │  │              │  │              │
│ settlement   │  │                  │  │              │  │              │
│ tx-query     │  │                  │  │              │  │              │
└──────────────┘  └──────────────────┘  └──────────────┘  └──────────────┘
┌─────────────────────────────────────────────────────────────────────────┐
│                       atlashub-accounting                               │
│         gl (General Ledger)  │  ap/ar  │  assets  │  cash  │  budget   │
└─────────────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────────────┐
│                      atlashub-infrastructure                            │
│          eventbus (Outbox/Inbox)  │  notifications  │  audit            │
│          rate-limiter             │  file-storage                       │
└─────────────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────────────┐
│                          atlashub-shared                                │
│      Money, AggregateRoot, DomainEvent, BaseUseCase, Exceptions,        │
│      Shared Ports (AccountQueryPort), Value Objects                     │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Module Directory

| Module | Package | Purpose |
|---|---|---|
| [identity](modules/identity-design.md) | `atlashub-platform:identity` | Organizations, Users, Members, Invitations, Compliance (KYC) |
| [auth](modules/auth-design.md) | `atlashub-platform:auth` | Passwords, Sessions, JWT, Email Verification |
| [admin](modules/admin-design.md) | `atlashub-platform:admin` | AtlasHub staff management, KYC review, catalog management |
| [catalog](modules/catalog-design.md) | `atlashub-platform:catalog` | Platform product catalog (Atlas Pay, Commerce, etc.) and pricing |
| [billing](modules/billing-design.md) | `atlashub-platform:billing` | Organizational subscriptions, invoices, and access control |
| [pay](modules/pay-design.md) | `atlashub-pay` | All money movement: virtual accounts, ledger, charges, payouts, splits |
| [commerce](modules/commerce-design.md) | `atlashub-commerce` | POS, inventory, product catalog, hospitality, suppliers |
| [logistics](modules/logistics-design.md) | `atlashub-logistics` | Shipments, fleet, riders, stock transfers, returns |
| [hr](modules/hr-design.md) | `atlashub-hr` | Employees, payroll, leave, attendance, loans |
| [accounting](modules/accounting-design.md) | `atlashub-accounting` | General ledger, journal entries, reports, assets, budgets |
| [infrastructure](modules/infrastructure-design.md) | `atlashub-infrastructure` | Outbox/Inbox, notifications, audit, rate limiting |

---

## The Organization Journey

Here is how a real organization interacts with AtlasHub end-to-end:

### 1. Onboarding (Identity + Auth + Billing)
1. Business owner registers → `identity` creates `User` + `Organization` + `OrganizationMember(OWNER)`.
2. `auth` creates `AuthAccount`, sends setup email.
3. Owner logs in, completes 5-step KYC compliance form.
4. Submits compliance → `admin` reviews → **ApproveCompliance**.
5. On approval: `pay:accounts` issues a live NUBAN (via Anchor). Organization can now receive bank transfers.
6. Owner subscribes to Atlas Pay + Atlas Commerce via `billing` → invoices generated.

### 2. Daily Commerce Operations (Commerce + Pay + Accounting)
1. Manager opens the till → `CloseTillUseCase` posts opening float to ledger (Debit Till Account, Credit Operating Account).
2. Cashier processes sales → `SalesOrder` created → stock reserved.
3. Customer pays by card → Paystack checkout → webhook received → `PaymentSuccessfulEvent` → sale completed.
4. Ledger posts: Debit Cash/Clearing, Credit Sales Revenue.
5. `accounting` listener posts journal entry automatically.
6. End of day: till closed → `ReconcileTillCommand` sweeps till cash to Operating Account.

### 3. Supplier Replenishment (Commerce + Logistics + Accounting)
1. Manager raises a `PurchaseOrder` → sent to supplier.
2. Supplier ships goods → logistics creates inbound `Shipment`.
3. Delivery confirmed with POD → `ShipmentDeliveredEvent` → Commerce receives the purchase order.
4. Stock added to inventory at outlet.
5. Accounting posts: Debit Inventory Asset, Credit Accounts Payable.
6. When supplier is paid: pay initiates bank transfer payout → Debit Accounts Payable, Credit Bank Account.

### 4. Monthly Payroll (HR + Pay + Accounting)
1. HR initiates payroll for "2026-09" → calculates gross, allowances, deductions for all employees.
2. Manager reviews and approves → `PayrollApprovedEvent`.
3. Accounting auto-posts: Debit Salary Expense, Credit Payroll Payable.
4. Org must ensure Payroll Reserve Account is funded (via `FundPayrollReserveCommand` in Pay).
5. Disbursement runs: bulk payouts to employee banks via Paystack.
6. On success: `PayrollDisbursedEvent` → Accounting posts: Debit Payroll Payable, Credit Payroll Reserve Account.
7. Each employee gets an SMS: "Your salary of ₦X has been paid."

### 5. Stock Transfer Between Outlets (Commerce + Logistics + Pay + Accounting)
1. Outlet A has excess stock; Outlet B is low. Manager initiates `StockTransfer`.
2. Logistics assigns rider, dispatches goods.
3. Outlet B receives and confirms delivery.
4. Commerce: `addStock(Outlet B)`, `deductStock(Outlet A)`.
5. Pay posts inter-outlet ledger transaction:
   ```
   DEBIT  Till Account (Outlet B)   [cost value]
   CREDIT Till Account (Outlet A)   [cost value]
   ```
6. Accounting posts: Debit Inventory Asset (Outlet B), Credit Inventory Asset (Outlet A).

---

## Key Cross-Module Event Flows

```
ORGANIZATION ONBOARDING
identity: OrganizationRegistered ──────→ billing (prepare for subscriptions)
identity: OrganizationComplianceApproved ─→ pay:accounts (issue NUBAN)
identity: UserCreated ─────────────────→ auth (create AuthAccount)
identity: InvitationAccepted ──────────→ auth (activate account), hr (draft employee)

POS SALE
commerce: ProcessPosCheckoutUseCase
  → pay: InitializePaymentUseCase (sync)
  → [customer pays externally]
  → pay: PaymentSuccessfulEvent
    → commerce: complete sale, deduct stock
    → billing: match invoice if platform billing ref
    → pay:ledger: post DEBIT Clearing, CREDIT Operating Account
    → accounting: post DEBIT Cash, CREDIT Sales Revenue
    → notifications: send receipt

PAYROLL
hr: PayrollApprovedEvent ──────────────→ pay (execute bulk payouts)
pay: BulkPayoutCompletedEvent ─────────→ hr (mark DISBURSED)
hr: PayrollDisbursedEvent ─────────────→ accounting (post final journal)
                                        → notifications (SMS employees)

SUBSCRIPTION RENEWAL
pay: PaymentSuccessfulEvent ───────────→ billing (mark invoice paid)
billing: SubscriptionRenewedEvent ─────→ identity (restore access)
                                        → notifications (renewal confirmation)
```

---

## Two Ledgers — One Source of Truth

AtlasHub operates two complementary ledgers:

| | `atlashub-pay:ledger` | `atlashub-accounting:gl` |
|---|---|---|
| **Purpose** | Real-time cash tracking | Financial reporting |
| **Model** | Double-entry, balance snapshots | Journal entries, Chart of Accounts |
| **Granularity** | Every money movement | Business event-level |
| **Users** | Treasury / cash management | Finance / accounting team |
| **Drives** | Wallet balances, available funds | P&L, Balance Sheet, Cash Flow |

The `LedgerBridgeListener` in accounting maps every `LedgerTransactionPostedEvent` from Pay to the corresponding accounting journal entry using the organization's `LedgerAccountMapping` configuration.

---

## Internal Account Structure (Per Organization)

On onboarding, every organization gets these ledger accounts bootstrapped in `atlashub-pay:ledger`:

| Account | Type | Purpose |
|---|---|---|
| Operating Account | Asset | Daily transactions — revenue in, payments out |
| Payroll Reserve Account | Asset | Funds locked for upcoming payroll disbursement |
| Tax Holding Account | Liability | PAYE, VAT collected but not yet remitted |
| Escrow Account | Asset | Funds held during commerce transactions pending delivery |
| Suspense Account | Asset | Inter-outlet transfer clearing (in-transit) |
| Till Accounts (per outlet) | Asset | Cash at each physical POS till |

---

## External Integration Points

| Provider | Module | What It Does |
|---|---|---|
| **Anchor** | `pay:accounts` | Issues real NUBAN bank accounts per organization. Webhooks notify AtlasHub of incoming transfers. |
| **Paystack** | `pay:charges` | Card, bank transfer, USSD payment collection. Webhooks notify of payment outcomes. Settlement of collected funds to org's bank. |
| **Moniepoint** | `pay:charges` | Physical POS terminal transactions. Webhooks notify of transaction outcomes. |
| **Paystack / Moniepoint** | `pay:transfers` | Outbound bank transfers (salary payouts, supplier payments, refunds). |
| **SendGrid / AWS SES** | `infrastructure:notifications` | Transactional email delivery. |
| **Termii / Twilio** | `infrastructure:notifications` | SMS delivery. |
| **Firebase FCM** | `infrastructure:notifications` | In-app push notifications. |
| **Redis** | `infrastructure:rate-limiter` | Sliding window rate limiting counters. |
| **Kafka / RabbitMQ** | `infrastructure:eventbus` | Async event delivery between modules. |

---

## Folder Structure Convention

Each module follows this structure:

```
{module}/
├── src/main/java/com/atlashub/{module}/
│   ├── domain/
│   │   ├── model/          (Aggregates, Entities)
│   │   ├── valueobject/    (Value Objects, Enums)
│   │   ├── events/         (Domain Events — records)
│   │   ├── exception/      (ErrorCode enum)
│   │   └── repository/     (Repository interfaces)
│   ├── application/
│   │   ├── command/        (Command DTOs)
│   │   ├── query/          (Query DTOs)
│   │   ├── result/         (Result DTOs)
│   │   ├── usecase/        (Use Cases extending BaseUseCase<I,O>)
│   │   └── port/out/       (Outbound port interfaces)
│   └── adapter/
│       ├── in/
│       │   ├── web/
│       │   │   ├── controller/     (REST Controllers)
│       │   │   ├── request/        (Web Request DTOs)
│       │   │   └── response/       (Web Response DTOs — if different from result)
│       │   └── messaging/          (Event Listeners / Saga Handlers)
│       └── out/
│           ├── entity/             (JPA Entities)
│           ├── mapper/             (Domain ↔ JPA Mappers)
│           ├── repository/         (Spring Data JPA interfaces + Adapters)
│           ├── query/              (Query-side adapters)
│           └── external/           (External API adapters — Anchor, Paystack, etc.)
```
