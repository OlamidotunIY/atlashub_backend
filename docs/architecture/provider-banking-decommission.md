# Target architecture — merchant-owned providers and retirement of Atlas-operated banking

**Status:** Target design, migration not implemented. **Replaces** mandatory Anchor/NUBAN/Atlas subaccount and global banking KYB assumptions. Companion: [merchant provider registry](global-provider-integrations-proposal.md), [identity separation](identity-organization-separation.md).

## Product decision
Atlas Hub is a **software/business operations platform**, not the required bank or custodian for merchant funds. **Merchants connect their own eligible providers** for payment collection, payouts (where available), domestic/international/last-mile logistics, etc. Direct provider-to-merchant settlement wherever supported by that provider's contract. AtlasHub may provide orchestration, UI, webhooks, reconciliation and accounting projections but must not claim to hold/sweep/freeze external funds without separate contractual/legal authority.

A business with **zero** connected payment providers can still register organization, use HR, inventory, catalog, analytics and GL, configure storefront, do offline/non-provider workflows. Provider-dependent customer payment checkout and banking/transfer automation remain unavailable until a verified connection/capability is active.

## Remove global mandatory banking/KYB path
- Stop automatic `RegisterOrganization -> InitializeComplianceRecord -> five-step KYB -> Anchor business customer -> Atlas operating deposit/FBO/subaccount -> Paystack settlement to Atlas account` journey. Do not gate `Organization ACTIVE`, global login or `LIVE` business operations on `ComplianceStatus.APPROVED`.
- `atlashub-platform:compliance` as **the existing Anchor KYB product module** is **retirement/decommission candidate**: stop mandatory UI/trigger/approval dependencies; keep historical record read-only until retention/migrations are safely completed. Bank-specific status does not gate normal features.
- Do not delete all software/commercial compliance: user privacy/consent, marketplace moderation, anti-fraud, security, tax/company records, data protection and any regulated services added later retain their appropriate obligations. Provider-specific legal verification is performed according to provider's supported onboarding integration.
- Paystack's own merchant activation/compliance requirements remain independent; direct merchant credentials/connected accounts cannot bypass provider verification. If using Stripe Connect, choose provider-hosted onboarding where feasible; actual KYC collection responsibility depends on integration model, geography and provider agreement.
- Remove `registrationType` / industry restrictions that existed solely for Anchor from basic organization creation; if an org requests a provider service with restricted eligibility, deny **that capability**, not business signup.

## Provider connection model

Proposed `IntegrationProvider`, `OrganizationIntegration`, `IntegrationCredentialReference`, `ProviderCapability`, `IntegrationCapability`, `ProviderOnboardingSession`, `IntegrationRoutePolicy`, `IntegrationWebhookEvent`, `IntegrationHealth` and `ProviderComplianceStatus`. Secrets encrypted and isolated per organization and TEST/LIVE environment; use provider OAuth/hosted onboarding where supported or BYO restricted credentials plus provider validation. No raw tokens to React theme bundles.

Store explicit authorized owner, external merchant ID, verification status and capability matrix across country/currency/channel/method/limit. Multiple active eligible payment providers (Paystack, Stripe etc only where supported by merchant location and account) may be presented for customer choice or deterministic merchant routing; no invented eligibility. Separate collection, virtual account, card, USSD, POS, payout, refund, settlement report and logistics capabilities.

## Payment flow
1. Merchant connects payment provider and completes any provider-required onboarding (provider-hosted where possible); stores limited external status/credentials and settlement account reference only when needed.
2. Cart/Commerce creates Order/PaymentIntent; route selects eligible OrganizationIntegration and persists `PaymentAttempt(connectionId, environment, amount, currency, idempotencyKey)`; call provider through a connection-aware port.
3. Customer completes provider checkout through approved SDK/hosted fields/trusted Atlas checkout as appropriate; never send merchant secret to untrusted themes.
4. Independently verify signed provider webhook/server status, deduplicate, reconcile unknown outcomes; publish normalized `PaymentSucceeded/Failed`, `RefundConfirmed`, `SettlementConfirmed`, `PayoutConfirmed`. Java Commerce and Accounting own authoritative order/GL transitions.
5. Provider settles money to merchant (where contracted). AtlasHub books external clearing/fees and reconciliation, **not** fictional AtlasHub bank balances.
6. Funding-dependent features only become available where provider capability and terms actually allow. No default fake payout: `ExecutePayoutHandler` must fail closed when no gateway configured.

## Logistics flow
Merchants connect eligible last-mile/domestic/cross-border carriers. AtlasHub orchestrates shipping quotes/book/tracking/returns and retains inventory/fulfillment truth, while contracted provider delivers. Cross-border legal/customs duties remain appropriately assigned. Retire mandatory Atlas-owned fleet/shipping percentage fee assumptions unless deliberately offered as a separate managed service.

## Legacy module mapping and explicit retirement checklist

| Existing Java area | Target |
|---|---|
| `platform:accounts` `RegisterOrganizationHandler` | separate `RegisterUser` + `CreateOrganization`; no KYC/bank activation in registration |
| `platform:compliance` Anchor five-step KYB | stop global triggers/gates; archive/migrate sensitive records; use per-connection `ProviderOnboardingSession` |
| `infrastructure:anchor` clients/webhooks | remove compulsory integration; optionally preserve as disabled optional connector if legal/commercial reason; no default Atlas FBO/deposit service |
| `pay:accounts` `OrganizationBankingProfile`, `BusinessDepositAccount`, Anchor reserved accounts | replace default banking model with `OrganizationIntegration` + provider account references scoped to capability |
| `pay:charges` Paystack-centric listener, `ChargeProviderPort` | provider-neutral connection-aware charge attempts, standard normalized events and signed webhooks |
| `pay:settlement` Paystack->Anchor settlement polling | merchant-owned settlement reporting/reconciliation, provider-native direct settlement |
| `pay:transfers` payout service + MOCK success | connected provider payout only if supported; fail closed otherwise |
| `pay:ledger` shadow bank-balance assumptions | merchant accounting-clearing/reconciliation ledger, avoid depicting external bank balances as Atlas held |
| `pay:controls` freeze/hold funds | unavailable on external accounts unless provider explicitly permits and authorizes it |
| `logistics:shipping` fleet + 3PL simulated quotes | real per-merchant carrier connections, route origin/destination country/region/leg, no fictional live tracking |
| `billing` | AtlasHub's own subscriptions/theme-sales billing independent of merchants' payment collection |

## Countries and currencies
User/organization registration global where permitted. Distinguish **platform registration availability**, local company laws, provider country eligibility, tax/payroll jurisdiction, currencies and shipping coverage. Registering outside Nigeria does not imply local Stripe/Paystack or payroll support. Remove `Country SUPPORTED={"NG"}`, currency only NGN and auto-deduced outlet country after schema/migration/testing.

## Migration phases
1. Inventory bank accounts, Anchor tokens and webhook subscriptions, KYB/PPI docs, merchant current balances, payouts and liabilities; legal/regulatory review of customer funds and exiting arrangements.
2. Introduce optional provider connection model and normalized payment/shipping contracts alongside legacy paths; TEST/LIVE isolation, encrypted credentials, least privilege and per-org auth.
3. Split identity/organization registration; remove KYC and Anchor hooks from registration; feature flags to bypass legacy global approval without weakening other security controls.
4. Bring existing merchants to real merchant-owned provider connections and capture validated settlement destinations; reconcile historic inflight charges, refunds, account balances, ledger entries and hold/release controls.
5. Migrate provider-independent modules and Storefront checkout; retire old Anchor dependence only after confirmed continuity, webhooks switched, historical data/retention and emergency rollback.
6. Remove legacy Java modules/dependencies only when no consumers/data remain. Update provider onboarding, accounting, analytics, payouts/financing to new semantics.

## Compliance nuance / official sources
Payment providers may still require KYB/KYC and hold the corresponding responsibility based on account setup. Paystack: https://support.paystack.com/en/articles/2123970 and https://support.paystack.com/en/articles/2125506 . Stripe Connect hosted onboarding: https://stripe.com/connect/onboarding and responsibility: https://support.stripe.com/questions/know-your-customer-%28kyc%29-requirements-for-connected-accounts?locale=en-GB .

AtlasHub must still meet applicable data protection and any remaining operator obligations; do not claim no legal compliance exists. No legal advice here; validate jurisdiction and contracts before launch.
