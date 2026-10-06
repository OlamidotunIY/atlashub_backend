# AtlasHub Financing Module Design (`atlashub-financing`)

## Role and Boundaries

AtlasHub Financing connects an eligible business to one or more lending partners using consented, explainable AtlasHub operational data. It owns financing readiness, AtlasScore, application preparation, consent, offers, facility status, monitoring and normal repayment orchestration.

It does **not** lend AtlasHub capital, make a lender's final underwriting decision, perform debt collection/recovery, initiate GSI recovery, enforce collateral, or operate a post-default recovery workflow. Serious-default/recovery status may be recorded when reported by the lender, but responsibility remains with that lender.

**Gradle module:** `atlashub-financing`  
**Root package:** `com.atlashub.financing`

## Domain Layer

### Aggregates and Entities

| Type | Key state | Ownership and rules |
|---|---|---|
| `AtlasScoreAssessment` | organization, score version, score, classification, input/factor snapshot, capacity, calculated time | Immutable after calculation. It is deterministic and reproducible; an LLM cannot create or modify it. |
| `FinancingRequest` | purpose, requested amount/tenor, justification, linked outlets/purchase requirements, status | Owns the request lifecycle and selected offer; it does not own lender data. |
| `FinancingDataConsent` | lender, application, categories, purpose, version, granted/revoked status, timestamps | Explicit before package creation/submission. Revocation prevents future permitted sharing but preserves historical evidence. |
| `FinancingApplication` | lender/product, request, package snapshot/hash, external reference, status history | An immutable lender-facing package is created from consented DTOs, never live operational records. |
| `FinancingOffer` | lender, application, amount, tenor, pricing/terms, expiry, offer status | Multiple offers may exist per request; exactly one accepted offer activates a facility. |
| `FinancingFacility` | accepted offer, disbursement/repayment status, active policy, monitoring status | Owns active-financing lifecycle. It does not collect debt. |
| `FinancingRiskSignal` | baseline snapshot, observed event, deviation, explanation, severity, review state | Anomaly is not fraud. It records evidence and routes a signal to the merchant/lender. |

`FinancingPurpose`, `RequestStatus`, `ApplicationStatus`, `OfferStatus`, `FacilityStatus`, `RiskStatus`, `DataCategory`, `ConsentVersion`, `ScoreVersion`, `ScoreFactor`, `FinancingCapacity`, `MonitoringBaseline`, and `RepaymentArrangement` are value objects/enums.

### Domain Services

- `AtlasScoreCalculationService` applies the approved score version to immutable feature/accounting snapshots and emits factor contributions.
- `FinancingPackageService` builds a lender-specific DTO package from an active consent; it rejects any category not consented.
- `FacilityHealthService` calculates repayment coverage, financing-health classification and early-warning thresholds.
- `AnomalyAssessmentService` evaluates observed payment behaviour against a facility baseline and produces explainable risk signals.

### Domain Errors

`ScoreInputIncompleteException`, `InvalidFinancingRequestStateException`, `ConsentRequiredException`, `ConsentScopeViolationException`, `ApplicationPackageImmutableException`, `OfferExpiredException`, `OfferAlreadySelectedException`, `FacilityStateException`, and `InvalidRiskReviewStateException` are module-owned semantic errors.

## Score and Explainability

An AtlasScore is a deterministic weighted assessment on a 0–1000 range. Each `ScoreVersion` documents: eligible feature definitions, data-quality rules, minimum operating history, normalization/banding, weights, threshold/classification mapping, and capacity-estimation formula. Initial inputs may include revenue consistency/trend, gross margin/profitability, cash-flow stability, inventory turnover/stock-outs, customer retention/concentration, outlet performance, payroll burden, expense growth, and repayment behaviour where available.

The persisted assessment stores the score version, complete input snapshot hash/value set, factor contribution, missing-data treatment, calculation timestamp, and resulting capacity. Every application references that specific assessment. The lender decides underwriting and final approval independently.

## Application Layer

### Commands

| Command | Response |
|---|---|
| `CalculateAtlasScore` | `AtlasScoreAssessmentResult` |
| `CreateFinancingRequest` | `FinancingRequestResult` |
| `GrantFinancingDataConsent` | `FinancingDataConsentResult` |
| `RevokeFinancingDataConsent` | `void` |
| `CreateFinancingApplicationPackage` | `FinancingApplicationResult` |
| `SubmitFinancingApplication` | `void` |
| `RecordLenderApplicationStatus` | `void` |
| `ReceiveFinancingOffer` | `FinancingOfferResult` |
| `AcceptFinancingOffer` / `RejectFinancingOffer` | `void` |
| `RecordDisbursement` | `void` |
| `ConfigureFundUsagePolicy` | `void` |
| `RecordNormalRepayment` | `void` |
| `AssessFacilityHealth` | `FinancingRiskSignalResult` |
| `AcknowledgeFinancingRiskSignal` | `void` |

Listener/scheduler-triggered commands return `void` where their result is persisted and published. User-triggered commands obtain organization/user context only from `AuthenticatedPrincipal`.

### Queries

| Query | Return |
|---|---|
| `GetLatestAtlasScore` | single `AtlasScoreAssessmentResult` |
| `ListAtlasScoreHistory` | `PageResult<AtlasScoreAssessmentResult>` |
| `GetFinancingRequest` | single `FinancingRequestResult` |
| `ListFinancingRequests` | `PageResult<FinancingRequestSummaryResult>` |
| `ListFinancingOffers` | `List<FinancingOfferResult>` |
| `GetFinancingFacility` | single `FinancingFacilityResult` |
| `ListFinancingRiskSignals` | `PageResult<FinancingRiskSignalResult>` |
| `GetFinancingConsent` | single `FinancingDataConsentResult` |

## Lifecycle

```
DRAFT → REQUESTED → CONSENT_PENDING → READY_TO_SUBMIT → SUBMITTED
      → INFORMATION_REQUIRED → READY_TO_SUBMIT
      → OFFERED → ACCEPTED → DISBURSEMENT_PENDING → ACTIVE → PAID_OFF | CLOSED
      → DECLINED | CANCELLED | EXPIRED
```

Only an active consent permits a package snapshot. Only the merchant may accept an unexpired offer. Facility monitoring starts only after activation/disbursement. `SERIOUS_DEFAULT` is an externally reported informational state that stops AtlasHub's normal monitoring/repayment orchestration and hands recovery responsibility to the lender.

## Ports, Integrations, and Events

### Module-Owned Ports

- `LendingPartnerPort`: submit package, retrieve normalized status/offer, receive lender webhook reconciliation.
- `RepaymentPort`: create/cancel normal repayment instruction on a supported rail.
- `FinancingPaymentControlPort`: request policy activation/release for a facility; Pay decides what it can enforce.
- `FinancingFeatureQueryPort`: read consent-safe Analytics/Accounting feature snapshots through a shared port implementation.

Partner API models exist only in the infrastructure adapter. A demo adapter may deterministically return an offer but uses the same port and package shape as production.

### Published Events

`AtlasScoreCalculated`, `AtlasScoreChanged`, `FinancingRequestCreated`, `FinancingConsentGranted`, `FinancingConsentRevoked`, `FinancingApplicationSubmitted`, `FinancingApplicationStatusChanged`, `FinancingOfferReceived`, `FinancingOfferAccepted`, `FinancingFacilityActivated`, `FinancingRepaymentRecorded`, `FinancingRiskChanged`, and `FinancingAnomalyDetected` are outbox-backed events.

The module consumes Analytics feature-ready events, Accounting financial-close/adjustment events, supported Pay transaction/control events, and lender webhooks. All listeners use inbox idempotency and delegate exclusively to a handler.

## Consent, Privacy, and Audit

Consent is granular by lender, application, data category, purpose, version and time. Categories include business/KYB, commerce, inventory, customers, financial, and financing data. The application package stores the exact normalized DTOs transmitted, a hash, recipient and transmission time. It never exposes a live database view or unrestricted customer data.

Every score, package, lender transition, offer, policy change, control decision, anomaly, acknowledgement and repayment instruction is audit logged with actor/system identity, correlation ID, immutable snapshot references and redacted sensitive fields.

## Presentation

| Method | Path | Permission | Purpose |
|---|---|---|---|
| `GET` | `/api/v1/financing/score` | `financing:score:read` | Latest explainable score. |
| `POST` | `/api/v1/financing/requests` | `financing:requests:create` | Create a financing request. |
| `POST` | `/api/v1/financing/requests/{id}/consents` | `financing:consent:manage` | Grant explicit lender/category consent. |
| `POST` | `/api/v1/financing/requests/{id}/submit` | `financing:requests:submit` | Create/submit the consented package. |
| `GET` | `/api/v1/financing/requests/{id}/offers` | `financing:offers:read` | List offers. |
| `POST` | `/api/v1/financing/offers/{id}/accept` | `financing:offers:accept` | Accept an offer. |
| `GET` | `/api/v1/financing/facilities/{id}` | `financing:facilities:read` | Facility, policy, repayment and health. |
| `GET` | `/api/v1/financing/risk-signals` | `financing:monitoring:read` | Paginated risk/anomaly signals. |

Separate lender-facing endpoints authenticate partner credentials and are scoped to the lender's own applications. They never expose an organization that has not consented.

## Detailed Domain Model

### `AtlasScoreAssessment` (Aggregate Root)

```
AtlasScoreAssessment
├── id: Long
├── organizationId: Long
├── environment: Environment
├── scoreVersion: String
├── featureSnapshotId: Long
├── featureSnapshotHash: String
├── score: Integer                      ← 0..1000
├── classification: ScoreClassification ← STRONG | HEALTHY | WATCH | ELEVATED_RISK | INSUFFICIENT_DATA
├── financingCapacity: Money            ← nullable where data is insufficient
├── factors: List<ScoreFactorContribution>
├── dataQuality: ScoreDataQuality
├── calculatedAt: ZonedDateTime
└── createdAt: ZonedDateTime
```

`calculate(...)` validates the feature-schema version, data window, score bounds and factor totals before registering `AtlasScoreCalculated`. There is no `updateScore` method. A revised score creates a new assessment, emits `AtlasScoreChanged` where applicable, and preserves all prior assessments.

### `FinancingRequest` (Aggregate Root)

```
FinancingRequest
├── id: Long
├── organizationId: Long
├── environment: Environment
├── requestedAmount: Money
├── purpose: FinancingPurpose
├── requestedTenorMonths: Integer       ← nullable where product permits
├── businessJustification: String
├── relatedOutletIds: Set<Long>
├── relatedPurchaseRequirements: List<PurchaseRequirement>
├── supportingDocumentRefs: List<String>
├── scoreAssessmentId: Long
├── status: RequestStatus
├── selectedOfferId: Long               ← nullable
├── createdBy: Long
├── submittedAt: ZonedDateTime          ← nullable
├── cancelledAt: ZonedDateTime          ← nullable
└── version: Long
```

`request()`, `markConsentPending()`, `markReadyToSubmit()`, `markSubmitted()`, `requireInformation(...)`, `recordOffer(...)`, `selectOffer(...)`, `decline(...)`, `cancel(...)`, and `expire(...)` enforce the lifecycle. The request cannot submit without an eligible score snapshot, an active lender-specific consent, and a complete package.

### `FinancingDataConsent` (Aggregate Root)

```
FinancingDataConsent
├── id: Long
├── organizationId: Long
├── financingRequestId: Long
├── lenderId: Long
├── purpose: FinancingPurpose
├── dataCategories: Set<DataCategory>
├── consentVersion: String
├── status: ConsentStatus               ← DRAFT | GRANTED | REVOKED | EXPIRED
├── grantedBy: Long
├── grantedAt: ZonedDateTime
├── revokedBy: Long                     ← nullable
├── revokedAt: ZonedDateTime            ← nullable
├── revocationReason: String            ← nullable
└── version: Long
```

`grant(...)`, `revoke(...)`, and `expire(...)` make the consent state explicit. A consent cannot be silently broadened; changed categories, recipient, purpose or consent text require a new consent version. Revocation stops future permitted disclosure but does not destroy a package already lawfully transmitted or audit evidence that must be retained.

### `FinancingApplication`, `FinancingOffer`, and `FinancingFacility`

```
FinancingApplication                 FinancingOffer                  FinancingFacility
├── lenderId                          ├── applicationId               ├── offerId
├── financingProductId                ├── lenderOfferReference        ├── organizationId
├── requestId                         ├── offeredAmount                ├── status
├── consentId                         ├── tenorMonths                  ├── disbursedAmount
├── packageSnapshotJson/hash          ├── pricingAndRepaymentTerms     ├── outstandingAmount
├── lenderApplicationReference        ├── expiresAt                     ├── fundUsagePolicyVersion
├── status/history                    ├── status                        ├── monitoringStatus
└── submittedAt                       └── acceptedAt                    └── activatedAt
```

An application snapshot is immutable once submitted. An offer may be accepted only before expiry and only when it belongs to the request's organization. Accepting one offer atomically marks competing offers unavailable. A facility activates after a recorded lender disbursement/activation event; it is not inferred merely from offer acceptance.

### `FinancingRiskSignal` (Aggregate Root)

```
FinancingRiskSignal
├── id: Long
├── facilityId: Long
├── organizationId: Long
├── type: RiskSignalType               ← REVENUE_DROP | MARGIN_DETERIORATION | CASH_FLOW_CHANGE | INVENTORY_DECELERATION | PAYMENT_ANOMALY | REPAYMENT_COVERAGE
├── severity: RiskSeverity             ← INFO | WATCH | ELEVATED | CRITICAL
├── baselineSnapshot: MonitoringBaseline
├── observedSnapshot: ObservedBehaviour
├── explanation: String
├── recommendedAction: String
├── status: RiskSignalStatus           ← OPEN | ACKNOWLEDGED | UNDER_REVIEW | RESOLVED
├── detectedAt: ZonedDateTime
├── acknowledgedBy: Long               ← nullable
└── version: Long
```

`detect(...)`, `acknowledge(...)`, `startReview()`, and `resolve(...)` make a signal auditable. `PAYMENT_ANOMALY` means an explainable deviation from baseline, **not fraud**. It can request evidence, trigger a control evaluation, or notify a lender; it cannot initiate collection or recovery.

## AtlasScore Calculation Contract

### Initial Score Version

`AtlasScore-v1` is a documented deterministic model. Its documentation must publish the input definition, look-back window, missing-data rule, normalisation/bands, weights, classification thresholds and capacity formula before it is used. Illustrative inputs are:

| Dimension | Example evidence | Initial treatment |
|---|---|---|
| Revenue consistency/trend | daily/monthly sales projection | score stable growth and volatility |
| Profitability and margin | Accounting financial-health projection | penalise sustained margin deterioration |
| Cash-flow stability | reconciled cash-flow and ledger indicators | score volatility and available coverage |
| Inventory health | turnover, stock-outs, dead stock | score efficiency and deterioration |
| Customer quality | retention/repeat rate/concentration | score sustainable demand |
| Operating burden | payroll burden, OPEX growth | penalise unhealthy growth |
| Facility history | normal repayment behaviour where available | add only after a documented data-quality threshold |

Capacity is an explainable indicative support estimate—not an approved facility, promise, lender decision or credit-bureau score. The response must label it accordingly.

### Scoring and Data Failure Rules

- An insufficient-history/quality result is valid and must return `INSUFFICIENT_DATA`, not a guessed score.
- The score version and every raw/derived input used are persisted before a package is built.
- A manual override requires an authorized, separately audited score-version release; it cannot modify one historical assessment.
- A lender cannot alter an AtlasScore result through its integration adapter.

## Lender Integration and Webhooks

### Lender Product Model

`LendingPartner` and `LendingProduct` are provider-normalized reference entities. A product declares supported purposes, amount/tenor ranges, documentation requirements, data-category requirements, normal repayment methods, policy/control capabilities, callback requirements and version/effective dates. No core class is named for a specific bank.

### Submission and Reconciliation

```
CreateFinancingApplicationPackage
  → validate active consent and score/input snapshot
  → produce lender-specific normalized DTO package + hash
  → submit through LendingPartnerPort with idempotency/correlation key
  → persist external reference/status

Lender webhook
  → validate signature and partner event ID
  → inbox deduplicate
  → RecordLenderApplicationStatusHandler / ReceiveFinancingOfferHandler
  → update aggregate and outbox event
```

Unrecognized lender references are quarantined for operational reconciliation; a webhook never creates a new request, consent or offer. Provider failure records a retryable status without inventing a decline.

## Monitoring, Repayment, and Controls

Facility health is calculated over explicit windows and records the source snapshot. Initial signals include revenue/volume drop, margin deterioration, rising expense/payroll burden, slow stock, customer decline, repayment coverage change, abnormal withdrawal amount/frequency/beneficiary and purpose divergence.

Normal repayment is limited to lender-authorized, customer-authorized mechanisms exposed through `RepaymentPort`: scheduled repayment, merchant-initiated repayment, direct debit where valid, early repayment, or percentage-of-sales repayment where contractually supported. A failed normal repayment creates a monitoring signal/status; it does not start a recovery workflow.

`ConfigureFundUsagePolicy` stores the Financing-owned policy version and publishes it to `pay:controls`. Pay Controls evaluates/acts only on payment rails it controls and reports an immutable decision. The lender policy may request `ALLOW`, `REVIEW`, `HOLD`, `BLOCK`, or `FREEZE`; the actual action is capability-aware and never treated as arbitrary external account freezing.

## MySQL Persistence and Indexes

| Table | Required indexes/constraints |
|---|---|
| `financing_score_assessments` | `(organization_id, environment, calculated_at)`; unique snapshot/version where appropriate |
| `financing_requests` | `(organization_id, environment, status, created_at)`; optimistic `version` |
| `financing_consents` | `(financing_request_id, lender_id, status)`; consent-version uniqueness |
| `financing_applications` | unique lender application reference; `(organization_id, status, submitted_at)` |
| `financing_offers` | `(application_id, status, expires_at)` |
| `financing_facilities` | unique accepted offer; `(organization_id, status, activated_at)` |
| `financing_risk_signals` | `(facility_id, status, detected_at)` and `(organization_id, severity, detected_at)` |
| `financing_repayment_records` | unique provider repayment reference; facility/date index |

Sensitive consent content, lender packages and supporting-document references are encrypted or stored as private object references. Lender-facing DTOs are persisted separately from internal aggregate fields and are redacted in logs, metrics and exception messages.

## Presentation, Authorization, and Notifications

| Permission | Grants |
|---|---|
| `financing:score:read` | Read own-organization score and factor explanation. |
| `financing:requests:create` / `financing:requests:submit` | Create and submit an organization's request. |
| `financing:consent:manage` | Grant/revoke lender-specific data sharing. |
| `financing:offers:read` / `financing:offers:accept` | View/act on own organization's offers. |
| `financing:facilities:read` / `financing:monitoring:read` | View facility, policy and risk signals. |
| `financing:controls:respond` | Supply evidence for a held payment/control case. |

Notifications are event-driven. The merchant receives score changes, consent confirmations, request status, offers, policy activation, anomalies and growth recommendations. A lender receives only application/facility signals permitted by the active consent and product agreement.

## Test and Verification Plan

- Unit-test score bands/weights, missing-data outcomes, capacity rules, all aggregate transitions, consent narrowing/revocation, offer selection/expiry, monitoring thresholds and non-fraud anomaly language.
- Integration-test MySQL snapshots/indexes/optimistic locks, encrypted package persistence, Kafka inbox/outbox behaviour, lender webhook signature/deduplication, adapter idempotency and payment-control event handling.
- Security-test cross-organization request/offer/consent IDs, environment separation, lender-scoped endpoints, category leakage, revoked consent, redaction and audit completeness.
- End-to-end-test the Ade Stores flow from historical events through score, consent, demo lender offer, facility activation, anomalous withdrawal and grounded merchant/lender outcomes.

## Event Payloads and Choreography

| Event | Required payload | Primary consumers |
|---|---|---|
| `AtlasScoreCalculated` | assessment ID, org/environment, score version, score/classification, feature snapshot ID, capacity, data quality | Intelligence, request UI, audit |
| `FinancingConsentGranted` | consent ID, request/lender IDs, categories, version, granted time | application-package preparation |
| `FinancingApplicationSubmitted` | application ID, lender/product, package hash, external reference, submitted time | lender adapter, notifications |
| `FinancingOfferReceived` | offer ID, application ID, amount, tenor, terms hash, expiry | merchant UI, notifications |
| `FinancingFacilityActivated` | facility ID, org/environment, offer ID, disbursement reference, policy version | Pay Controls, monitoring, Intelligence |
| `FinancingAnomalyDetected` | signal/facility ID, type, severity, baseline/observed hashes, recommended action | Pay Controls, Intelligence, authorized lender notification |
| `FinancingRepaymentRecorded` | facility ID, amount, status, provider reference, received time | Accounting, Analytics, monitoring |

Each payload is a published-language DTO, not a domain entity. It includes only consent-safe identifiers/summary values. The receiver loads its own authorized read model or snapshot if it requires further detail. Outbox publication and inbox deduplication are mandatory.

### Application Submission Saga

```
Merchant creates request
  → FinancingRequestCreated
  → CalculateAtlasScore using immutable Analytics feature snapshot
  → Merchant grants lender/category-specific consent
  → FinancingConsentGranted
  → CreateFinancingApplicationPackage
  → FinancingApplicationSubmitted
  → LendingPartnerPort submits/reconciles application
  → lender webhook/status event
  → FinancingOfferReceived | FinancingApplicationStatusChanged
  → merchant accepts one offer
  → FinancingOfferAccepted
  → lender disbursement confirmation
  → FinancingFacilityActivated
  → Pay Controls activates policy; monitoring/intelligence begin
```

The saga has no distributed transaction. Each step is idempotent and has a visible pending/error state. Failure to submit to a partner does not revoke consent or discard a package; it records retry/reconciliation status. Cancelling an unsubmitted request disables its active consent/pending application workflow without deleting audit history.

## Fund-Usage Policy Model

Financing owns the business representation of a `FundUsagePolicy`: mode, permitted/restricted categories, approved-beneficiary rules, amount/frequency thresholds, withdrawal allowance, evidence requirements, escalation actions, effective dates and lender/policy version. It is attached to a facility, not a general organization. `pay:controls` receives a normalized policy event and maps it to provider/ledger capabilities.

The policy response always reports the distinction between requested action and enforceable action. For example, a policy may require `FREEZE` after an unusually large withdrawal, while a transfer that has already settled on an unsupported external rail can only become `REVIEW` plus a lender alert. This is an enforcement-capability fact, not an application failure.

## Serious-Default Boundary

`FacilityStatus` may retain `SERIOUS_DEFAULT_REPORTED` when a lender supplies that status. At that point Financing stops normal repayment scheduling and facility-health reminders according to partner contract, marks the facility's monitoring handoff, and continues to expose only authorized historical status/evidence. It does not create commands, ports, listeners, endpoints, or background jobs for collection, debt recovery, GSI, collateral enforcement, seizure, or legal action.

## Hackathon Fixture Requirements

The demo fixture includes a deterministic score configuration, explicit score factors, months of MySQL-projected retail/accounting history, a seedable behaviour baseline, a consent document version/category checklist, one `DemoLendingPartnerAdapter`, one product, one offer, and a facility policy. The mock adapter simulates lender approval/offer only; it must not bypass local request, consent, package, offer, facility or audit transitions.

## Complete Planned Artifact List

```
atlashub-financing/src/main/java/com/atlashub/financing/
├── domain/
│   ├── entities/ [AtlasScoreAssessment, FinancingRequest, FinancingDataConsent, FinancingApplication, FinancingOffer, FinancingFacility, FinancingRiskSignal]
│   ├── valueobject/ [FinancingPurpose, RequestStatus, ApplicationStatus, OfferStatus, FacilityStatus, RiskStatus, DataCategory, ConsentVersion, ScoreVersion, ScoreFactor, FinancingCapacity, MonitoringBaseline, RepaymentArrangement]
│   ├── services/ [AtlasScoreCalculationService, FinancingPackageService, FacilityHealthService, AnomalyAssessmentService]
│   ├── ports/ [LendingPartnerPort, RepaymentPort, FinancingPaymentControlPort, FinancingFeatureQueryPort]
│   ├── repositories/ [AtlasScoreAssessmentRepository, FinancingRequestRepository, FinancingConsentRepository, FinancingApplicationRepository, FinancingOfferRepository, FinancingFacilityRepository, FinancingRiskSignalRepository]
│   ├── events/ [AtlasScoreCalculated, AtlasScoreChanged, FinancingRequestCreated, FinancingConsentGranted, FinancingConsentRevoked, FinancingApplicationSubmitted, FinancingApplicationStatusChanged, FinancingOfferReceived, FinancingOfferAccepted, FinancingFacilityActivated, FinancingRepaymentRecorded, FinancingRiskChanged, FinancingAnomalyDetected]
│   └── exceptions/ [ScoreInputIncompleteException, InvalidFinancingRequestStateException, ConsentRequiredException, ConsentScopeViolationException, ApplicationPackageImmutableException, OfferExpiredException, OfferAlreadySelectedException, FacilityStateException, InvalidRiskReviewStateException]
├── application/
│   ├── commands/ [CalculateAtlasScore, CreateFinancingRequest, GrantFinancingDataConsent, RevokeFinancingDataConsent, CreateFinancingApplicationPackage, SubmitFinancingApplication, RecordLenderApplicationStatus, ReceiveFinancingOffer, AcceptFinancingOffer, RejectFinancingOffer, RecordDisbursement, ConfigureFundUsagePolicy, RecordNormalRepayment, AssessFacilityHealth, AcknowledgeFinancingRiskSignal]
│   └── queries/ [GetLatestAtlasScore, ListAtlasScoreHistory, GetFinancingRequest, ListFinancingRequests, ListFinancingOffers, GetFinancingFacility, ListFinancingRiskSignals, GetFinancingConsent]
├── infrastructure/
│   ├── lender/adapters/ [DemoLendingPartnerAdapter, production lender adapters]
│   ├── repayment/adapters/ [supported Pay repayment adapter]
│   ├── messaging/listeners/ [AnalyticsFeatureListener, PayTransactionListener, PayControlListener, LenderWebhookListener]
│   ├── messaging/schedulers/ [FacilityHealthScheduler, RepaymentReviewScheduler]
│   └── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [CreateFinancingRequestRequest, GrantConsentRequest, OfferResponse, FacilityResponse, RiskSignalResponse]
    └── rest/ [FinancingRequestController, FinancingOfferController, FinancingFacilityController, LenderApplicationController]
```
