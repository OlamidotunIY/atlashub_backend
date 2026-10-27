# Target architecture — independent user identity and organization lifecycle

**Status:** Approved product direction; proposed implementation (2026-10-25). This document **supersedes mandatory atomic user+organization registration** in historical accounts/auth/compliance docs. No code changes are made by this PR.

## Decision
**User registration and organization creation are separate authenticated operations.** A global Atlas Hub account may belong to zero, one or multiple organizations; it may be an independent theme creator or AI user without being a merchant. Creating an organization is optional and must not trigger mandatory Atlas-owned banking, Anchor KYB or deposit account provisioning.

### User registration/authentication
- Central user identity: `User` global ID, verified email/identity fields, optional profile, locale, password/passkey/credentials delegated to existing Java `auth` and/or standards-compliant IdP. `AuthAccount` references User, not mandatory Organization.
- `RegisterUser` command creates User + AuthAccount/verification state with transactional/idempotent account creation. Global unique normalized email, verified email proof, rate limiting, secure password hashing/registration and session revocation.
- User may register and log in with **no organization**. `activeOrganizationId` in authenticated context is nullable; organization-scoped endpoints fail with deliberate `ORGANIZATION_REQUIRED`/403 if absent, while identity and creator functions work. No fake default org, no auto-created shell org.
- After sign-in: Atlas Web shows `Create a business` / `Join via invitation`; Storefront Portal shows `Become a creator` / browse public marketplace. Product use can be independent of organization when permitted.
- Central **Auth Web** frontend lives in [atlashub_auth_web](https://github.com/OlamidotunIY/atlashub_auth_web), handling login/register/verify/recovery/MFA screens. Java/IdP remains credential, session and OIDC authority. Atlas Web + [Storefront Portal](https://github.com/OlamidotunIY/atlashub_storefront_portal) use central SSO. No second auth backend repository required.
- Use OIDC Authorization Code + PKCE/BFF for multiple browser apps. Existing JWT/session auth is **not automatically conformant IdP SSO**. App-specific sessions, audience checks, logout propagation, no raw tokens in redirect URL/localStorage across origins.

### Create organization (distinct workflow)
1. An authenticated verified user invokes `CreateOrganization` with business/trading name, jurisdiction/country, optional industry/type, timezone/base reporting currency, legal entity details **when applicable**; no provider account, Anchor customer ID, bank verification, document uploads or business KYB required to create the organization.
2. Transaction creates Organization + OWNER membership and an event `OrganizationCreated` (or deliberately migrated alias) in an idempotent transaction. Existing users may join via invitation; never create duplicate User to create another organization.
3. Business gets role-based entitlements/plan setup without triggering any bank account provisioning or legal onboarding for ordinary product usage. Organization status **ACTIVE** for core permitted operations independent of integrations (separate subscription and abuse status gates).
4. Separate `ConnectProvider` and `EnableProviderCapability` workflows handle desired payment/logistics services *only* when merchant chooses to connect them; provider owns its onboarding/KYC/KYB and verification according to API/method/contract; platform can show provider verification state and links/requests, not run mandatory five-step Anchor flow.
5. Outlet, country/market, timezone, currency, storefront and physical legal entity must be modeled independently; support user/global country and provider-country scopes, not NG-only gating.

### Proposed commands, queries and events (names illustrative)
- `RegisterUser`, `VerifyUserEmail`, `AuthenticateUser`, `GetCurrentIdentity`, `ListMyOrganizations`.
- `CreateOrganization`, `InviteOrganizationMember`, `AcceptOrganizationInvitation`, `SelectActiveOrganization`, `SuspendOrganization`, `GetOrganizationEntitlements`.
- `CreateCreatorProfile` in Storefront service references existing User, no Organization needed; earnings onboarding handled later as specific provider/legal requirement.
- `UserRegistered`, `UserEmailVerified`, `OrganizationCreated`, `MembershipCreated`, `OrganizationSuspended`, `OrganizationCountryChanged`.
- Existing `OrganizationRegistered` and existing Java handler names remain until versioned migration; do not simply rename emitted events without dual-consumer compatibility and data migration.

## Ownership
- **accounts** domain owns User, Organization, Outlet, Owner Membership reference and independent business onboarding commands.
- **authentication** domain owns sessions, credentials, email verification, passkey/MFA challenge, OIDC IdP/integration.
- **IAM** owns organization role membership, permissions, invitations, org switch authorization; user identity is not organization role.
- **billing** owns org subscriptions and platform theme licenses, not global identity signup.
- **integrations** owns optional external connection states and eligibility; provider compliance/verification belongs to provider onboarding.
- **compliance (legacy module)**: retire obligatory organization banking KYB journey; preserve minimal appropriate platform privacy/terms/abuse/sanctions checks where legally required, not a blanket global gate.

## Data migration (critical)
- Existing User/Organization created together remain valid; split DB unique user registration from org creation, permit zero-org user and many memberships; don't delete existing orgs/users.
- Resolve dual-account email collisions and AuthAccount/User projections; preserve session validity or deliberately issue a safe reauthentication.
- Remove `compliance==APPROVED` as generic `canUseAtlasHub`/login/LIVE guard; replace with per-feature eligibility gates and provider account status. Retire bank-specific data through retention/compliance review, not immediate destructive deletes.
- Decompose `Country.deriveCurrency` and NG-only enum, distinguish org/home/outlet/legal-entity currency and market/settlement currency.
- Review all `OrganizationRegistered` consumers (IAM owner membership, billing, compliance init, payments/banking provisioning, analytics) and change their behavior atomically or behind compatibility flags. No accidental banking provisioning on org creation.
- Preserve existing merchant plans/entitlements and historical accounting/financial records; migration requires integration tests, contracts, and staged rollout.

## Acceptance tests
Register->verify->login user with zero org; creator registration without organization; later create multiple organizations; invitation join; org picker and nullable active org; independent HR/inventory/accounting use without a connected provider; payment checkout gated until verified eligible provider; provider onboarding failure must not block organization or identity; revoked org rights must not block global identity login; all cross-tenant commands denied.

## Not a promise of no compliance
Moving away from Atlas-operated banking removes the **platform-owned universal Anchor KYB approval gate**, not privacy/data protection, applicable sanctions/fraud controls, consumer protection, tax, provider contracts or the obligations of any future regulated finance product. See [provider integration migration](provider-banking-decommission.md).
