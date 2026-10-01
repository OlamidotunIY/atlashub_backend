# AtlasHub — Product Requirements Document (PRD)

> **Status**: ✅ LOCKED  
> **Last updated**: 2026-09-30  
> **Version**: 1.0

---

## 1. Product Overview

**AtlasHub** is an API-first, headless business operating system for Nigerian businesses.

Businesses subscribe to AtlasHub and integrate its REST APIs and SDKs to power any commerce application they need — a physical POS app, an online storefront, a hotel booking system, a restaurant ordering system — without having to stitch together separate platforms like Medusa (commerce), ERPNext (ERP/HR/accounting), Stripe (payments), and Gusto (payroll).

AtlasHub handles **everything on the backend**:
- Commerce & POS logic
- Payments (collection and disbursement)
- Inventory management
- Logistics & delivery
- HR & payroll
- Accounting
- Customer management

Businesses interact with AtlasHub in two ways:
1. **API / SDK** — their developers integrate AtlasHub to power their consumer-facing apps (storefronts, POS terminals, hotel frontends, etc.)
2. **Admin Dashboard** — their staff manage configuration, view analytics, manage team, and monitor operations — like a Stripe dashboard

AtlasHub does **not** build or expose consumer-facing UIs. That is the business's responsibility. AtlasHub may offer custom frontend development as a paid professional service.

---

## 2. Problem Statement

Today, a Nigerian store owner (retail, restaurant, hotel) who wants a modern digital system must integrate 4–6 separate platforms and write compensation logic to make them talk to each other:

| What they need | Current solution |
|---|---|
| Online store / POS | Shopify, WooCommerce, StoreApp |
| Payments | Paystack, Flutterwave |
| Accounting | QuickBooks, Sage, Excel |
| HR & Payroll | BambooHR, Sage HR, Excel |
| Inventory | Separate tool or spreadsheet |
| Logistics | DHL, GIG, manual |

The result: data silos, manual reconciliation, expensive developer time, and no single source of truth.

**AtlasHub removes all of this.** One subscription, one API, one dashboard, everything connected by design.

---

## 3. Target Users

### 3.1 Primary Customer — The Business (Subscriber)

Any Nigerian business that runs a store, restaurant, hotel, or similar operation and needs digital operations management.

**Size**: 1–200 employees. Targeting small-to-medium businesses first.
**Geography**: Nigeria (Phase 1). Africa in future phases.
**Examples**: A fashion retail chain with 3 branches, a restaurant with physical + online ordering, a hotel group with 2 properties.

### 3.2 Secondary User — The Business's Developer

A developer employed by or contracted to the subscriber. They:
- Integrate AtlasHub REST APIs into the business's consumer-facing app
- Use API reference docs and SDKs
- Manage API keys, webhook endpoints, and test/live mode from the dashboard

### 3.3 Tertiary User — The Business's Staff

Employees who use the **AtlasHub admin dashboard** directly:
- **Owners / Admins** — configure account, manage subscription, view reports
- **HR managers** — onboard employees, run payroll, approve leave
- **Finance managers** — view accounting, approve transfers
- **Store managers** — manage outlets, view inventory, review POS reports

### 3.4 NOT a User

End-consumers (the business's customers) do **not** interact with AtlasHub directly. They interact with the app the business built using AtlasHub's API. However, **their data (profiles, purchase history, loyalty)** is stored in AtlasHub and accessible to the business.

---

## 4. Positioning

| Competitor | What they do | AtlasHub vs. them |
|---|---|---|
| StoreApp | Desktop POS for Nigerian stores | AtlasHub is API-first, cloud-native, and covers HR + Accounting too |
| Medusa | Headless commerce engine | AtlasHub adds payments, HR, accounting, logistics — no integration needed |
| ERPNext | Open-source ERP | AtlasHub is SaaS, API-first, designed for Nigerian SMBs |
| Paystack | Payments | AtlasHub sits on top of Paystack — adds commerce, HR, accounting |
| Shopify | E-commerce platform | Shopify is app-only; AtlasHub is API-first and includes full operations |

**Differentiator**: One unified API for everything a business needs to operate — commerce, payments, HR, accounting, logistics — with zero integration overhead and no stitching.

---

## 5. Business Model

### 5.1 Freemium Tier

**HR module is always free** — any registered business can use HR (employee management, attendance, leave, basic payroll) without an active subscription. This reduces the barrier to entry and builds stickiness.

### 5.2 Subscription (Advanced Features)

All advanced modules — Commerce, Pay, Inventory, Logistics, Accounting — require an active subscription.

- **Pricing model**: Flat monthly subscription, per-seat
- **Trial**: All advanced features are free for the first 30 days after registration
- **One plan covers all modules** — no modular add-on pricing

### 5.3 Professional Services

Businesses may pay AtlasHub to build their consumer-facing frontend (POS app, storefront, etc.) as a separate engagement.

### 5.4 No Transaction Fees

AtlasHub does not sit in the payment flow and does not take a cut of transactions. Paystack/Moniepoint settle funds directly to the business's own bank account.

---

## 6. API & SDK

### 6.1 API Design

- **Protocol**: REST (primary, launched first)
- **Format**: JSON
- **Versioning**: `/api/v1/` (URL versioning)
- **Authentication**: API keys (server-to-server) + JWT (dashboard sessions)
- **Modes**: `live` and `test` (see section 6.2)
- **Rate limiting**: per API key, per organization
- **Documentation**: OpenAPI 3.0, auto-generated from controllers via Swagger

### 6.2 Test Mode vs. Live Mode

Every organization gets **two sets of API keys**:

| Key type | Prefix | Behaviour |
|---|---|---|
| Live secret key | `live_sk_...` | Real transactions, real money, real Paystack/Moniepoint calls |
| Test secret key | `test_sk_...` | No real money moves; uses Paystack test mode; all other modules behave identically |

**Why this is necessary**: Developers must be able to build and test their POS/storefront integration without charging real cards. Without test mode, every mistake in code costs real money, and no developer will trust the platform. Paystack natively supports test/live keys — AtlasHub mirrors this pattern at the platform level.

Test mode data is siloed from live data within the same organization.

### 6.3 SDK Roadmap

| Phase | SDK | Notes |
|---|---|---|
| Phase 1 | REST API (no SDK) | Full API reference + Swagger docs |
| Phase 2 | JavaScript / TypeScript SDK | Node.js + browser compatible |
| Phase 3 | Flutter / Dart SDK | Mobile-first markets |
| Phase 4 | Python SDK | Backend integrations |

### 6.4 Webhooks

Businesses register HTTP endpoint URLs in their dashboard. AtlasHub sends signed webhook payloads to those URLs when events occur across any module:
- Payment successful / failed
- Order completed
- Shipment dispatched
- Payroll disbursed
- Stock level low
- etc.

Webhook payloads are signed with an HMAC secret. Businesses validate the signature before processing. Delivery is retried with exponential backoff on failure.

---

## 7. Core Modules

### 7.1 Platform (atlashub-platform)

| Module | Purpose | Subscription required? |
|---|---|---|
| `authentication` | API key management, JWT auth, dashboard login | No |
| `accounts` | Org registration, user profiles, outlet management | No |
| `iam` | Roles, permissions, team member invitations | No |
| `billing` | AtlasHub subscription management, invoicing | No (manages subscription) |
| `compliance` | KYC/KYB document submission and review | No |
| `notifications` | Email, SMS, push notifications for all events | No |
| `admin` | AtlasHub internal staff tools (KYC review, banning) | N/A (AtlasHub staff only) |
| `catalog` | Subscription plans and fee structures | N/A (AtlasHub managed) |

### 7.2 Pay (atlashub-pay)

| Module | Purpose | Subscription required? |
|---|---|---|
| `pay:accounts` | Virtual NUBAN for the org's business wallet (Anchor-issued) | Yes |
| `pay:charges` | Collect payments from end-customers via Paystack/Moniepoint | Yes |
| `pay:transfers` | Disburse money — payroll, vendor payments, refunds | Yes |
| `pay:mandates` | Recurring charges on end-customers (gym, subscription box) | Yes |
| `pay:ledger` | Internal double-entry ledger — source of truth for all money | Yes |
| `pay:tx-query` | Transaction history and wallet balance read model | Yes |
| `pay:webhooks` | Deliver payment events to business's own webhook endpoint | Yes |
| `pay:settlement` | Track Paystack settlement batches to business bank account | Yes |
| `pay:splits` | Revenue split rules for marketplace businesses (see Section 9) | Yes — conditional |

### 7.3 Commerce (atlashub-commerce)

| Module | Purpose | Subscription required? |
|---|---|---|
| `commerce:storefront` | Orders, POS checkout logic, till management, credit sales, layaway | Yes |
| `commerce:inventory` | Product catalog, stock levels, multi-location, stock movements | Yes |
| `commerce:customers` | End-customer profiles, purchase history, loyalty | Yes |

### 7.4 HR (atlashub-hr)

| Module | Purpose | Subscription required? |
|---|---|---|
| `hr:employees` | Employee onboarding, profiles, contracts | **No — free** |
| `hr:payroll` | Payroll runs, payslips, maker-checker | **No — free** |
| `hr:attendance` | Clock-in/out, attendance records | **No — free** |
| `hr:leave` | Leave requests, approvals, leave balances | **No — free** |
| `hr:loan` | Employee loan management | **No — free** |

### 7.5 Accounting (atlashub-accounting)

| Module | Purpose | Subscription required? |
|---|---|---|
| `accounting:gl` | General ledger, chart of accounts, journal entries | Yes |
| `accounting:ap-ar` | Accounts payable, accounts receivable, invoicing | Yes |

### 7.6 Logistics (atlashub-logistics)

| Module | Purpose | Subscription required? |
|---|---|---|
| `logistics:shipping` | Outbound deliveries to end-customers | Yes |
| `logistics:receiving` | Inbound supplier deliveries (GRN) | Yes |
| `logistics:returns` | Customer return shipments | Yes |
| `logistics:transfers` | Inter-outlet stock transfers | Yes |

### 7.7 Hotel (atlashub-hotel)

**Phase 2.** Covers room management, reservations, F&B, housekeeping.

---

## 8. Customer Management

Businesses track their end-customers within AtlasHub. The `Customer` entity is owned by the business (multi-tenant isolated). Customer data includes:

- Profile: name, email, phone
- Purchase history (linked to orders)
- Loyalty points / tier (if business uses loyalty)
- Credit account (for credit sales)
- Layaway/deposit records

Businesses access customer data via API (`GET /customers`, `GET /customers/{id}`, etc.) and from the admin dashboard.

---

## 9. Vendor / Marketplace (Conditional)

### What it is
A business using AtlasHub could build an **Amazon-style marketplace** where independent vendors list their own products and inventory. The business's storefront aggregates all vendor products.

### Impact on architecture

If this feature is included:
- A `Vendor` entity is added to `commerce:catalog`
- Vendors have their own product catalog and inventory
- `pay:splits` becomes essential — when a customer buys from a vendor, revenue is split between the business (platform fee) and the vendor (sale proceeds)
- Vendor payout runs through `pay:transfers`

### Decision — ✅ LOCKED

**Vendor/marketplace is Phase 2.** `pay:splits` is deferred to Phase 2. Both are documented in the architecture but not built in Phase 1.

Phase 1 commerce is single-vendor only — the subscribing business is the sole seller.

---

## 10. Admin Dashboard (Frontend)

### 10.1 Technology
- **Framework**: React + Vite
- **Type**: Single-page application (SPA)
- **Auth**: JWT from `authentication` module

### 10.2 What it is

A **management and analytics interface** — not a consumer app. Think Stripe dashboard.

### 10.3 What it is NOT
- Not a POS terminal (businesses build that using the API)
- Not a consumer storefront
- Not a customer-facing app

### 10.4 Dashboard Sections

| Section | Who uses it | Key screens |
|---|---|---|
| **Overview** | Owner, Admin | Revenue today, pending payroll, low stock alerts, active orders summary |
| **Pay** | Owner, Finance | Wallet balance, transaction history, initiate transfer, mandates list |
| **Orders** | Store Manager, Admin | Order list, order detail, till sessions, credit sales |
| **Inventory** | Store Manager | Product list, stock levels per outlet, low stock alerts, movement log |
| **Customers** | Store Manager, Admin | Customer profiles, purchase history, loyalty tiers |
| **HR** | HR Manager, Owner | Employees, payroll runs, leave requests, attendance |
| **Accounting** | Finance, Owner | P&L, balance sheet, journal entries, AR/AP |
| **Logistics** | Store Manager | Active shipments, GRNs, returns, inter-outlet transfers |
| **Team** | Admin, Owner | Members, roles, permissions, invitations |
| **Developers** | Developer | API keys (live + test), webhook endpoints, API request logs |
| **Settings** | Owner | Subscription, outlets, compliance documents, notification preferences |
| **Compliance** | Owner | KYC/KYB document upload and status |

---

## 11. Phase 1 Scope (MVP)

**Target vertical**: Retail store with 1–3 physical outlets + optional online storefront.

**A new business in Phase 1 can**:
1. Register and complete KYC
2. Subscribe (or use 30-day trial)
3. Add products and set stock levels
4. Open a till and process POS sales (via their own POS app built on AtlasHub API)
5. Accept card payments via Paystack and bank transfers via virtual NUBAN
6. Manage employees and run payroll
7. View basic P&L and transaction history in dashboard
8. Receive payment events via webhooks to their own system

**Modules in Phase 1:**
- `authentication`, `accounts`, `iam`, `billing`, `compliance`, `notifications`
- `pay:accounts`, `pay:charges`, `pay:transfers`, `pay:ledger`, `pay:tx-query`, `pay:webhooks`, `pay:settlement`
- `commerce:storefront`, `commerce:inventory`, `commerce:customers`
- `hr:employees`, `hr:payroll`, `hr:attendance`, `hr:leave`
- `accounting:gl`, `accounting:ap-ar`

**Deferred to Phase 2:**
- `pay:mandates`, `pay:splits`
- `logistics` (all submodules)
- `hotel`
- Vendor/marketplace
- JS SDK, Flutter SDK, Python SDK

---

## 12. Out of Scope (Explicitly)

- ❌ AtlasHub is not a bank — no direct banking licence
- ❌ No consumer-facing UIs built by AtlasHub (professional services excluded)
- ❌ No manufacturing module (Phase 1)
- ❌ No multi-currency (Phase 1 — NGN only)
- ❌ No AI/ML features (Phase 1)
- ❌ No inter-business marketplace between AtlasHub subscribers
- ❌ Hotel module (Phase 2)
- ❌ Vendor/marketplace (Phase 2 — pending decision)

---

## 13. Success Metrics (Phase 1)

- 10 paying business subscribers in first 3 months
- At least 3 businesses actively processing transactions via the API
- API uptime ≥ 99.5%
- Time to first successful API call for a new developer ≤ 30 minutes (with docs)
- Zero critical billing or payroll errors in production
