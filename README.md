# AtlasHub Backend — Unified Business Operating System

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-25-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-blue.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-DDD%20%7C%20CQRS%20%7C%20Clean-success.svg)]()
[![Live Dashboard](https://img.shields.io/badge/Dashboard-Live-success.svg)](https://dashboard.atlashub.name.ng)

> **AtlasHub** is an API-first, headless business operating platform engineered for African enterprises, scaling SMEs, and digital merchants. It unifies **Omnichannel Commerce (POS & Storefront)**, **Embedded Banking & Payments**, **Multi-Outlet Inventory**, and **Double-Entry General Ledger Accounting** into a single cohesive, event-driven modular monolith.

- 🌐 **Live Web Dashboard**: [https://dashboard.atlashub.name.ng](https://dashboard.atlashub.name.ng)
- 📖 **API Documentation (OpenAPI / Swagger)**: `http://localhost:8080/swagger-ui/index.html`

---

## 🚀 The Problem & The AtlasHub Solution

### The Challenge Facing African Businesses
Today, running an ambitious retail or service enterprise in Africa requires juggling 5 to 8 fragmented, disconnected tools:
* A standalone POS app for counter checkout.
* Spreadsheets or disjointed warehouse tools for stock tracking.
* Multiple commercial banking apps to monitor customer bank transfers manually.
* Payment gateway dashboards for online card charges.
* Disconnected accounting software requiring end-of-month manual reconciliation.

When data lives in silos, transactions go missing, stock counts drift, reconciliation requires days of painful manual labor, and management has zero real-time visibility into financial health.

### The AtlasHub Solution
AtlasHub eliminates operational silos through **deep architectural integration**:
* **Every transaction ripples automatically across the system**: When a cashier completes a sale on the POS terminal:
  1. The item stock is instantly decremented with reservation guards.
  2. The payment is processed (or customer credit balance updated).
  3. A double-entry transaction is written to the financial ledger.
  4. An automated journal entry is posted to the General Ledger.
  5. An immutable audit log entry is recorded.
* **Modular by Design**: Businesses can adopt the entire suite or subscribe independently to specific modules (e.g. Atlas Pay only, or Commerce only) without coupling.

---

## 🎯 Target Users & Who We Serve

AtlasHub is built specifically for:

1. **Multi-Outlet Retailers & Supermarkets**
   - Chains managing multiple branches, centralized stock warehouses, and frontline cashiers.
   - Require till management, fast barcode/variant POS checkout, cash-in/cash-out tracking, and inter-outlet stock transfers.

2. **Hospitality & Quick-Service Restaurants (QSR)**
   - Businesses managing physical table reservations, Kitchen Order Tickets (KOT), and split or credit bills.

3. **Digital Platforms & Marketplaces (B2B2C)**
   - Tech-enabled startups that need programmatic embedded banking: issuing dedicated virtual NUBAN accounts to their customers, processing online card charges, and automatically splitting payouts to sub-merchants.

4. **Finance Teams, CFOs & Accountants**
   - Controllers demanding mathematically enforced double-entry accounting ($\sum \text{Debits} = \sum \text{Credits}$), Maker-Checker approval controls on journal entries, and automated trial balances.

5. **Developers & Independent Software Vendors (ISVs)**
   - Teams seeking headless REST APIs to power custom mobile checkout apps, ecommerce storefronts, or ERP integrations.

---

## 💼 What Businesses Can Do With AtlasHub

### 1. Omnichannel Commerce & Point-of-Sale (POS)
* **High-Speed POS Checkout**: Process orders in physical stores with card, bank transfer, cash, or store credit payments.
* **Cash Till Lifecycle**: Open tills with initial cash floats, record cash-in/cash-out adjustments, and enforce end-of-day reconciliation before till closure.
* **Hospitality Table & Kitchen Management**: Create and track dining tables, issue Kitchen Order Tickets (KOT) with automated status updates (`SENT_TO_KITCHEN`, `READY`, `SERVED`).
* **Store Credit & Customer Deposits**: Accept upfront customer advance deposits, issue store credit, and track running credit limits safely.

### 2. Multi-Outlet Inventory & Warehousing
* **Real-Time Stock Reservations**: Reserve stock during active checkouts to eliminate overselling across simultaneous online and physical purchases.
* **Stock Adjustments with Reason Codes**: Record damaged items, expired goods, or shrinkage with full supervisor audit trails.
* **Inter-Outlet Stock Transfers**: Dispatch stock between regional branches, complete with `DISPATCHED` and `RECEIVED` verification workflows.
* **Periodic Stock Counts**: Carry out discrepancy auditing and variance reconciliations.

### 3. Embedded Banking & Payments (Atlas Pay)
* **Dedicated Virtual NUBAN Accounts**: Generate programmatic deposit accounts (via Anchor integration) for instant bank transfer collections with zero manual confirmation.
* **Card & Online Charge Processing**: Initiate and verify card transactions via Paystack integration with webhook validation and signature authentication.
* **Split Settlements & Automated Commission**: Configure flexible split rules (percentage or fixed fees) across vendor and parent bank accounts upon checkout.
* **Double-Entry Transaction Ledger**: Maintain immutable financial balances with running calculation engines for business operating and settlement accounts.

### 4. General Ledger Accounting (GL)
* **Standard Chart of Accounts**: Structured hierarchical accounts across 5 fundamental types (`ASSET`, `LIABILITY`, `EQUITY`, `REVENUE`, `EXPENSE`).
* **Enforced Double-Entry Invariants**: Balanced journal entries where debits strictly equal credits.
* **Maker-Checker Governance**: Sensitive manual adjustments over configured thresholds require a separate supervisor approval; self-approval is rejected at the domain layer.
* **Balance Snapshotting**: Point-in-time financial snapshots for historical balance verification and reporting.

### 5. Enterprise Identity, Access & Compliance
* **Multi-Tenant Organizations**: Hierarchical organizational structures supporting multiple outlets and staff permissions.
* **Robust Authentication**: RSA-256 signed JWTs with refresh token rotation, device fingerprinting, and OTP verification via email.
* **Granular Role-Based Access Control (RBAC)**: Fine-grained permissions per resource (`commerce:orders:create`, `pay:ledger:read`, `gl:entry:approve`, etc.).
* **Regulatory Compliance & KYC**: Automated onboarding workflows with business document verification (CAC, director IDs, utility bills) and automated verification tier escalation.
* **Tamper-Evident Audit Logging**: Centralized activity stream recording every critical actor action, IP address, and payload.

---

## 🏗️ Architectural Foundations

AtlasHub follows **Clean Architecture**, **Domain-Driven Design (DDD)**, and **CQRS** principles organized as a **Modular Monolith**:

```
Presentation (REST Controllers, DTOs)
      ↓
Application (Command & Query Handlers, Ports)
      ↓
Domain (Aggregate Roots, Entities, Value Objects, Domain Events)
      ↑
Infrastructure (JPA Repositories, Kafka Listeners, External Adapters)
```

### Key Architectural Tenets
* **Framework-Free Domain**: Aggregates, value objects, domain services, and repository interfaces contain zero framework annotations (`@Component`, `@Entity`, etc.).
* **Asynchronous Cross-Module Side-Effects**: Modules communicate writes purely through domain events written to an outbox and published to Apache Kafka.
* **Synchronous Cross-Module Reads**: Reads across bounded contexts use dedicated query port contracts defined in `atlashub-shared`. No module ever injects another module's repository or database entity.
* **Automatic Event Dispatch**: Domain events registered via `registerEvent(...)` are persisted and published automatically on repository `save()` through `JpaBaseRepository`.

---

## 📦 Project Structure

```
atlashub-backend/
├── atlashub-shared/                    ← Core abstractions, base types, Money VO, query ports
├── atlashub-platform/
│   ├── authentication/                ← Identity, session management, RSA JWT signing, OTP
│   ├── accounts/                      ← Users, organizations, outlets
│   ├── iam/                           ← Roles, permissions, memberships, API keys
│   ├── compliance/                    ← Business KYC, document verification, AML status
│   ├── notifications/                 ← Email transmission, delivery tracking
│   └── storage/                       ← Document and asset uploads (Firebase Storage)
├── atlashub-pay/
│   ├── accounts/                      ← Dedicated business deposit & sub-accounts (Anchor)
│   ├── charges/                       ← Card & direct charge orchestration (Paystack)
│   ├── ledger/                        ← Real-time internal financial accounts & entries
│   ├── settlement/                    ← Settlement polling, credit evidence, reconciliation
│   ├── splits/                        ← Split rules and automated revenue sharing
│   └── tx-query/                      ← Transaction reporting and search queries
├── atlashub-commerce/
│   ├── catalog/                       ← Products, variants, prices, categories
│   ├── inventory/                     ← Real-time stock counts, reservations, adjustments
│   └── storefront/                    ← Orders, POS checkout, cash tills, tables, store credit
├── atlashub-accounting/
│   └── gl/                            ← Chart of accounts, double-entry journal entries, maker-checker
├── atlashub-infrastructure/
│   ├── anchor/                        ← Anchor Banking API integration adapter
│   ├── paystack/                      ← Paystack Payments API integration adapter
│   ├── eventbus/                      ← Transactional Outbox pattern & Kafka messaging
│   ├── rate-limiter/                  ← Redis token-bucket rate limiting
│   └── audit/                         ← Centralized activity logging
└── atlashub-main/                     ← Spring Boot entrypoint, security filter chain, application config
```

---

## 🛠️ Technology Stack

| Category | Technology |
|---|---|
| **Language & Runtime** | Java 25 (OpenJDK 25) |
| **Framework** | Spring Boot 3.5.4 |
| **Persistence** | Spring Data JPA, Hibernate ORM 6.6, MySQL 8.0 |
| **Caching & Invalidation** | Redis 7.x (Lettuce client) |
| **Event Streaming** | Apache Kafka, Transactional Outbox Pattern |
| **Security & Cryptography** | Spring Security 6, RSA-256 JWT (`jjwt`), BCrypt |
| **Code Generation** | MapStruct 1.5.5, Lombok |
| **API Documentation** | SpringDoc OpenAPI 2.8.4 (Swagger UI) |
| **Third-Party Providers** | Anchor (Banking/NUBANs), Paystack (Payments), Resend (Transactional Email), Firebase (Storage) |
| **DevOps & Cloud** | Docker, K3s (Kubernetes), Terraform, Azure Cloud |

---

## ⚡ Quickstart & Local Setup

### Prerequisites
* **Java 25** (JDK 25 installed and configured on your `PATH`)
* **MySQL 8.x** running locally on port `3306`
* **Redis** running locally on port `6379`
* **Gradle 9.x** (or use the included `./gradlew`)

### 1. Clone the Repository
```bash
git clone https://github.com/OlamidotunIY/atlashub_backend.git
cd atlashub_backend
```

### 2. Configure Environment Variables
Copy `.env.example` or create a `.env` file in the project root:
```properties
# Database
ATLASHUB_DB_URL=jdbc:mysql://localhost:3306/atlashub?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
ATLASHUB_DB_USERNAME=root
ATLASHUB_DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Keys (leave blank for auto-generated ephemeral keys in local profile)
ATLASHUB_JWT_PRIVATE_KEY_PATH=infrastructure/JWT/atlashub-jwt-private.pem
ATLASHUB_JWT_PUBLIC_KEY_PATH=infrastructure/JWT/atlashub-jwt-public.pem
ATLASHUB_JWT_KEY_ID=atlashub-rs256-1

# External Providers (Sandbox / Test)
ATLASHUB_PAYSTACK_ENABLED=false
ATLASHUB_ANCHOR_ENABLED=false

# Frontend CORS
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,https://dashboard.atlashub.name.ng
```

### 3. Build & Run
Run the application using the `local` Spring profile:
```bash
# Build all subprojects
./gradlew build -x test

# Launch the application
./gradlew :atlashub-main:bootRun --args='--spring.profiles.active=local'
```

### 4. Verify API
Once the application boots:
* **Interactive OpenAPI (Swagger UI)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* **Actuator Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
* **Frontend Web Dashboard**: [https://dashboard.atlashub.name.ng](https://dashboard.atlashub.name.ng)

### 5. Running Tests
```bash
# Run unit tests across all modules
./gradlew test

# Run tests for a specific module
./gradlew :atlashub-accounting:gl:test
./gradlew :atlashub-commerce:storefront:test
```

---

## 🔒 Security & Governance

* **Zero Direct Database Cross-Access**: Modular isolation guarantees that no business domain can compromise or corrupt another module's state.
* **Maker-Checker Financial Controls**: High-value journal adjustments cannot be self-approved.
* **Auditability**: Every write operation records the initiator's principal context (`userId`, `organizationId`, IP address, timestamp).
* **Stateless Sessions**: JWT tokens with short lifetimes paired with cryptographic revocation tracking in Redis.

---

## 📄 License & Ownership

Copyright © 2026 AtlasHub Technologies. All rights reserved.
For enterprise licensing, partner integration, or API support, visit [dashboard.atlashub.name.ng](https://dashboard.atlashub.name.ng).
