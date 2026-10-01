# AtlasHub

AtlasHub is an **API-first, headless business operating system** for Nigerian businesses — combining Commerce, Payments, HR, Accounting, and Logistics in a single unified API.

## Pillars

- **Platform**: Identity (`auth`), organization management (`accounts`), access control (`iam`), compliance (`compliance`), subscriptions (`billing`), notifications, and admin tools.
- **Pay**: Virtual accounts, double-entry ledger, card charges, bank transfers, mandates, transaction history, webhooks, and settlement.
- **Commerce**: POS logic, online storefront, inventory management, customer management.
- **HR**: Employee management, payroll, leave, attendance, and loans. *(HR is always free — no subscription required.)*
- **Accounting**: General ledger, accounts payable/receivable, financial reports.
- **Logistics**: Outbound shipments, inbound GRN, returns, inter-outlet transfers.

## Tech Stack

- Java 25
- Spring Boot 3.5.x
- MySQL 8.x
- Redis
- Kafka
- Anchor (virtual accounts)
- Paystack / Moniepoint (payment processing)

## Architecture

AtlasHub follows Clean Architecture + Domain-Driven Design (DDD) + CQRS in a modular monolith. Each module is an independent Gradle subproject with strict dependency direction: `Presentation → Application → Domain ← Infrastructure`.

Each module has:
- `domain/` — Aggregates, value objects, domain events, domain services, repository interfaces, exceptions
- `application/commands/` and `application/queries/` — CQRS handlers
- `infrastructure/` — Persistence (JPA), Kafka listeners, external service adapters
- `presentation/` — REST controllers and DTOs

Cross-module communication is **always async via Kafka**. Synchronous cross-module calls are limited to read-only query ports.

## Setup

Please review `application.yml` and `.env.example` for required configuration.
See `docs/design.md` for full architecture documentation.
