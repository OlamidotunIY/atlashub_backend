# Global AtlasHub delivery plan and documentation inventory

Status: PROPOSED — implementation and rollout plan, not a claim of completed features.
Date: 2026-10-24.

## Existing module inventory
The current `settings.gradle` registers accounts/authentication, compliance, IAM, billing, commerce/catalog/inventory/storefront, pay/accounts/charges/ledger/settlement/splits/transfers/mandates/controls/tx-query, accounting/gl/cash/ap/ar/assets/budget, logistics/shipping/warehousing/returns, HR/staff/payroll/attendance/leave/loan, analytics, infrastructure Anchor/Paystack and a Spring Boot entry point.

The proposals preserve the Java modular monolith as business system of record. Two **separate repositories already exist**: [atlashub_intelligence](https://github.com/OlamidotunIY/atlashub_intelligence) for Python AI, and [atlashub_storefront](https://github.com/OlamidotunIY/atlashub_storefront) for the TypeScript Storefront Portal, Node management backend, SDK/CLI and rendering. Atlas Web is the existing business dashboard with storefront recommendations. **No additional storefront backend repository is required.**

## Accounting: documentation-to-code gaps found by static source inspection
- GL design: default chart-of-accounts seeding and `OrganizationComplianceApprovedListener` not present in current GL implementation.
- GL design: `GetAccountBalance`, `GetLedger`, `GetTrialBalance`, `GetIncomeStatement`, `GetBalanceSheet` query handlers not found in current GL implementation; existing GL query handlers cover account/journal lookup/list.
- GL design: proposed listeners for POS sale/refund, purchase orders, stock transfer, payroll, loans, settlements, and ledger bridge not present in GL source tree.
- `VoidJournalEntryHandler` calls `entry.voidEntry` and saves; the documented equal-and-opposite reversing journal is not separately created by that handler.
- Fixed assets depreciation scheduler exists and calls `RunDepreciationHandler`; that handler depreciates asset aggregates but does not directly post depreciation GL journals.
- BalanceSnapshot aggregate and repository exist, but a scheduled/command snapshot-generation implementation is not established in the reviewed GL source.
- `CurrencyCode` contains only NGN despite multi-currency and FX design document.
- Budget/AP/AR/Cash/Assets submodules do have handlers/controllers. Do not describe whole submodules as absent; completion, persistence and integration test verification remains open.

These findings are limited to static repository inspection; no runtime tests or sandbox provider flows have been executed.

## Prioritized delivery phases and exit criteria

### Phase A: freeze business and capability contracts
- Adopt the proposed boundary: merchant-connected external integrations, AtlasHub-owned business truth, billing segregation.
- Specify provider installation, connection ownership, capabilities, endpoint/event contracts.
- Specify legal entity/country/currency/address changes; avoid changing DB representation without a migration plan.
- Agree non-goals for global localization and financial services licensing.
Exit: reviewed ADRs, API schemas and migration/test matrix.

### Phase B: provider migration
- Migrate one provider end-to-end (Paystack merchant-connected), including account authorization, safe checkout, webhook validation, reconciliation, retries and refunds.
- Connect second eligible provider and test explicit customer choice/merchant routing.
- Replace fail-open fake payout successes with hard failures/unsupported statuses.
- Remove Anchor compulsory onboarding coupling; optionally preserve Anchor as one installed banking connector.
Exit: completed paid test order, refunds/replays/unknown-status recovery and accounting invariants.

### Phase C: worldwide data model and accounting
- Independent organization, legal entity, outlet, region, timezone and currency fields.
- Add currency precision and FX exchange snapshots, trial balance, P&L, balance sheet, full posting listeners, reversals and reconciliations.
- Audit payroll/taxes as country-specific capabilities and restrict unsupported jurisdictions explicitly.
Exit: NG historical migrations and multi-currency reconciliation tests pass.

### Phase D: logistics capabilities
- Refactor RouteType/quotes/3PL adapters to address/country-aware verified carriers.
- Add coverage, international and last-mile legs, customs and tracking/returns.
- Isolate optional AtlasHub-managed shipping incentives/contracts.
Exit: test domestic and international shipments with fully attributed external events.

### Phase E: dedicated hosted React storefronts (2026-10-25 decision)
- Reuse existing `atlashub_storefront` TypeScript monorepo for a separate Storefront Portal frontend (merchant and independent creator workspaces) plus its Node management backend, theme scaffold/SDK/CLI, review/build workers, hosting and storage integration. Atlas Web displays theme recommendations/deep links, not duplicate management screens; central IdP OIDC/BFF SSO must be built.
- Give third-party developers Vite + React + TypeScript + React Router Framework Mode route contracts, public storefront SDK and schema-defined merchant settings with valid defaults; no drag-and-drop MVP; creator submits source via CLI to private storage, isolated reproducible builds, security review and immutable theme releases.
- Merchant licensing/purchases use AtlasHub platform billing. Merchants customize own logo/colors/banners/content/assets in dedicated portal; manage domain validation/TLS, preview, publish, rollback and preserved overrides. The Java core remains authoritative for catalog, stock, pricing, orders, trusted checkout, provider routing and payment status.
- Selected subject to PoC: Cloudflare Workers for Platforms **untrusted mode**, one user Worker per storefront installation, trusted host dispatch with per-store limits, R2 private source/releases and tenant assets, CDN for immutable assets/public HTML caching, Cloudflare for SaaS custom domains, PostgreSQL for metadata only. Browser theme JS still requires CSP/review, and checkout/account stays trusted and separated.
- Prototype **two independently authored themes and two separate merchants**, including different and same theme release installs; prove SSR SEO, cross-tenant isolation, public Java SDK, provider-safe cart/checkout, catalog invalidation, domain certificate, rollback, script CPU/traffic/asset cost. If infeasible, choose safe alternative with new ADR; do not deploy arbitrary creator Node code in a privileged shared renderer.
Exit: merchants can enter from Atlas Web with central SSO, buy/install theme, configure branding, bind domain and publish a safe isolated public storefront. Current detailed contracts: [Java integration](storefront-platform-integration.md), [Theme proposal](storefront-theme-platform-proposal.md) and [Storefront repo](https://github.com/OlamidotunIY/atlashub_storefront).

### Phase F: intelligence
- Java tool policies + text-only read tools first; authenticated write commands with previews, approvals and audit second.
- Self-hosted Python model and gRPC integration; streaming speech and accessibility fallbacks.
- Optional consent-based speaker similarity/spoof risk testing only, never sole authentication.
Exit: demonstrated authorized workflows, denial of cross-tenant and impersonation attempts, repeatable model evaluations.

## Docs requiring subsequent reconciliation after decisions
`README.md`, `docs/README.md`, `docs/atlashub-prd.md`, `docs/design.md`, `docs/architecture/multi-currency.md`, and module docs for accounts, commerce/storefront, pay, compliance, logistics, accounting, intelligence and billing. The existing docs are retained as current/legacy design statements until feature-by-feature migration; proposed docs do not assert that implementation already exists.

## Cross-cutting gates
Security threat models, consent and retention, country availability, merchant/provider eligibility, RBAC, SCA or transaction step-up where relevant, test/live tenant separation, outbox-inbox idempotency, reconciliation, observability, runbooks and rollback all have explicit acceptance tests before launch.
