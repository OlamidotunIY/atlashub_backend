# Architecture proposal: AtlasHub Storefront, theme engine and creator ecosystem

Status: PROPOSED — not implemented. Date: 2026-10-24.
Companion repository proposed: `atlashub_storefront` (TypeScript monorepo), not yet provisioned.

## Separate the systems
- AtlasHub Java commerce core owns products, prices, inventory, fulfillment location selection, order lifecycle, customer checkout, provider routing and permissions. Expose versioned storefront-facing APIs; do not expose internal aggregates or shared database tables.
- Storefront platform backend owns theme registry, theme versions, manifests, tenant storefront configuration, publication, asset references, domain verification, render deployment, and third-party creator workflow. Platform billing/entitlement source of truth remains Java, coordinated through supported ports/events.
- Theme engine owns a constrained declarative format (page templates, sections, blocks, data bindings, design tokens, language strings and assets) and rendering contracts.
- Storefront runtime is the tenant-aware public renderer. Next.js is an optional implementation choice for this runtime, not the entire theme engine or one project per theme.
- Theme editor handles previews, draft/published state, drag/drop or configuration UI, validation and revisions.

## Why hundreds of themes are feasible
A single multitenant deployment can serve thousands of storefronts by resolving Host -> Storefront -> PublishedThemeVersion -> configuration -> commerce data. A store is a tenant configuration + domain + active theme, not a separately deployed server by default. 200 themes can be separately versioned theme packages/manifests and assets while using one constrained component library and one runtime. No need for 200 Next.js apps or 200 Node servers.

Alternative to Next.js: a custom Node.js (Fastify/NestJS) server with a Liquid-like safe template interpreter and CDN caching. Recommend TypeScript monorepo and a renderer spike **before freezing** Next.js as mandatory. Next.js offers SSR/streaming/routing and documented multi-tenant patterns; creator flexibility and security depend on theme format, not Next.js itself. See https://nextjs.org/docs/app/guides/multi-tenant and https://shopify.dev/docs/storefronts/themes/architecture.

## Proposed repository layout
```
atlashub_storefront/
  docs/
  apps/
    management-api/       # Theme/domain/deployment/catalog APIs (Node/TypeScript)
    storefront-runtime/    # Single multi-tenant SSR/runtime (candidate Next.js)
    theme-editor/          # Creator/merchant browser editor
    jobs/                  # Build/validation/publishing/domain workers
  packages/
    theme-schema/          # Versioned manifests and safe bindings
    theme-engine/          # Render resolution and validation
    storefront-api-client/ # Java commerce API contracts
    components/            # Allowed audited UI primitives
    theme-sdk/             # Creator tooling / SDK
  themes/
    starter/               # First-party theme only; no other themes initially
```

## Theme contract
`ThemeManifest`: ID, creator, version, schema/runtime compatibility, supported page types, settings/sections/blocks, allowed feature flags, translations, assets, security constraints and license.
`Storefront`: organization, domain aliases, publication state, active theme version, locale, market, permissions.
`PageDefinition`: page type/route, ordered section instances, settings, data bindings and optional blocks.
`ThemeRelease`: immutable uploaded asset bundle, signature/hash, validation output, rollback version.
No theme directly chooses payment processor from secrets; checkout obtains allowed methods from Java commerce APIs. Inventory/price must not be authoritative in theme state.

## Creator security
Initial launch: approved declarative themes and audited components; no arbitrary server-side user JavaScript or build scripts. Third-party submissions get static analysis, sandbox previews, content security policy, asset limits, signed releases, human review/appeal, version compatibility checks and staged publishing. Any future custom executable extensions run isolated with explicit capabilities and resource budgets. Hard tenant isolation and hostname ownership verification prevent cross-store data leakage.

## Hosted domains
Merchant adds domain; verify DNS/ownership, provision certificate, map host to verified storefront, configure redirects, cache invalidation and safe origin routing. Prevent arbitrary Host header tenant spoofing; only resolve verified domain records. Shared multi-tenant runtime or regionally distributed runtime pools are preferred initially; dedicated deployments only if explicitly required. Domain registration sales are optional separate registrar integration, not necessary for connect-existing-domain MVP.

## Publishing sequence
Create store draft -> choose entitled theme -> configure sections/assets -> validate and preview -> verify domain -> publish immutable theme release/config -> warm cache -> serve catalog/pricing/checkout from AtlasHub -> rollback if necessary.

## DDD and Clean Architecture
Domains: ThemeCatalog, ThemeAuthoring, ThemeRelease, StorefrontPublication, DomainProvisioning, CreatorMarketplace, CommerceIntegration. Keep business rules and policies framework independent. CQRS for theme catalog/search/read projections and explicit publishing commands, but do not over-engineer ephemeral rendering operations. TypeScript monorepo can use per-package ports/adapters, schema validation and contract tests without duplicating every Spring abstraction.

## Non-goals and risks
A purchased theme does not guarantee unrestricted arbitrary code execution. Do not imply merchant data resides in a dedicated deployment. Preserve uptime and security, accessibility, performance, SEO, localization, payment redirects/webhooks, checkout order security, privacy and observability. Inventory pricing and provider capabilities remain verified by backend at checkout.
