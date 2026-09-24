# AtlasHub Backend — Documentation

AtlasHub is a B2B multi-product platform combining payments, commerce, logistics, HR, and accounting into a single integrated product suite. Businesses register for the products they need; each product can stand alone or work with others.

---

## Architecture Overview

- **Pattern**: Modular Monolith — Hexagonal Architecture (Ports & Adapters) + DDD + CQRS
- **Language**: Java 21 (Records, Pattern Matching, Virtual Threads)
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL 16 (OLTP) + TimescaleDB (analytics time-series)
- **Cache**: Redis 7
- **Search**: Elasticsearch 8
- **Messaging**: Apache Kafka (event bus via Transactional Outbox)
- **Real-time**: Spring WebSocket + STOMP

→ Full architecture: [design.md](./design.md)

---

## Module Structure

```
atlashub-backend/
├── atlashub-shared/          ← Shared kernel: base classes, ports, value objects, Money
├── atlashub-infrastructure/  ← Cross-cutting infra: outbox, inbox, sequences, JWT, WebSocket
│
├── atlashub-platform/
│   ├── accounts/             ← User and Organization registration
│   ├── auth/                 ← JWT, sessions, API key HMAC verification
│   ├── compliance/           ← KYC 5-step journey, document verification
│   ├── iam/                  ← Custom roles, permissions, memberships, invitations, API keys
│   ├── catalog/              ← Platform product catalogue and pricing plans
│   ├── billing/              ← Subscriptions, invoices, entitlements
│   ├── admin/                ← Internal AtlasHub staff portal (KYC review, org management)
│   ├── notifications/        ← Email, SMS, Push, WhatsApp, WebSocket delivery
│   └── support/              ← Customer support ticketing
│
├── atlashub-pay/             ← Virtual accounts, ledger, charges, payouts, splits, webhooks
├── atlashub-commerce/        ← POS, inventory, marketplace/vendors, online storefront
├── atlashub-logistics/       ← Fleet, dispatch, 3PL, stock transfers, returns
├── atlashub-hr/              ← Employees, payroll (maker-checker), leave, loans, attendance
├── atlashub-accounting/      ← General ledger, journal entries (maker-checker), AP/AR, assets
├── atlashub-analytics/       ← CQRS projections + TimescaleDB time-series dashboards
│
└── atlashub-bootstrap/       ← Spring Boot application entry point, module wiring
```

---

## Module Design Docs

### Platform Modules

| Module | Doc | Purpose |
|---|---|---|
| `accounts` | [accounts-design.md](./modules/accounts-design.md) | User & Organization registration |
| `auth` | [auth-design.md](./modules/auth-design.md) | JWT, sessions, HMAC signing |
| `compliance` | [compliance-design.md](./modules/compliance-design.md) | KYC 5-step journey |
| `iam` | [iam-design.md](./modules/iam-design.md) | Roles, permissions, memberships, API keys |
| `catalog` | [catalog-design.md](./modules/catalog-design.md) | Platform product catalogue & pricing |
| `billing` | [billing-design.md](./modules/billing-design.md) | Subscriptions, invoices, entitlements |
| `admin` | [admin-design.md](./modules/admin-design.md) | AtlasHub internal staff tools |
| `notifications` | [notifications-design.md](./modules/notifications-design.md) | 5-channel notification delivery |
| `support` | [support-design.md](./modules/support-design.md) | Customer support ticketing |

### Product Modules

| Module | Doc | Purpose |
|---|---|---|
| `pay` | [pay-design.md](./modules/pay-design.md) | Virtual accounts, ledger, charges, payouts, B2B2C |
| `commerce` | [commerce-design.md](./modules/commerce-design.md) | POS, inventory, marketplace, online store |
| `logistics` | [logistics-design.md](./modules/logistics-design.md) | Fleet, dispatch, 3PL, stock transfers |
| `hr` | [hr-design.md](./modules/hr-design.md) | Employees, payroll with maker-checker |
| `accounting` | [accounting-design.md](./modules/accounting-design.md) | Double-entry GL, AP/AR, assets |
| `analytics` | [analytics-design.md](./modules/analytics-design.md) | CQRS projections + time-series dashboards |
| `hotel` *(future)* | [hotel-design.md](./modules/hotel-design.md) | PMS — NOT in MVP |

---

## Architecture Docs

| Doc | What It Covers |
|---|---|
| [module-communication.md](./architecture/module-communication.md) | Open Host Service (sync), Published Language (async events), Anti-Corruption Layer |
| [multi-tenancy.md](./architecture/multi-tenancy.md) | Row-level tenancy + PostgreSQL RLS, ThreadLocal context, cross-tenant analytics |
| [multi-currency.md](./architecture/multi-currency.md) | `Money` value object, `Currency` enum, org base currency, FX entries |
| [maker-checker.md](./architecture/maker-checker.md) | Four-eyes principle, domain-level enforcement, per-org thresholds |
| [sagas-design.md](./architecture/sagas-design.md) | Choreography vs orchestration, 5 concrete sagas, compensation, timeout |

---

## Setup & Integration Docs

| Doc | What It Covers |
|---|---|
| [setup/docker-setup.md](./setup/docker-setup.md) | Local dev Docker Compose — all 8 services |
| [setup/outbox-pattern.md](./setup/outbox-pattern.md) | Transactional Outbox, BaseJpaRepositoryAdapter, Inbox idempotency |
| [setup/kafka-setup.md](./setup/kafka-setup.md) | Topics, partitions, retry/DLQ, schema evolution |
| [setup/redis-setup.md](./setup/redis-setup.md) | Sessions, revocation, rate limiting, API key cache |
| [setup/websocket-setup.md](./setup/websocket-setup.md) | STOMP channels, selective broadcaster, production scaling |
| [setup/elasticsearch-setup.md](./setup/elasticsearch-setup.md) | Index schemas, CDC sync, full-text search |
| [setup/timescaledb-analytics.md](./setup/timescaledb-analytics.md) | Hypertables, continuous aggregates, retention |
| [setup/api-key-hmac-auth.md](./setup/api-key-hmac-auth.md) | HMAC signing algorithm, verification, key rotation |
| [setup/webhook-infrastructure.md](./setup/webhook-infrastructure.md) | Outbound webhooks, delivery, retry, merchant verification |
| [setup/anchor-integration.md](./setup/anchor-integration.md) | NUBAN issuance, inbound transfers |
| [setup/paystack-integration.md](./setup/paystack-integration.md) | Card payments, BVN verification, bank name enquiry |
| [setup/moniepoint-integration.md](./setup/moniepoint-integration.md) | POS terminals, card-present, QR payments |

---

## Key Design Decisions

### 1. Repository Owns Outbox Writing
The `BaseJpaRepositoryAdapter.save()` method pulls domain events from the aggregate and writes them to the outbox table within the same database transaction. Use cases call `repository.save()` only — they are completely unaware of event publishing. See [outbox-pattern.md](./setup/outbox-pattern.md).

### 2. No Direct Module Imports
Modules never import each other's internal classes. Synchronous reads use port interfaces from `atlashub-shared`. Asynchronous state changes use Kafka domain events. See [module-communication.md](./architecture/module-communication.md).

### 3. Maker-Checker at Domain Level
The `approve()` method on PayrollRun, Payout, and JournalEntry aggregates enforces `approverId ≠ initiatedBy` inside the domain object — not just the controller. Even a bypassed auth check cannot allow self-approval. See [maker-checker.md](./architecture/maker-checker.md).

### 4. Selective WebSocket Push
WebSocket is a UI delivery mechanism for events where a human is actively waiting for the outcome. Most domain events are consumed only by Kafka listeners; only ~8 event types trigger WebSocket pushes. See [websocket-setup.md](./setup/websocket-setup.md).

### 5. Money Is Never a Raw Number
Every monetary value uses the `Money` value object (amount + currency). The `Money.assertSameCurrency()` guard makes it impossible to accidentally add NGN and USD at compile time. See [multi-currency.md](./architecture/multi-currency.md).

---

## MVP Scope

| Module | MVP Status |
|---|---|
| Accounts, Auth, Compliance, IAM | ✅ MVP |
| Catalog, Billing | ✅ MVP |
| Atlas Pay | ✅ MVP |
| Commerce (POS + Marketplace) | ✅ MVP |
| Logistics (Internal Fleet + LaaS) | ✅ MVP |
| HR & Payroll | ✅ MVP |
| Accounting | ✅ MVP |
| Analytics | ✅ MVP |
| Notifications, Support, Admin | ✅ MVP |
| Hotel | 🚫 Future (vision doc only) |
| Driver Network / Auto-Dispatch | 🚫 Near-future |
| OTA Channel Manager | 🚫 Future |
