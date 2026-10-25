# Atlas Hub — Product Requirements Document v2: Global, Provider-Connected Business Platform

**Status:** Target product specification **approved for implementation**, not an assertion software has been migrated. **Date:** 2026-10-25.
**Supersedes:** [historical v1 PRD](atlashub-prd.md) wherever it couples user signup, business activation and mandatory Atlas-held banking/KYB.

## 1. Mission
Global-first, modular multi-organization business operating platform for commerce, inventory, POS, HR, payroll, accounting, analytics, shipping orchestration, hosted storefronts and intelligent assistants. Atlas Hub **does not require merchants to bank through Atlas Hub**, does not create a virtual bank account by default and does not take custody of ordinary merchant order proceeds. Merchants connect their own eligible payment/logistics providers if they want externally fulfilled capabilities. Country features may vary legally/provider-wise.

## 2. Identity and onboarding — explicitly decoupled
**Register individual:** unique email, name, password/passkey as supported; global `User` and `AuthAccount`, verification and sessions; **no organization/KYB/provider** automatically created. Zero-org user permitted, authenticated user may become creator or accept org invitation.
**Create organization:** a separate authenticated explicit action; organization name, country/jurisdiction, optional legal entity/business details as context requires, timezone/reporting currency; transaction adds OWNER membership and business entitlements. No default Anchor deposit/FBO/subaccount or mandatory five-step KYB. Multi-org membership and invitation management with IAM. A user can choose an organization from Atlas Web after login.
**Frontend:** independent [Auth Web](https://github.com/OlamidotunIY/atlashub_auth_web) delivers universal sign-in/registration/recovery; existing [Atlas Web](https://github.com/OlamidotunIY/atlashub_web) provides merchant operations and organization creation; [Storefront Portal](https://github.com/OlamidotunIY/atlashub_storefront_portal) allows creators without business registration and merchant theme management. Actual central OIDC/BFF SSO must be implemented by Java/IdP; sharing UI is not sufficient.
**Provider onboarding:** optional and per capability; when connecting Paystack, Stripe, carriers, etc the provider may require verification; platform displays status and required steps, does not impose universal Anchor KYB before base operations.

## 3. Compliance scope and limits
Retire existing `platform:compliance` as a compulsory Atlas banking/KYB product journey and approval gate; migrate/retain old records safely, stop automatic Anchor customer generation and organization-state coupling. **Do not make the false claim Atlas Hub or merchants face no compliance:** contracts, data privacy, identity security, security/abuse, taxes, consumer rights, potential country-specific platform duties and provider KYC/KYB continue where applicable. If Atlas Hub later offers credit, wallet, escrow, payout control or other regulated services, these require an explicit separate legal/compliance product and feasibility assessment.

## 4. Provider integrations
Organizations may link eligible provider accounts with OAuth/hosted connection where supported, or encrypted scoped merchant credentials; no common Atlas-owned settlement account. Multiple eligible payment integrations and user choice/routing by country, currency, feature and actual provider account contract. Per-connection TEST/LIVE status, capabilities, health, idempotent webhooks, refunds, reporting, provider settlement reconciliation and audits. Missing provider means unavailable **specific payment capability**, not unavailable organization or commerce catalog.
Carrier integrations: per-merchant domestic/cross-border/last-mile provider connection, actual quote/book/track/cancel/returns and coverage. No mandatory platform-operated fleet/shipping split.

## 5. Platform financial truth
Java Commerce authoritative for items, prices, inventory, orders, fulfillment, cart and trusted checkout. Public browser themes only consume restricted storefront SDK; Java recalculates totals, discounts, tax, stock and eligible provider methods and verifies provider webhook/settlement outcomes. Third-party provider merchant funds are **not held by Atlas Hub**, so ledger/accounting should record external clearing, fees, invoices, settlements and reconciliation without inventing Atlas bank balance. Explicitly fail payouts/holds not authorized by the actual connected provider.

**Atlas Hub's own subscriptions, licenses and theme marketplace purchases** use separate platform billing/settlement, not the merchant's connected payment account.

## 6. Global organization data
Independent global identity country, organization registration country, legal entity country, outlet location, market, customer destination; ISO country/region codes, timezone; ISO currencies and minor units, multicurrency posting/FX where needed. Country registration support does **not** imply Paystack/Stripe/payroll/shipping/tax support. Avoid NG/NGN-only hardcoding and automatic assumption that all outlets share organization country.

## 7. Storefront ecosystem and repositories
Developers use Vite+React+TypeScript+React Router Framework Mode SDK/CLI/scaffold with typed public Java storefront APIs, design freedom under strict security capabilities and developer-supplied settings defaults. No initial drag/drop.
Dedicated Storefront Portal frontend [atlashub_storefront_portal](https://github.com/OlamidotunIY/atlashub_storefront_portal) for both creators and merchants; shared SSO. Existing Atlas Web only displays recommendations/deep links and business operations. Storefront management backend and hosting platform remain in [atlashub_storefront](https://github.com/OlamidotunIY/atlashub_storefront), **not a monorepo** with Portal or Auth Web. Theme SDK/CLI/scaffold planned as a separate published package/repo (name `atlashub_theme_sdk`, not yet verified created).
Developer source private object storage -> isolated build/review -> immutable approved SSR/client release -> paid/free license and merchant install -> isolated Cloudflare Workers for Platforms untrusted mode with trusted hostname dispatch, R2/CDN asset storage, verified custom domain+TLS, merchant config/settings/images independently stored, protected checkout. Subject to prototype cost/SSR/security evaluation; no shared privileged arbitrary theme JS.

## 8. AI / Intelligence
Separate Python [atlashub_intelligence](https://github.com/OlamidotunIY/atlashub_intelligence) for self-hostable models, conversations, text/voice, optional speaker similarity as *risk signal only*. Java command handlers authorize business actions with RBAC/maker-checker/step-up; voice cannot authenticate alone. Strong tool/evidence/audit contracts, gRPC private service boundary.

## 9. Modules retained (product/business responsibilities)
- accounts/auth/IAM: global user, organization, permissions; central SSO.
- billing: subscription/theme marketplace licenses and platform revenue.
- integration registry: merchant connections and provider onboarding (replaces mandatory universal banking/compliance).
- payments: provider-neutral orchestration, merchant-owned settlement, event verification, refunds/reconciliation; preserve transaction truth but eliminate default bank.
- commerce: product catalog, inventory, POS, cart, orders, checkout, sellers/marketplace.
- logistics: fulfillment and external carrier orchestration, warehousing/returns/inventory transfer.
- accounting: GL, AP/AR, budgets, assets, cash/clearing, posting, trial balance, P&L, balance sheet and global FX; fix documented gaps.
- HR: employee/payroll/leave etc subject to jurisdiction availability; not gated by payment provider.
- analytics/admin/support/notifications: existing business product functions with updated policies.
- platform bank-specific compliance/Anchor and Atlas FBO/shadow ledger assumptions: **legacy migration/decommission**, not MVP prerequisites.

## 10. Minimum viable journey
Register person → verify email → login without organization → choose creator / create business / accept invitation. Create business without KYB → configure catalog, HR, inventory, accounting, and optionally storefront → connect external payment/logistics provider only when capability needed → provider completes its onboarding/verification → verified payment/shipping enables corresponding checkout routes → Java manages order/accounting events. Subscription/licensing gates are separate from provider capabilities.

## 11. Cross-repository source of truth
[Identity separation](architecture/identity-organization-separation.md); [provider/Anchor retirement](architecture/provider-banking-decommission.md); [merchant-provider architecture](architecture/global-provider-integrations-proposal.md); [Storefront Java API boundary](architecture/storefront-platform-integration.md); [migration roadmap](architecture/global-expansion-delivery-plan.md). Client implementations are separate deployable repositories without Turborepo/Nx.

## 12. Acceptance gates and migrations
No live provider charges before tested connected integration; no unknown charge treated as paid; no fake payout fallback; no unsolicited Anchor customer/account provisioning; user without org can login; independent creator can publish after review; country/currency migration tested; old banking resources reconciled and closed according to contracts; non-provider commerce works without payment integration; business suspension/role revocation enforced; historical accounting reconciled; domain/render and SDK public data cross-tenant testing passed. Preserve backward compatibility with existing `RegisterOrganization` and `OrganizationRegistered` consumers during rollout; old v1 docs are historical until fully retired.

## 13. Operating and legal boundaries
Platform provides software and integration orchestration, not universal financial services. Merchant/customer/provider geography, provider verification and merchant contracts govern actual capabilities. Never promise that all countries are fully supported or no legal compliance exists. Document applicable consent, privacy, API security, refund/consumer rights and tax responsibilities by market.
