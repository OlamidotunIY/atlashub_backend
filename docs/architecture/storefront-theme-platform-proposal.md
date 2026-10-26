# Atlas Hub Storefront Platform — accepted proposal for implementation

**State:** Accepted design direction (2026-10-25), implementation not started. **This supersedes** the earlier draft describing a constrained declarative-only editor and Next.js-first website. Do not treat proposed services, CLI, SSO or Cloudflare deployment as implemented.

**Primary long-form source of truth:** [atlashub_storefront documentation index](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/README.md). Java cross-service contract: [storefront-platform-integration.md](storefront-platform-integration.md).

## Final user experience

### Merchant

Register individual account through Auth Web **without creating a business or passing Anchor KYB**; use Atlas Web for separate optional `CreateOrganization` when needed. Continue using Atlas Web for business administration, which shows Storefront recommendations/links. Clicking opens **dedicated Storefront Portal** via safe URL/deep link, automatically signed in **if** central Atlas Hub identity session exists and OIDC/BFF SSO is implemented. In Portal merchant can explore marketplace, view demos, purchase/license and install theme, configure defaults/overrides (logo, colors, banners, font, copy, collection, social links), upload store images, manage drafts/previews, verify custom domains/TLS, publish, update or roll back. Java still owns catalog/products, pricing, inventory, orders, payment/shipping availability and checkout; no duplicate merchant operations dashboard.

### Developer

Independent creator joins Atlas Hub identity (does not need merchant registration), uses **Vite + React + TypeScript + React Router Framework Mode scaffold** with required public pages, SDK and theme manifest. Complete design freedom for layouts/animations/styles/approved interactions, limited ability to access platform data. Local development and proposed `atlashub theme validate/build/preview/publish` CLI; source upload, isolated build, security review, immutable version, listing and creator workspace (versions, earnings, reviews/unlisting). No drag-and-drop theme authoring at launch. Developer provides valid default values for configurable fields; merchant dashboard forms consume developer-declared schema and preserve overrides across releases.

Portal's merchant and creator workspaces use **one AtlasHub user identity** and separate app sessions; same user may use both. Atlas Web does not duplicate Portal full marketplace or publishing.

## Repositories and technology

| Repo | Role |
|---|---|
| `atlashub_backend` | Java authoritative business/identity/billing/checkout APIs, permission and events |
| `atlashub_web` | Existing merchant operations and Storefront recommendations/deep links |
| `atlashub_storefront` | **Standalone Node/TypeScript management/hosting backend repository**, theme registry/build/review, domain management, Cloudflare Workers for Platforms and storage |
| `atlashub_storefront_portal` | **Separate frontend repository** for merchant theme marketplace, stores and creator dashboard |
| `atlashub_auth_web` | **Separate frontend repository** for central login, user signup, verification and recovery; Java/IdP owns sessions/token/identity |
| [`atlashub_theme_sdk`](https://github.com/OlamidotunIY/atlashub_theme_sdk) (created, documentation-first) | Independent Vite/React scaffold, public theme SDK, CLI and versioned contracts; implementation pending |
| `atlashub_web` | Existing merchant business dashboard and links to Portal, no duplicated marketplace |

**Repositories are independent (no Turborepo/Nx)**: Portal, Auth frontend, Storefront backend/hosting, Java backend and Atlas Web have independent build/deploy. SDK repo is created; tools and npm packages are not yet implemented. No additional standalone authentication **backend** repository needed, although Auth **frontend** is separate.

## Hosting selected subject to prototype

Preferred runtime: **Cloudflare Workers for Platforms** in **untrusted mode**, one isolated user Worker per installed merchant storefront initially, shared trusted **dynamic dispatch Worker** resolves verified custom hostname to deployment, per-merchant usage quotas; optional constrained outbound gateway. **Cloudflare R2** private source archive + immutable approved static/theme release storage + merchant-specific public branding assets; **Cloudflare CDN** for content-hash assets/cached public pages; **Cloudflare for SaaS** verified domains/certificates. **PostgreSQL** belongs to storefront metadata, not Java business entities.

React Router Framework Mode offers SSR and pre-rendering needed for SEO/public catalog; Vite is build/dev tooling, not an SSR runtime by itself. Next.js is not selected or required. Verify build and deployed SSR compatibility in Workers for Platforms, isolation and cost/limits in two-merchant/two-theme prototype before permanent production commitment.

## Runtime flow

1. Developer submits private source archive by scoped CLI upload. Isolated builder validates dependencies, contracts, assets, CSS/scripts/permissions, produces client and server bundles, calculates immutable digests. Security/human review approves public marketplace listing.
2. Merchant license confirmed through AtlasHub **platform billing**. Installation references approved ThemeRelease and a validated merchant config revision (developer defaults + merchant overrides/asset IDs).
3. Merchant domain ownership and certificate verification produce a ready DomainBinding. Published storefront pointer maps to a healthy per-merchant Worker deployment.
4. Customer requests domain. Trusted dispatch validates exact hostname/store mapping -> isolated Worker SSR returns SEO HTML + CDN assets + hydrated React.
5. Theme's scoped public SDK obtains catalog view models from Java through authorized public gateway. Cart/checkout on trusted Java/Atlas-owned surfaces recalculates prices/tax/stock/shipping/payment options; provider webhooks are verified server-side before order marked paid.
6. Publish and rollback atomically switch ThemeRelease/ConfigRevision/Worker mapping; cache invalidation keyed by store/market/locale/version; preserve store data when theme changes.

**Security:** sandbox builds and server SSR, but React browser JS is still untrusted and can alter its page. Restrict CSP/egress and SDK capabilities, disallow privileged bindings/secrets, keep account/checkout origins trusted and separate, enforce server-side purchase/licensing/tenant rules. Unlisting a theme does not normally break existing licensed installations; abusive/harmful release revocation requires policy and incident workflow.

## Full specification links

- [Storefront system architecture](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/system.md)
- [Portals and SSO](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/portal-identity.md)
- [Creator SDK/CLI](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/developer/sdk-cli.md)
- [Theme routes, settings/defaults, manifest](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/contracts/theme-manifest.md)
- [Publishing, review, paid theme lifecycle](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/marketplace-lifecycle.md)
- [Object storage and tenant assets](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/storage-data.md)
- [SSR deployment and isolation](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/runtime-hosting.md)
- [Domain + trusted checkout + SEO](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/domains-checkout-seo.md)
- [Security threat model](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/security/theme-sandbox.md)
- [Cost, quotas and support](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/architecture/operations-costs.md)
- [API and event contracts](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/contracts/platform-apis.md)
- [Implementation roadmap](https://github.com/OlamidotunIY/atlashub_storefront/blob/main/docs/modules/index.md)

## Required Java migration

Decouple individual `RegisterUser` from authenticated `CreateOrganization` (OWNER membership); allow no-org user/independent creator; remove mandatory Anchor banking/KYB compliance gates and automatic bank account provisioning. Payment capability through merchant-owned provider connection only when supported, not universal Atlas-held funds. See [v2 PRD](../atlashub-prd-v2.md), [identity](identity-organization-separation.md), [provider decommission](provider-banking-decommission.md).


Create central SSO identity and independent creator ability, new permissions and entitlement APIs; define versioned public storefront commerce facade; separate theme licensing from merchant checkout payment rails; normalize provider-connected charge events; emit catalog/inventory/price invalidation; keep existing commerce-storefront POS/order/checkout functionality in Java; enforce trusted cart and checkout regardless of theme rendering. No production code is changed by this document.

## Go/no-go gates

End-to-end two independently authored React theme packages, two distinct merchants (including two installations of same approved theme), server-rendered discoverable product pages, correct tenant mapping and pricing, secure origin/checkout, unsafe dependency/network restrictions, SSO session and role isolation, domain verification/TLS, accurate publishing/rollback, storage lifecycle, per-store Worker cost/performance benchmarks. If Worker SSR cannot satisfy security/cost/compatibility, record a replacement ADR before shipping.
