> **TARGET ARCHITECTURE NOTICE — 2026-10-25 (not yet implemented):** Atlas Hub now separates global **User registration** from optional **Organization creation**, retires mandatory Atlas-owned Anchor banking and five-step KYB approval, and uses **merchant-connected payment/logistics providers** with capability-specific provider verification. User with zero organizations is valid; no payment provider is required for identity, organization, HR, catalog, inventory, accounting or theme-creator signup. Auth UI lives in [atlashub_auth_web](https://github.com/OlamidotunIY/atlashub_auth_web), the dedicated theme portal in [atlashub_storefront_portal](https://github.com/OlamidotunIY/atlashub_storefront_portal), and storefront API/hosting in [atlashub_storefront](https://github.com/OlamidotunIY/atlashub_storefront). **Compliance is not globally eliminated:** provider-specific KYC/KYB and privacy, tax, fraud or jurisdictional requirements remain where applicable. Current Java code and legacy text below still reflect the old Anchor programme; do not mistake them for the target. Full [identity separation](architecture/identity-organization-separation.md), [provider migration](architecture/provider-banking-decommission.md), [integration contract](architecture/global-provider-integrations-proposal.md).

# AtlasHub Backend — Documentation

AtlasHub is a B2B multi-product platform combining payments, commerce, logistics, HR, and accounting into a single integrated product suite. Organizations subscribe **once** and gain access to all products — no per-product registration required. Some products are free and always included. When an organization registers, the platform automatically provisions entitlements across every active product.

---

## Architecture Overview

- **Pattern**: Modular Monolith — Hexagonal Architecture (Ports & Adapters) + DDD + CQRS
- **Language**: Java 21 (Records, Pattern Matching, Virtual Threads)
- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8 (OLTP, event projections, analytics feature snapshots, and audit data)
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
│   ├── compliance/           ← LEGACY mandatory Anchor banking KYB (retire after migration)
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
├── atlashub-analytics/       ← CQRS projections + MySQL feature snapshots and dashboards
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
| `compliance` (LEGACY, decommission target) | [compliance-design.md](./modules/compliance-design.md) | Historical Anchor five-step KYB, **not required** for user/org signup or general Atlas Hub operations |
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
| `analytics` | [analytics-design.md](./modules/analytics-design.md) | MySQL CQRS projections, feature snapshots and dashboards |
| `intelligence` | [intelligence-design.md](./modules/intelligence-design.md) | Grounded conversational AI, ML forecasting, recommendations and traceability |
| `financing` | [financing-design.md](./modules/financing-design.md) | AtlasScore, consent, lender applications/offers, facility monitoring |
| `pay:controls` | [pay-controls-design.md](./modules/pay-controls-design.md) | Capability-aware hold/block/freeze enforcement for financed facilities |
| `hotel` *(future)* | [hotel-design.md](./modules/hotel-design.md) | PMS — NOT in MVP |

---

## Architecture Docs

| Doc | What It Covers |
|---|---|
| [module-communication.md](./architecture/module-communication.md) | Open Host Service (sync), Published Language (async events), Anti-Corruption Layer |
| [multi-tenancy.md](./architecture/multi-tenancy.md) | Application-enforced row-level tenancy, tenant context, cross-tenant analytics |
| [multi-currency.md](./architecture/multi-currency.md) | `Money` value object, `Currency` enum, org base currency, FX entries |
| [maker-checker.md](./architecture/maker-checker.md) | Four-eyes principle, domain-level enforcement, per-org thresholds |
| [sagas-design.md](./architecture/sagas-design.md) | Choreography vs orchestration, 5 concrete sagas, compensation, timeout |
| [atlas-intelligence-financing-delivery-plan.md](./architecture/atlas-intelligence-financing-delivery-plan.md) | MySQL-based delivery plan, build priorities and product boundaries |

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
| [setup/api-key-hmac-auth.md](./setup/api-key-hmac-auth.md) | HMAC signing algorithm, verification, key rotation |
| [setup/webhook-infrastructure.md](./setup/webhook-infrastructure.md) | Outbound webhooks, delivery, retry, merchant verification |
| [setup/anchor-integration.md](./setup/anchor-integration.md) | **Legacy Atlas-owned banking**; retire/migrate before removing |
| [setup/paystack-integration.md](./setup/paystack-integration.md) | Card/USSD checkout, refunds, settlement routing and reconciliation |
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
| Accounts, Auth, IAM | Target: independent user/org registration, central SSO and IAM; **not yet migrated** |
| Catalog, Billing | ✅ MVP |
| Atlas Pay | ✅ MVP |
| Commerce (POS + Marketplace) | ✅ MVP |
| Logistics (Internal Fleet + 3PL + Consolidation + Returns) | ✅ MVP |
| HR & Payroll | ✅ MVP |
| Accounting | ✅ MVP |
| Analytics | ✅ MVP |
| Notifications, Support, Admin | ✅ MVP |
| Hotel | 🚫 Future (vision doc only) |
| Driver Network / Auto-Dispatch | 🚫 Near-future |
| OTA Channel Manager | 🚫 Future |


---

## Architecture modernization: proposed Storefront and AI integration (2026-10-25)

The module documentation above records existing/legacy assumptions and can differ from future target architecture. The new Storefront target decisions, not yet implemented, are:
- [Storefront React platform proposal](./architecture/storefront-theme-platform-proposal.md) — Vite/React Router scaffold, full React templates, Portal for merchant and creator, SDK/CLI, isolated Cloudflare Workers for Platforms, R2 and CDN.
- [Java storefront business/identity/API boundary](./architecture/storefront-platform-integration.md) — Java-owned identity, merchant data, licensing and trusted checkout vs TypeScript storefront portal/management backend. **No fourth backend repository.**
- [Global multi-provider integrations](./architecture/global-provider-integrations-proposal.md), [AI voice/command-center architecture](./architecture/ai-command-center-voice-security-proposal.md) and [global delivery plan](./architecture/global-expansion-delivery-plan.md).

The dedicated [atlashub_storefront repository](https://github.com/OlamidotunIY/atlashub_storefront) is **already created** and contains detailed architecture, module, contract, security, operations and decision documents. Independent creators publish reviewed React themes there. Businesses follow Atlas Web recommendations to the separate Storefront Portal, using planned central Atlas Hub SSO.


## Cross-repository clean architecture standards (2026-10-26)

The independent Atlas Hub repositories adopt the Java backend's **DDD + Clean Architecture + Hexagonal/Ports-and-Adapters** principles with *language- and product-specific folder layouts*. Do not duplicate Java's business aggregates or force CQRS for trivial UI/stream operations. Each repository is built/released independently; **no Turborepo/Nx**.

- [Auth Web frontend: Clean/Hexagonal rules](https://github.com/OlamidotunIY/atlashub_auth_web/blob/main/docs/architecture/clean-hexagonal.md) — central identity UI, not auth backend.
- [Storefront Portal frontend: Clean/Hexagonal rules](https://github.com/OlamidotunIY/atlashub_storefront_portal/blob/main/docs/architecture/clean-hexagonal.md) — merchant and creator UI.
- [Storefront backend: Clean/Hexagonal rules](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/clean-hexagonal.md) — theme domain, publishing, storage and Workers.
- [Theme SDK: Clean/Hexagonal rules](https://github.com/OlamidotunIY/atlashub_theme_sdk/blob/main/docs/architecture/clean-hexagonal.md) — public API, scaffold, CLI and versioned contracts; repository now exists but code is not built.
- [Atlas Intelligence: Clean/Hexagonal rules](https://github.com/OlamidotunIY/atlashub_intelligence/blob/main/docs/architecture/clean-hexagonal.md) — Python tool orchestration, local models and voice risk signals.

Each new repo also maintains `docs/architecture/folder-structure.md`, `docs/guidelines/engineering-rules.md`, `docs/guidelines/testing-release.md`, a module plan, and a PR architecture checklist. Java remains authoritative for user/organization registration, IAM, business operations, merchant-connected provider eligibility and financial/checkout state.


## Shared canonical contracts: Protobuf and gRPC (2026-10-26)

The now-created [atlashub_contracts](https://github.com/OlamidotunIY/atlashub_contracts) repository is the **single schema authority** for shared wire protocols and events. The Java Spring backend maps generated Java Protobuf messages and native gRPC endpoints to existing DDD/CQRS application use cases; **generated message types do not become domain entities**. Python Intelligence consumes private native gRPC tool contracts. Storefront/public browser SDK consumes only approved public contracts through a compatible **Connect/gRPC-Web gateway** (a native Java gRPC server alone is not browser-compatible). Central OIDC authentication is separate from RPC, User registration does not require Organization, and optional merchant provider KYC is not a global banking gate.

Current contract versions are **draft v1alpha1**; package generation/registry, native gRPC controllers, browser gateway and consumer integration are not implemented. Details: [contract ownership](https://github.com/OlamidotunIY/atlashub_contracts/blob/main/docs/modules/index.md), [API security](https://github.com/OlamidotunIY/atlashub_contracts/blob/main/docs/architecture/transports-security.md), [generated package plan](https://github.com/OlamidotunIY/atlashub_contracts/blob/main/docs/architecture/generated-packages.md).
