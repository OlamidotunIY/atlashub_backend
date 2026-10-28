# Architecture proposal: merchant-connected integrations and global operations

Status: IMPLEMENTED CORE — authentication separation, compliance removal, provider catalog/connections, Paystack and Stripe charge routing, and connection-scoped Paystack webhooks are in the Java backend. Additional provider operations continue through the same adapter boundary.
Date: 2026-10-24
Scope: AtlasHub backend (Java/Spring Boot modular monolith).
Related: [User/Organization separation](./identity-organization-separation.md), [Anchor retirement](./provider-banking-decommission.md), [AI command center](./ai-command-center-voice-security-proposal.md), [storefront platform](./storefront-theme-platform-proposal.md), [delivery plan](./global-expansion-delivery-plan.md).

## Individual vs business vs provider lifecycle

Identity signup creates global User/AuthAccount without organization; a zero-organization user can authenticate and become independent template creator. An existing authenticated User may create an Organization separately with OWNER IAM membership. No default Atlas bank account or compulsory Anchor 5-step KYB, no universal compliance approval gate. Business/provider capabilities follow separate contract/verification requirements. Provider KYB/KYC, privacy/data protection, tax and any actual regulatory duties still apply where required. [Details](./identity-organization-separation.md), [decommission checklist](./provider-banking-decommission.md).

## Product boundary
AtlasHub owns business identity, authorization, commerce, product catalog, inventory, fulfillment orders, HR, general-ledger accounting, audit, and normalized payment/shipping status. Merchants own provider accounts and control receipt of merchant funds. Providers fulfill external services through merchant-authorized integrations. AtlasHub does **not** claim to hold the merchant's external balances, settle their funds, control arbitrary bank accounts, or guarantee provider availability.

Global signup does not promise universal local tax, payroll, banking, shipping or compliance support. Separate country availability from feature eligibility and legal-entity readiness.

## Verified current implementation constraints
- `atlashub-shared/.../domain/valueobject/Country.java` accepts only `NG`; `CurrencyCode.java` contains only `NGN`.
- `RegisterOrganizationHandler` derives currency from registration country, and `Organization.validateRequired` requires equality.
- `CreateOutletHandler` hardwires the organization's country and base currency into new outlets; `CreateOutletRequest` has no country field.
- `EnablePaymentCapabilityHandler` maps BANKING and BANK_TRANSFER_COLLECTION to Anchor and CARD/USSD to Paystack.
- `PaymentProviderProfileQueryPortAdapter` returns the first matching active profile; not suitable for multiple-eligible-provider routing.
- `ChargeProviderPort` is not parameterized by integration/connected credentials; `InitializeChargeHandler` uses an external merchant identifier, so the boundary requires redesign.
- `ProvisionOrganizationBankingHandler`, `IssueReservedAccountHandler`, `ReservedAccount`, and `PollPaystackSettlementsHandler` carry Anchor/Paystack-specific data assumptions.
- `ExecutePayoutHandler` marks payouts completed with a MOCK reference when no payout gateway bean exists. Fail closed in production.
- `atlashub-logistics:shipping` models internal fleets/coverage and two route types INTRA_STATE / INTER_STATE; `CalculateShippingQuotesHandler` determines routes by comparing state names. `Partner3PLCarrierAdapter` generates simulated quotes/bookings/tracking; it is not a verified commercial carrier integration.
- Accounting GL designs document reports and multiple business-event listeners that do not match registered GL implementation handlers; see the delivery plan for the gap list.

## Gradle modular-monolith proposal
Add `atlashub-integrations` with `registry`, `connections`, `routing` and `webhooks` bounded contexts. Provider adapters remain behind ports and may initially live under `atlashub-infrastructure`. Do not split into independent services before a demonstrated deployment need.

### Minimum domain concepts
- IntegrationProvider: provider ID, category, supported auth methods, documentation, status, connector version.
- ProviderCapability: named operation and coverage/limitations, never merely a provider-wide yes/no.
- OrganizationIntegration: org, environment, provider, merchant-managed external account, status, credential reference, ownership and audit.
- IntegrationCapability: per-connection enabled and verified ability, country/currency/limits.
- RoutingPolicy: merchant-configured eligibility, priority, explicit consumer choice and fallback rules.
- IntegrationEvent: verified provider payload reference, delivery timestamp, event ID, origin, idempotency processing state.
- IntegrationHealth: connectivity, token validity, webhook state and last successful synchronization.

All records are organization/environment scoped. Provider secrets must be encrypted at rest using managed keys; never embed secrets in client-visible responses or Kafka payloads. OAuth is preferable where the provider supports it; API-key connection requires explicit scopes, encryption and test validation.

### Payment interface requirements
Replace singular `findActiveProfile` with something like `findEligibleConnections(org, environment, capability, amount, currency, merchantCountry, customerContext)`. Deterministic eligibility precedes merchant routing preference/customer choice. The selected organizationIntegrationId is persisted on each PaymentAttempt. Multiple payment options are possible only where merchant accounts and provider contracts actually support those options; country of customer does not imply merchant eligibility.

Canonical payments use a PaymentIntent/PaymentAttempt split. PaymentAttempt references the selected connection and external provider reference, channel, amount, currency, status, expiration, and idempotency key. Order status changes only after verified provider outcome. Signed webhook processing, trusted server-side verification, de-duplication, reconciliation, and delayed-event handling are mandatory. A timeout or unknown status must not trigger a second potentially charging provider automatically.

Separate externally collected money from internally controlled ledger balances. Preserve finance/accounting/audit events; normalize `PaymentSucceeded`, `PaymentFailed`, `RefundConfirmed`, `SettlementConfirmed`, and `PayoutConfirmed` with event IDs, organization/environment/connection IDs and exact amounts/currencies. Accounting must recognize clearing accounts, fees, chargebacks and settlement discrepancies. Platform subscription billing is separate from merchants' order payments.

### Banking capabilities
Model SETTLEMENT_ACCOUNT_REFERENCE, COLLECTION_VIRTUAL_ACCOUNT, MERCHANT_EMBEDDED_BANKING, PAYOUT and ACCOUNT_VERIFICATION independently. Provisioning is provider/country/account-specific and subject to compliance and explicit scopes. No fallback that invents successful payout/account provisioning.

### Logistics capabilities
Keep `shipping`, `warehousing`, and `returns` as business domains. Decouple from internally operated fleets and delivery-fee splits. Model shipping provider connections, delivery legs, domestic/cross-border/last-mile services, origin/destination country/region/postal code, quotes with expiry, label/book/track/cancel capabilities, customs fields, carrier error mappings and shipment evidence. Support single end-to-end carrier or multiple legs. `ThirdPartyCarrierPort` calls actual tested provider adapters, while simulated adapters remain TEST-only. Existing automatic 10% merchant incentive and AtlasHub-fee split require a separately funded/contracted managed-shipping product or retirement.

### Geography and accounting
Use ISO 3166-1 country and country-specific subdivisions (ISO 3166-2 where available); store address semantics and IANA time zones. Organization registration country, legal entity country, outlet country, store market and customer's delivery country are independent. Distinguish organization reporting currency, outlet operating currency, order currency, settlement currency, and provider fees. Use ISO 4217 currencies and currency-specific minor units; preserve FX snapshots, posting currency and local-equivalent accounting values.

### Availability model
Business capability availability is the intersection of subscription entitlement, user permissions, organization eligibility, geographic/currency/provider eligibility and connection health. No connected provider => external-dependent feature is UNAVAILABLE with a clear setup action; independent commerce, inventory, HR and accounting features continue working. The backend always enforces the capability gate even when the frontend hides a feature.

### Migration
1. Baseline tests and data inventory; document existing Anchor/Paystack and shipping side-effects.
2. Introduce provider-neutral interfaces and normalized events without removing old adapters.
3. Implement per-organization installation, secret storage, verification and capability resolver.
4. Adapt Paystack as the first connected-account implementation and complete webhook reconciliation.
5. Replace hardcoded country/currency and outlet-inheritance assumptions; migrate existing NG records.
6. Implement multi-provider checkout selection and fail-closed payout/account controls.
7. Add additional providers/countries only with verified merchant eligibility and tests.
8. Decommission platform-operated rails and legacy behavior after safe migration and data retention.

No repository code is changed by this document.
