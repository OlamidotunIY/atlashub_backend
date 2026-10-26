# Atlas Hub Java Backend — Storefront Platform integration contract

**Shared contract update (2026-10-26):** [atlashub_contracts](https://github.com/OlamidotunIY/atlashub_contracts) now holds the first Protobuf `v1alpha1` public [catalog contract](https://github.com/OlamidotunIY/atlashub_contracts/blob/main/proto/atlashub/commerce/v1alpha1/public_catalog.proto). Java can implement native gRPC for trusted clients, while browser themes must use a compatible Connect/gRPC-Web gateway with verified store identity. No native gRPC server, browser bridge or generated npm/Maven package is yet implemented. This contract is *not* Java domain model/authorization. Existing proposed REST-style paths here remain conceptual until transport is chosen and deployed.


**Status:** Proposed contract and migration, not implemented. Captures 2026-10-25 Storefront architecture decision. Backend/hosting, storage, SSR and Cloudflare details live in [atlashub_storefront](https://github.com/OlamidotunIY/atlashub_storefront). The independent merchant/creator frontend lives in [atlashub_storefront_portal](https://github.com/OlamidotunIY/atlashub_storefront_portal), centralized login/register frontend in [atlashub_auth_web](https://github.com/OlamidotunIY/atlashub_auth_web) and public theme creator SDK/CLI repository [`atlashub_theme_sdk`](https://github.com/OlamidotunIY/atlashub_theme_sdk) now exists with architecture documentation (source implementation pending). This document states **what Java must own or expose**.

## Repository boundaries

- **This Java backend:** users, identity/organization membership, RBAC, compliance and access checks, billing/theme entitlements, merchant provider integrations, products, variants, catalog public views, published price, taxes, inventory, orders, cart, trusted checkout, fulfillment and accounting.
- **atlashub_storefront backend repository:** independently deployed Node/TypeScript management API, PostgreSQL metadata, source/upload validation and build/publishing workers, marketplace/release/domain and Cloudflare Workers for Platforms hosting. Not a monorepo with frontends or SDK.
- **atlashub_storefront_portal (separate frontend):** creator/merchant marketplace, assets/configuration, domains, preview and publishing UX.
- **atlashub_auth_web (separate frontend):** central individual login/registration/verification/recovery UX (not a second backend); central SSO supported by Java/IdP.
- **atlashub_theme_sdk (created separate creator-tooling repo):** Vite React scaffold, public storefront SDK, CLI and contract packages; not yet implemented or published.
- **atlashub_web:** existing merchant dashboard, with storefront recommendations and safe external portal deep links only. It does not host complete theme marketplace/merchant publishing pages.

**No additional backend or authentication backend repo required.** The existing Storefront backend repo remains standalone and independently deployable. Separate Auth Web and Storefront Portal frontend repositories already exist; the independent theme SDK repository exists, but its tooling is not yet built. No Turborepo/Nx.

## Identity and SSO

Atlas Hub Java identity is authoritative across the central Auth Web, merchant Atlas Web and independent Storefront Portal. **User registration is a separate operation from Organization creation, with zero-org Users supported and no mandatory Anchor banking/KYB.** Independent creators can register creator profiles without forming a business; the same User may have creator status and memberships in several organizations.

Portal and Atlas Web use dedicated [Auth Web](https://github.com/OlamidotunIY/atlashub_auth_web) UI and require true central SSO: OIDC Authorization Code + PKCE or secure BFF equivalent with IdP session, independently scoped Portal session, secure cookies and revocation. Existing AtlasHub login/session does **not** automatically constitute OIDC SSO. No handoff of bearer tokens in redirect parameters; no reading Atlas Web JWT from another origin's localStorage. Validate selected organization/role/entitlement on every server mutation. Creator ownership is not merchant management permission.

Java IAM evolution: grant per-org storefront manage/license/domain/publish permissions; public storefront query scopes; creator identity associations/scopes; reviewer audit and scoped staff actions. If creator metadata stays in Storefront DB, Java User remains identity reference.

## Registration and provider activation boundary

- `RegisterUser` creates a valid global account independently of `CreateOrganization`; users need not own a business to build React themes.
- `CreateOrganization` establishes the business and OWNER membership with no Anchor bank/KYB. Provider connections are optional and do not gate normal merchant configuration or non-financial modules.
- Public checkout is enabled only when merchant-selected payment method has an active eligible provider connection and server-confirmed provider verification. No blanket compliance approval gate; applicable provider onboarding requirements still apply.
- Existing `platform:compliance` five-step Anchor KYB is a retirement/migration candidate; retain historical data appropriately. See [Identity separation](identity-organization-separation.md) and [banking decommission](provider-banking-decommission.md).

## Business data and public storefront APIs

**Source of truth:** Java stores products, variants, stock levels/reservations, prices and currencies, customers, cart/orders, payments, refund status, fulfillment and platform billing. No duplicate authoritative orders/product data in Storefront PostgreSQL or theme release archives. Java may publish versioned public projections/cache invalidation events to the Storefront platform.

Proposed versioned APIs (names are illustrative; align with real Java OpenAPI and contracts):
- `GET /storefront/v1/context`: public verified store/merchant, market/locale/brand, capabilities; trust context from verified dispatch mapping/server identity, not user-supplied org ID.
- `GET /storefront/v1/products`, `GET /storefront/v1/products/{handle}`, `GET /storefront/v1/collections`, etc: published catalog only; price and availability appropriate to selected verified storefront market.
- `POST /storefront/v1/carts` and items: safe cart/session mutation; authoritatively revalidate SKU/quantity, inventory, tax, price and merchant policy.
- `POST /storefront/v1/checkout/sessions`: validated total, allowed connected payment and shipping providers, controlled redirect/checkout session. Checkout/customer account security remains Java/Atlas controlled, separately isolated from arbitrary theme browser JS.
- `GET /api/v1/me`, authorization/entitlement/purchase APIs: Portal user/org membership and theme licensing, versioned platform service-to-service use.
- Events via existing outbox: product publication/update, price/availability, collection changes, org membership/suspension, theme purchase/refund/entitlement changes. Idempotent consumers; explicit retention/ordering/replay.

No generic proxy that lets themes reach `/api/v1/admin`, `/api/v1/pay/transfer`, accounting or private customer endpoints. All public commerce SDK calls are scoped to trustworthy Host->Storefront context and limited by server authorization/rate limits.

## Payment boundary

There are **two distinct payment flows**:

1. **Merchant selling goods in public storefront:** business-owned Paystack/Stripe/etc provider connection where eligible. Java commerce creates/owns order, cart, payment intent/attempt, provider routing, webhook verification, idempotent state and accounting events. Theme may display trusted checkout button, must not receive provider secret or claim payment succeeded from callback URL.
2. **Merchant purchasing an AtlasHub theme/license:** Atlas Hub **platform billing** owns checkout, entitlement, refund and accounting. Creator earnings are a derived projection/payout workflow subject to contracts and jurisdiction; not merchant payment funds.

Geographical merchant/provider/currency legality remains enforced even if theme SSR supports many countries.

## Theme/asset/routing boundary

Java does **not** host creator uploaded React files, run npm build jobs or store all merchant theme images in business tables. Storefront service owns private R2 source archive, approved immutable theme build assets, store-specific branding media and config versions. Catalog images remain Java Commerce-owned assets even if same object-storage provider is used. Java may retain/store a high-level storefront reference/entitlement and order provenance where needed, not a duplicate full Storefront release aggregate.

The Storefront service resolves verified hostname to isolated worker deployment and queries a **public Java commerce facade** with scoped, server-attested context. Never use domain header provided by arbitrary direct Java requests as sole proof of scope. Signed service identity, secure proxy forwarding and allowlists are required.

## Feature ownership / migration

Existing `atlashub-commerce/storefront` Java module remains authoritative for cart, POS, checkout and order workflow, not theme rendering, marketplace, third-party source execution or merchant custom domain TLS/hosting. The name **storefront** is overloaded: document `commerce.storefront` as domain order/checkout workflow and `atlashub_storefront` as separate website/design/hosting platform.

Old checkout saga currently mentions provider-specific charge events; migrate to merchant-connected provider-normalized payment events per [global provider proposal](global-provider-integrations-proposal.md), preserving authoritative order/stock transition and idempotency. POS is a separate capability and must not be disabled by lack of storefront theme/host.

## Required implementation/gates

- Central SSO and independent creator identity permission design; Java membership/entitlements cross-service contract.
- Public read API capable of high-scale tenant/market-safe queries, caching, authorized contact forms and cart/checkout; deny cross-store and leaked PII.
- Trusted checkout and provider eligibility in Java; no theme control of price/order/payment state.
- Platform theme license charge/refund/creator earnings event contracts and tax/legal review.
- Catalog media ownership and public media access, product/price/stock cache invalidation, domain/release context correlation.
- Contract/integration tests across Java backend, Atlas Web and Storefront; staging identity/config separate from live.
- Security tests: swapped Host, manipulated store ID, stolen portal cookie, malicious theme, cross-org install, license/purchase duplication, stale inventory, fake payment callback, replayed provider webhooks.

## Canonical Storefront repository documentation

- [System architecture](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/system.md)
- [Portal / SSO](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/portal-identity.md)
- [SDK and CLI](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/developer/sdk-cli.md)
- [API/event contract](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/contracts/platform-apis.md)
- [Storage](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/storage-data.md)
- [Runtime](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/runtime-hosting.md)
- [Marketplace lifecycle](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/marketplace-lifecycle.md)
