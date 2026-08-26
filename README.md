# AtlasHub

AtlasHub is a unified product hub for **Payments**, **Commerce**, and **Logistics**.

## Pillars

- **Platform**: Core identity, authentication, and the Hub catalog where organizations manage their product subscriptions.
- **Pay**: The payment engine handling virtual accounts, double-entry ledger, transfers, charges, and settlement.
- **Commerce**: Digital storefronts, product inventory, and order orchestration.
- **Logistics**: Shipping and fulfillment integrations.

## Tech Stack
- Java 25
- Spring Boot 3.5.x
- MySQL 8.x
- Redis
- Kafka

## Architecture
AtlasHub follows a Hexagonal Architecture (Ports and Adapters) combined with Domain-Driven Design (DDD) and CQRS. The project is structured as a Gradle multi-project build with nested subprojects representing the pillars.

Each module adheres to the following structure:
- `domain/` - Core aggregates, value objects, domain events, domain services, repository interfaces, and exceptions.
- `application/` - Use cases (extending `BaseUseCase`), commands, queries, DTOs, and outbound ports.
- `adapter/in/` - Web controllers, webhook receivers, and messaging consumers.
- `adapter/out/` - Persistence (JPA entities, Spring Data repositories) and external service adapters.

## Setup
Please review `application.yml` and `.env.example` for required configuration.
