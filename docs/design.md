# AtlasHub Platform — System Design Overview

## What Is AtlasHub?

AtlasHub is an **all-in-one business operating platform** for African businesses. It gives organizations a single place to manage their payments, commerce operations, logistics, HR, and accounting — with every module communicating through a shared, event-driven infrastructure.

Unlike point solutions ("just a POS app" or "just an HR tool"), AtlasHub modules are deeply integrated by design. A sale made at a POS terminal automatically updates stock, triggers an accounting entry, and if payment was by card, reconciles to the organization's virtual account — without any manual configuration. At the same time, each module stands completely alone: a business can subscribe to just Atlas Pay without touching Commerce or HR.

AtlasHub is also a **B2B2C platform**. Businesses don't just use AtlasHub for their own operations — they can expose AtlasHub's payment infrastructure to their own end-customers (e.g., a merchant accepting card payments from shoppers through AtlasHub Pay).

---

## Geographic Scope

- **MVP**: Nigeria-first (NGN as primary currency, Anchor/Paystack/Moniepoint as primary infrastructure)
- **Architecture**: Multi-country from day one — all money types are `Money(amount, currency)`, all organizations carry a `country` and `baseCurrency`, all modules are currency-aware
- **Near-future**: Kenya (KES), Ghana (GHS), South Africa (ZAR)

---

## Core Architectural Principles

AtlasHub is a **Modular Monolith** using:
- **Hexagonal Architecture** (Ports and Adapters) — business logic is isolated from infrastructure
- **Domain-Driven Design (DDD)** — strict bounded contexts, aggregate roots, domain events
- **CQRS** — commands mutate state through use cases; queries are served from separate read models
- **Event-Driven Architecture** — all significant state changes publish domain events through a transactional outbox; modules react via Kafka consumers
- **Open Host Service + Published Language** — the only way modules communicate is through stable public port interfaces (for sync reads) or domain events (for async state changes)

No module ever directly calls another module's repository, internal use case, or database table.

---

## The Module Problem — Why We Split `identity`

A common mistake in platform design is creating a "god module" that owns too many concepts. The previous `identity` module owned: User profiles, Organizations, KYC, API keys, custom roles, and memberships — seven distinct bounded contexts in one module.

The correct split (following Stripe's internal architecture principles) is:

| Concept | Module | Why Separate |
|---|---|---|
| Who you are | `accounts` | Pure identity — User + Organization records |
| How you log in | `auth` | Authentication mechanisms — tokens, passwords |
| Whether you're verified | `compliance` | KYC is a regulatory workflow, not an identity fact |
| What you can access | `iam` | Authorization is a separate concern from identity |
| Which platform product you're paying for | `billing` | Billing is a commercial concern |

---

## Platform Architecture

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                              atlashub-platform                                        │
│                                                                                       │
│  ┌──────────┐  ┌──────┐  ┌────────────┐  ┌─────┐  ┌─────────┐  ┌─────────────────┐ │
│  │ accounts │  │ auth │  │ compliance │  │ iam │  │ catalog │  │    billing      │ │
│  └──────────┘  └──────┘  └────────────┘  └─────┘  └─────────┘  └─────────────────┘ │
│                                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                               │
│  │    admin     │  │notifications │  │   support    │                               │
│  └──────────────┘  └──────────────┘  └──────────────┘                               │
└──────────────────────────────────────────────────────────────────────────────────────┘

┌───────────────┐  ┌──────────────────┐  ┌───────────────────┐  ┌──────────────────┐
│  atlashub-pay │  │atlashub-commerce │  │atlashub-logistics │  │   atlashub-hr    │
│               │  │                  │  │                   │  │                  │
│ accounts      │  │ catalog          │  │ shipping          │  │ staff            │
│ ledger        │  │ storefront (POS) │  │ fleet             │  │ payroll          │
│ charges       │  │ marketplace      │  │ warehousing       │  │ leave            │
│ transfers     │  │ inventory        │  │ 3pl               │  │ attendance       │
│ splits        │  │ vendors          │  │ returns           │  │ loans            │
│ mandates      │  │ online           │  │                   │  │                  │
│ settlement    │  │                  │  │                   │  │                  │
│ tx-query      │  │                  │  │                   │  │                  │
└───────────────┘  └──────────────────┘  └───────────────────┘  └──────────────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                         atlashub-accounting                            │
│    gl (General Ledger) │ ap/ar │ assets │ cash │ budget │ reports     │
└───────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                          atlashub-analytics                            │
│    projections (CQRS) │ timeseries (TimescaleDB) │ dashboards          │
└───────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                       atlashub-infrastructure                          │
│    eventbus (Outbox/Inbox) │ audit │ rate-limiter │ file-storage      │
│    webhooks (outbound delivery)                                        │
└───────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                          atlashub-shared                               │
│    Money, AggregateRoot, DomainEvent, BaseUseCase, Repository,        │
│    Exceptions, Value Objects, Shared Ports                             │
└───────────────────────────────────────────────────────────────────────┘
```

---

## Module Directory

| Module | Package | Purpose |
|---|---|---|
| [accounts](modules/accounts-design.md) | `atlashub-platform:accounts` | User and Organization records — who you are |
| [auth](modules/auth-design.md) | `atlashub-platform:auth` | Authentication — JWT, refresh tokens, passwords, HMAC API signing |
| [compliance](modules/compliance-design.md) | `atlashub-platform:compliance` | KYC journey — document verification, compliance status |
| [iam](modules/iam-design.md) | `atlashub-platform:iam` | Authorization — custom roles, permissions, memberships, API keys |
| [catalog](modules/catalog-design.md) | `atlashub-platform:catalog` | Platform product catalog and pricing plans |
| [billing](modules/billing-design.md) | `atlashub-platform:billing` | Subscriptions, invoices, access entitlements |
| [admin](modules/admin-design.md) | `atlashub-platform:admin` | AtlasHub internal admin portal — multi-tier staff access |
| [notifications](modules/notifications-design.md) | `atlashub-platform:notifications` | Email, SMS, push, WhatsApp, in-app WebSocket delivery |
| [support](modules/support-design.md) | `atlashub-platform:support` | Support ticketing for businesses |
| [pay](modules/pay-design.md) | `atlashub-pay` | All money movement — virtual accounts, ledger, charges, payouts, splits |
| [commerce](modules/commerce-design.md) | `atlashub-commerce` | POS, inventory, marketplace, vendors, online storefront |
| [logistics](modules/logistics-design.md) | `atlashub-logistics` | Shipments, fleet, drivers, 3PL integrations, marketplace dispatch |
| [hr](modules/hr-design.md) | `atlashub-hr` | Employees, payroll, leave, attendance, loans |
| [accounting](modules/accounting-design.md) | `atlashub-accounting` | General ledger, journal entries, financial reports |
| [analytics](modules/analytics-design.md) | `atlashub-analytics` | CQRS projections, time-series metrics, dashboards |
| [hotel](modules/hotel-design.md) | `atlashub-hotel` | **Future** — Hotel PMS, room management, front desk |
| [infrastructure](modules/infrastructure-design.md) | `atlashub-infrastructure` | Outbox/Inbox, audit, rate limiting, outbound webhooks |

---

## Cross-Module Communication Rules

**Rule 1 — No direct calls between module internals.**
Module A never calls Module B's repository, entity, or use case class directly.

**Rule 2 — Sync reads use Open Host Service ports.**
If Module B needs to read data owned by Module A synchronously (e.g., commerce checking if an org's compliance is approved), Module A exposes a stable port interface in `atlashub-shared`. Module B injects and calls it. This port is injected by Spring (same JVM), avoiding network calls.

```
// In atlashub-shared — owned by the accounts module
public interface OrganizationQueryPort {
    Optional<OrganizationDto> findById(Long orgId);
    boolean isComplianceApproved(Long orgId);
}

// In atlashub-platform:accounts — implements the port
@Component
public class OrganizationQueryAdapter implements OrganizationQueryPort { ... }

// In atlashub-commerce — injects the port, no import of accounts internals
@Service
public class ProcessPosCheckoutUseCase {
    private final OrganizationQueryPort orgQueryPort;
    ...
}
```

**Rule 3 — State changes use domain events via Kafka.**
If Module A needs to tell Module B that something happened (e.g., payment succeeded, compliance approved), Module A publishes a domain event through the Outbox. Module B consumes it via a Kafka listener. Events are the only mechanism for cross-module state propagation.

**Rule 4 — No shared database tables.**
Every module owns its own tables. Cross-module references are by ID only (e.g., `organizationId: Long`), never by foreign key join.

See [architecture/module-communication.md](architecture/module-communication.md) for the complete guide.

---

## Data Store Stack

| Store | Purpose | Used By |
|---|---|---|
| **PostgreSQL** | Primary OLTP database — all aggregate state, outbox, audit | All modules |
| **Redis** | JWT refresh token store, revocation list, rate-limit counters, distributed locks | Auth, IAM, Infrastructure |
| **Kafka** | Async event bus — event delivery between modules | All modules |
| **Elasticsearch** | Full-text search — products, customers, transactions | Commerce, Pay, Analytics |
| **TimescaleDB** | Time-series analytics — revenue per hour, orders per day | Analytics |

---

## The Organization Journey (End-to-End)

### 1. Onboarding
1. Business owner registers → `accounts` creates `User` + `Organization`.
2. `UserCreated` event → `auth` creates `AuthAccountJpa`, sends welcome email via `notifications`.
3. `OrganizationCreated` event → `iam` creates default member record (OWNER), `billing` initializes subscription state.
4. Owner logs in, submits 5-step KYC form → `compliance` module tracks the journey.
5. Submitted → `compliance` publishes `ComplianceSubmittedEvent` → `admin` creates KYC review task.
6. Admin approves → `OrganizationComplianceApprovedEvent` → `pay:accounts` issues live NUBAN.
7. Owner subscribes to Atlas Pay + Atlas Commerce → `billing` generates invoices, `iam` grants entitlements.

### 2. Daily Commerce Operations
1. Manager opens the till → `OpenTillUseCase` posts opening float to ledger.
2. Cashier processes a sale → `SalesOrder` created → stock reserved in `inventory`.
3. Customer pays by card → `pay` initializes Paystack checkout.
4. Paystack webhook → `pay` confirms payment → `PaymentSuccessfulEvent`.
5. `commerce` completes sale, deducts stock → `PosSaleCompletedEvent`.
6. `accounting` posts journal entry. `notifications` sends receipt. `analytics` updates sales projection.

### 3. Monthly Payroll
1. HR initiates payroll run → calculates all payslips → `PayrollRun` in DRAFT.
2. Manager (maker) submits for approval.
3. Owner/Director (checker) approves → `PayrollApprovedEvent`.
4. `pay` executes bulk payouts to employee bank accounts.
5. `BulkPayoutCompletedEvent` → HR marks DISBURSED → `accounting` posts final journal → `notifications` SMSes each employee.

### 4. Logistics Delivery (B2B2C)
1. Customer places order via merchant's app → merchant calls AtlasHub Pay API.
2. Payment confirmed → `commerce` creates delivery request → `logistics` creates `Shipment`.
3. Nearest rider assigned (manual in MVP) → `ShipmentAssignedEvent` → rider notified.
4. Rider delivers, submits POD photo → `ShipmentDeliveredEvent` → merchant's webhook called → merchant's server confirmed.

---

## Two Ledgers — One Source of Truth

| | `atlashub-pay:ledger` | `atlashub-accounting:gl` |
|---|---|---|
| **Purpose** | Real-time cash tracking | Financial reporting |
| **Model** | Double-entry, balance snapshots | Journal entries, Chart of Accounts |
| **Granularity** | Every money movement | Business-event level |
| **Users** | Treasury / cash management | Finance / accounting team |
| **Drives** | Wallet balances, available funds | P&L, Balance Sheet, Cash Flow |

---

## Security Model

| Layer | Mechanism |
|---|---|
| Human auth | JWT (15-min access token) + refresh token (Redis-backed, revocable) |
| B2B machine auth | API Key + HMAC-SHA256 request signing (replay-proof via timestamp + nonce) |
| Authorization | Custom RBAC — permission claims embedded in JWT, enforced per endpoint |
| Data isolation | `organization_id` on every table + PostgreSQL Row-Level Security as defense-in-depth |
| Financial controls | Maker-Checker on payroll disbursement, fund transfers > threshold, manual journal entries |
| Rate limiting | IP-based + org-level sliding window (Redis) |
| Webhook integrity | HMAC-SHA256 on all outbound webhook payloads |

---

## External Integration Points

| Provider | Module | What It Does |
|---|---|---|
| **Anchor** | `pay:accounts` | Issues real NUBAN bank accounts. Webhooks notify of incoming transfers. |
| **Paystack** | `pay:charges`, `pay:transfers` | Card/USSD/bank transfer collection. Payout to bank accounts. |
| **Moniepoint** | `pay:charges` | Physical POS terminal transactions. |
| **SendGrid / AWS SES** | `notifications` | Transactional email. |
| **Termii / Twilio** | `notifications` | SMS delivery. |
| **Firebase FCM** | `notifications` | In-app push notifications. |
| **WhatsApp Business API** | `notifications` | Transactional WhatsApp messages. |
| **DHL / GIG Logistics** | `logistics:3pl` | Third-party logistics carrier integration. |
| **Redis** | `infrastructure` | Sessions, rate limiting, revocation. |
| **Kafka** | `infrastructure:eventbus` | Async event delivery between modules. |
| **Elasticsearch** | `commerce`, `pay`, `analytics` | Product, customer, transaction search. |
| **TimescaleDB** | `analytics` | Time-series metrics. |

---

## Folder Structure Convention

```
{module}/
├── src/main/java/com/atlashub/{module-name}/
│   ├── domain/
│   │   ├── model/          ← Aggregates, Entities
│   │   ├── valueobject/    ← Enums, Value Objects
│   │   ├── event/          ← Domain Events (records, past-tense names)
│   │   ├── exception/      ← ErrorCode enum
│   │   └── repository/     ← Repository interfaces (no Spring imports)
│   ├── application/
│   │   ├── command/        ← Write input DTOs (suffixed Command)
│   │   ├── query/          ← Read input DTOs (suffixed Query)
│   │   ├── result/         ← Output DTOs from use cases
│   │   ├── usecase/        ← Use case implementations (extend BaseUseCase)
│   │   └── port/
│   │       ├── in/         ← Inbound ports (Open Host Service contracts)
│   │       └── out/        ← Outbound ports (external API interfaces)
│   └── adapter/
│       ├── in/
│       │   ├── web/
│       │   │   ├── controller/     ← REST Controllers
│       │   │   ├── request/        ← Web Request DTOs
│       │   │   └── response/       ← Web Response DTOs
│       │   └── messaging/          ← Kafka Listeners
│       └── out/
│           ├── persistence/
│           │   ├── entity/         ← JPA Entities
│           │   ├── mapper/         ← Domain ↔ Entity Mappers
│           │   └── repository/     ← Spring Data JPA + Repository Adapters
│           ├── external/           ← External API adapters
│           └── catalog/            ← Cross-module query adapters
```
