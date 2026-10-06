# Atlas Intelligence & Financing — Delivery Plan

## Purpose

This document identifies the work required to deliver the Atlas Intelligence and AtlasHub Financing demonstration, then the production-capable foundation behind it. AtlasHub remains the business operating system; it is not a lender, credit bureau, debt-recovery platform, or collateral-enforcement system.

**Database decision:** all operational records, analytics projections, feature snapshots, score snapshots, monitoring records, and audit records use the existing **MySQL** platform database. PostgreSQL and TimescaleDB are not prerequisites for this work.

## What Exists and What Must Be Built

The registered codebase provides organization/outlet identity, authentication, IAM, compliance, account provisioning, ledger foundations, audit infrastructure, and the event bus. The following capabilities are documented but are not present as registered implementation modules and must be built for the proposed demo:

| Priority | Capability | Why it is required |
|---|---|---|
| 1 | Commerce retail slice: catalog, inventory, customers, completed sales | Provides the real operational facts that make the demo credible. |
| 2 | Pay transaction/transfer slice and accounting GL/AP-AR slice | Provides cash movement, expenses, profitability, and reconciliation evidence. |
| 3 | MySQL analytics projections and feature snapshots | Produces deterministic, tenant-scoped metrics without cross-module table reads. |
| 4 | Atlas Intelligence | Production-oriented conversational AI, deterministic-tool orchestration, ML forecasting/anomaly services, recommendations, and traceability. |
| 5 | AtlasScore and Financing | Consent, applications, offers, facilities, score snapshots, monitoring, and lender integration abstraction. |
| 6 | Pay Controls | Enforces the active facility's policy for AtlasHub-controlled payment paths and exposes capability-aware holds/blocks/freezes. |
| 7 | Dashboard/API demo surfaces | Lets merchant and lender actors complete the coherent end-to-end story. |

## Required Delivery Order

1. **Retail and accounting truth.** Implement the retail, payment, and accounting events needed to record historical sales, inventory cost, receivables/payables, expense growth, cash movement, and outlet performance. Seeded data is acceptable for the hackathon only when it enters through the same aggregate/event paths as normal data.
2. **Analytics on MySQL.** Build idempotent event-driven projection tables and feature snapshots. MySQL uses composite unique keys and `INSERT ... ON DUPLICATE KEY UPDATE`; it does not use PostgreSQL `ON CONFLICT`, `jsonb`, RLS, or Timescale hypertables.
3. **Accounting-backed metrics.** Build/reconcile P&L, gross profit, operating-expense, cash-flow and repayment-coverage inputs. AtlasScore must not infer authoritative financial values through an LLM.
4. **AtlasScore.** Implement score versioning, fixed input snapshots, factor contributions, bounds, classification, and the capacity estimate. A score result is immutable after issuance.
5. **Financing lifecycle.** Implement request, explicit data consent, lender-facing package snapshot, mock lender submission, offer, acceptance, facility activation, and normal repayment status.
6. **Payment controls and monitoring.** Activate a facility policy, evaluate eligible AtlasHub payment instructions, block or freeze only controlled funds/rails where the configured provider capability permits it, then detect and explain anomalous activity.
7. **Intelligence experience.** Add the conversational API and tool registry only after deterministic tools exist. The AI uses authorized tool results and produces summaries, explanations, forecasts, and recommendations with evidence references.
8. **Demo integration.** Complete the merchant and lender dashboard/API flows, audit views, and end-to-end tests.

## Hackathon Vertical Slice

The demo uses Ade Stores Ltd with two outlets and several months of events. It must demonstrate:

1. A merchant asks a production-style conversational interface how the business is doing.
2. Deterministic MySQL projections and accounting reports supply the structured answer.
3. AtlasScore computes from a documented score version and shows its input/factor snapshot.
4. The merchant requests inventory financing, views the exact data categories, and explicitly consents.
5. A mock lender adapter receives an immutable application package and returns an offer.
6. The merchant accepts; an active facility and hybrid fund-use policy are created.
7. An AtlasHub-controlled payout/withdrawal is evaluated. An ordinary payment is allowed; an anomalous ₦800,000 attempt is held, blocked, or frozen according to the provider capability and policy.
8. The lender receives an early-warning signal and the merchant receives a grounded explanation plus growth recommendations.

## Production-Capable Capabilities Included in Scope

- **Accounting:** GL, AP/AR, cash reconciliation and financial-report query inputs remain first-class sources for profitability, cash-flow stability, expense growth, repayment coverage, and audit reconstruction.
- **ML:** feature generation, versioned forecast/anomaly models, training/evaluation metadata, model registry, drift monitoring, fallbacks, and deterministic confidence/limitations are required. A hackathon may ship one configured model/version, but must not hardcode model outputs.
- **Conversational AI:** a provider-neutral AI gateway, prompt/version registry, tool allowlist, authorization checks before every tool call, retrieval/context minimization, conversation/answer audit trail, redaction, rate limiting, response streaming, safety handling, and user-feedback capture are in scope.
- **Automated payment controls:** a financing policy can automatically allow, review, hold, block, or freeze funds on AtlasHub-owned ledgers, virtual accounts, and provider rails that explicitly expose that control. It cannot purport to freeze arbitrary external bank accounts.

## Explicit Boundary

AtlasHub monitors active financing, produces health/risk signals, orchestrates normal repayment, and applies approved controls on supported payment rails. It does **not** perform lender underwriting, make the lender's final credit decision, debt collection, recovery, GSI recovery, collateral enforcement, or other post-default enforcement. When a facility enters serious default/recovery, the lender owns the process; AtlasHub may retain and surface the lender-provided status.

## Documentation-to-Implementation Checklist

- Register the documented modules only when their contracts and tests are ready: `atlashub-analytics`, `atlashub-intelligence`, `atlashub-financing`, and `atlashub-pay:controls`.
- Add only shared synchronous read contracts under `atlashub-shared/.../application/port`; no module may import another module's internal repository or entity.
- Publish all cross-module mutations as outbox-backed events and consume them through idempotent listeners.
- Use `AuthenticatedPrincipal` for organization and user context; request bodies never choose the active organization.
- Add MySQL schema migrations, tenant composite indexes, optimistic locking where aggregates mutate, encryption/redaction for consent and lender data, and audit records for every decision/control.
- Require unit tests for each aggregate/handler and integration tests for JPA, MySQL upserts, Kafka listeners, provider adapters, controls, and security.
