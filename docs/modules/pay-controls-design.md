# Pay Controls Design (`atlashub-pay:controls`)

## Role and Boundary

`pay:controls` is the execution-side control boundary for financing policies. Financing owns the business policy and facility decision; Pay Controls owns enforcement on AtlasHub ledger accounts, virtual accounts, transfer instructions, and payment-provider capabilities that AtlasHub actually controls.

It can automatically allow, review, hold, block, release, or freeze a controlled payment path. It must never claim to freeze an arbitrary external bank account or initiate recovery/GSI/collateral enforcement. A provider capability is checked before any hold/block/freeze action; unsupported rails result in monitoring/alerting, not a false enforcement claim.

**Gradle module:** `atlashub-pay:controls`  
**Root package:** `com.atlashub.pay.controls`

## Domain Layer

### Aggregates

| Aggregate | Purpose | Key business methods |
|---|---|---|
| `ControlledFundsPolicy` | Provider-capability-aware materialization of a financing facility policy | `activate`, `suspend`, `replaceRules`, `expire` |
| `PaymentControlDecision` | Immutable decision for one payment/withdrawal/transfer instruction | `allow`, `review`, `hold`, `block`, `freeze`, `release`, `expire` |
| `ControlCase` | Human or lender review of a held/blocked/frozen controlled instruction | `requestEvidence`, `approveRelease`, `rejectRelease`, `close` |

`ControlMode` (`UNRESTRICTED`, `MONITORED`, `PURPOSE_RESTRICTED`, `HYBRID`), `ControlAction` (`ALLOW`, `REVIEW`, `HOLD`, `BLOCK`, `FREEZE`), `ProviderControlCapability`, `PermittedCategory`, `RuleSet`, `PaymentContext`, `ControlReason`, and `ControlStatus` are value objects/enums.

The policy is facility-specific and versioned. A decision references the policy and risk/baseline evidence used at the exact time it is made. Decisions are idempotent by payment instruction and policy version.

### Domain Services

- `FundUsageEvaluationService` evaluates purpose, category, beneficiary, amount, available controlled funds, and facility rules.
- `ProviderCapabilityService` downgrades an enforcement request to the strongest supportable action; it never fabricates a freeze.
- `ControlEscalationService` combines Financing anomaly signals with the configured policy to determine a review/hold/block/freeze outcome.

## Application Layer

### Commands

| Command | Response |
|---|---|
| `ActivateControlledFundsPolicy` | `void` |
| `UpdateControlledFundsPolicy` | `void` |
| `EvaluatePaymentInstruction` | `PaymentControlDecisionResult` |
| `ApplyPaymentControlDecision` | `void` |
| `ReleaseControlledPayment` | `void` |
| `FreezeControlledFunds` | `void` |
| `UnfreezeControlledFunds` | `void` |
| `OpenControlCase` | `ControlCaseResult` |
| `ResolveControlCase` | `void` |

`EvaluatePaymentInstruction` is invoked before a supported Pay transfer/withdrawal is submitted to a provider. Provider callbacks and Financing risk events use listener-triggered commands only.

### Queries

| Query | Return |
|---|---|
| `GetActiveControlledFundsPolicy` | single `ControlledFundsPolicyResult` |
| `GetPaymentControlDecision` | single `PaymentControlDecisionResult` |
| `ListControlCases` | `PageResult<ControlCaseResult>` |
| `ListControlDecisions` | `PageResult<PaymentControlDecisionResult>` |

## Enforcement Flow

```
FinancingFacilityActivated / FundUsagePolicyConfigured
  → Pay Controls activates a capability-aware policy

Payout or controlled withdrawal requested
  → EvaluatePaymentInstruction
  → ALLOW: Pay transfer proceeds
  → REVIEW/HOLD: instruction is retained pending evidence/review
  → BLOCK: Pay refuses this AtlasHub-controlled instruction
  → FREEZE: only if ledger/virtual-account/provider capability confirms it
  → decision/control event + Financing monitoring update + audit log
```

An existing external account outside the configured provider's control cannot be frozen. Provider webhooks reconcile whether the requested hold/freeze succeeded, failed, expired, or needs manual action.

## Ports, Events, Security

### Ports

- `ProviderPaymentControlPort`: normalized activate-hold, release-hold, block instruction, and freeze/unfreeze operations.
- `ControlledLedgerPort`: freezes/releases eligible AtlasHub internal ledger balance subject to facility policy.
- `FinancingPolicyQueryPort`: retrieves a facility's approved policy through a shared read model; it does not import Financing internals.

### Events

Publishes `ControlledFundsPolicyActivated`, `PaymentControlEvaluated`, `PaymentHeld`, `PaymentBlocked`, `ControlledFundsFrozen`, `ControlledFundsUnfrozen`, and `ControlCaseResolved`. Consumes `FinancingFacilityActivated`, `FundUsagePolicyConfigured`, `FinancingAnomalyDetected`, `PayoutRequested`, and provider control callbacks.

Every decision is audit logged with policy version, request context, provider capability, action, actor/system source, correlation ID, and redacted evidence. Lender notification follows consent/policy rules; a control event alone is never a debt-recovery action.

## Presentation

Merchant endpoints expose the active policy, a held instruction and required evidence. Lender/operations endpoints expose only facilities for which a lender has permission. No endpoint exposes raw provider credentials, unrelated account details, or a cross-organization facility.

| Method | Path | Permission |
|---|---|---|
| `GET` | `/api/v1/pay/controls/policies/{facilityId}` | `financing:facilities:read` |
| `GET` | `/api/v1/pay/controls/decisions` | `financing:monitoring:read` |
| `POST` | `/api/v1/pay/controls/cases/{id}/evidence` | `financing:controls:respond` |
| `POST` | `/api/v1/pay/controls/cases/{id}/resolve` | `financing:controls:review` |

## Detailed Domain Model

### `ControlledFundsPolicy` (Aggregate Root)

```
ControlledFundsPolicy
├── id: Long
├── facilityId: Long
├── organizationId: Long
├── environment: Environment
├── policyVersion: String
├── mode: ControlMode
├── permittedCategories: Set<PermittedCategory>
├── restrictedCategories: Set<RestrictedCategory>
├── beneficiaryRules: BeneficiaryRuleSet
├── withdrawalRules: WithdrawalRuleSet
├── amountAndFrequencyRules: AmountFrequencyRuleSet
├── providerCapabilities: Set<ProviderControlCapability>
├── status: PolicyStatus               ← DRAFT | ACTIVE | SUSPENDED | EXPIRED | REPLACED
├── effectiveFrom: ZonedDateTime
├── effectiveUntil: ZonedDateTime      ← nullable
├── activatedBy: String                ← Financing/lender correlation identity
└── version: Long
```

`activate(...)`, `suspend(...)`, `replaceRules(...)`, `expire(...)`, and `supports(action, paymentContext)` ensure an inactive or unsupported policy cannot be applied. A replacement policy retains the earlier version and decisions for audit reconstruction.

### `PaymentControlDecision` (Aggregate Root)

```
PaymentControlDecision
├── id: Long
├── policyId: Long
├── facilityId: Long
├── organizationId: Long
├── paymentInstructionReference: String
├── paymentRail: PaymentRail
├── paymentContext: PaymentContext
├── evaluatedAction: ControlAction     ← ALLOW | REVIEW | HOLD | BLOCK | FREEZE
├── enforcedAction: ControlAction
├── reasonCodes: Set<ControlReason>
├── riskSignalId: Long                 ← nullable
├── providerControlReference: String   ← nullable
├── status: ControlStatus              ← EVALUATED | PENDING_PROVIDER | APPLIED | FAILED | RELEASED | EXPIRED
├── evaluatedAt: ZonedDateTime
├── appliedAt: ZonedDateTime           ← nullable
└── version: Long
```

`evaluate(...)`, `markProviderPending(...)`, `confirmApplied(...)`, `fail(...)`, `release(...)`, and `expire(...)` ensure the intended policy action and provider-confirmed action cannot be conflated. The unique idempotency key is `(paymentInstructionReference, policyVersion)`.

### `ControlCase` (Aggregate Root)

```
ControlCase
├── id: Long
├── decisionId: Long
├── organizationId: Long
├── status: ControlCaseStatus          ← OPEN | EVIDENCE_REQUESTED | UNDER_REVIEW | RELEASED | REJECTED | CLOSED
├── requestedEvidence: List<EvidenceRequirement>
├── submittedEvidenceRefs: List<String>
├── openedAt: ZonedDateTime
├── resolvedBy: Long                   ← nullable
├── resolvedAt: ZonedDateTime          ← nullable
├── resolutionReason: String           ← nullable
└── version: Long
```

The case supports a normal business-purpose/evidence review. Resolving it releases or maintains the current supported control; it does not collect a debt, seize assets or decide the lender's underwriting outcome.

## Policy Semantics

| Mode | Normal handling | Escalated anomaly handling |
|---|---|---|
| `UNRESTRICTED` | Allow payments and monitor if required by product. | Notify/record only unless a separate supported control is activated. |
| `MONITORED` | Allow and build behaviour evidence. | Open review/notify; no block absent explicit policy rule. |
| `PURPOSE_RESTRICTED` | Permit configured business categories/beneficiaries only. | Hold/block/freeze on supported rails when a rule is breached. |
| `HYBRID` | Permit priority business spend and bounded withdrawals/miscellaneous expense. | Apply configured threshold, beneficiary and deviation rules. |

Rules are explainable: category match, beneficiary approval, amount limit, amount-to-baseline ratio, frequency, available controlled-fund balance, purpose match and provider capability. An anomaly alone does not imply fraud or automatically trigger a freeze; the active facility policy and rail capability determine the action.

## Payment-Rail Capability Matrix

| Rail/control surface | Allow/review | Hold/block an instruction | Freeze/release funds | Notes |
|---|---|---|---|---|
| AtlasHub internal ledger balance | Yes | Yes | Yes, with ledger reservation/audit | Strongest AtlasHub control. |
| AtlasHub initiated transfer before provider submission | Yes | Yes | Not applicable after submission | Block prevents the outgoing instruction. |
| AtlasHub virtual account/provider subaccount | Provider-specific | Provider-specific | Only if provider explicitly supports it | Callback reconciliation required. |
| External account not controlled by AtlasHub/provider | Monitor only | No | No | Never represent monitoring as a freeze. |

`ProviderCapabilityService` stores/version-controls this capability matrix per provider/product/environment. A requested `FREEZE` that is unsupported is converted to the highest documented safe outcome (for example `HOLD` before submission or `REVIEW`) and the decision records both actions and the downgrade reason.

## Application Command Flows

### Activate a Policy

```
FinancingFacilityActivated / FundUsagePolicyConfigured
  → ActivateControlledFundsPolicyHandler
  → validate facility/policy version and provider capability
  → persist ACTIVE policy
  → request provider activation only where needed
  → ControlledFundsPolicyActivated event
```

### Evaluate a Payment

```
PayoutRequested / controlled withdrawal request
  → EvaluatePaymentInstructionHandler
  → load active policy using facility/funds attribution
  → evaluate category, purpose, beneficiary, thresholds and risk signals
  → persist immutable PaymentControlDecision
  → ALLOW: release Pay instruction
  → REVIEW/HOLD/BLOCK/FREEZE: call supported port or retain instruction
  → publish decision and applied/failed outcome
```

The transfer/payout aggregate remains owned by its Pay module. Pay Controls never mutates its repository directly; it reacts through a listener/handler/event contract. A provider webhook is the authority that a remote hold or freeze has actually happened.

## MySQL Persistence and Idempotency

| Table | Required constraint/index |
|---|---|
| `pay_controlled_funds_policies` | unique `(facility_id, policy_version)`; `(organization_id, status)` index |
| `pay_payment_control_decisions` | unique `(payment_instruction_reference, policy_version)`; provider reference lookup |
| `pay_control_cases` | unique `decision_id`; `(organization_id, status, opened_at)` index |
| `pay_control_provider_callbacks` | unique `(provider, provider_event_id)` for inbox reconciliation |
| `pay_control_audit_entries` | `(organization_id, occurred_at)` and `(facility_id, occurred_at)` indexes |

All policy/decision/case aggregates use optimistic locking. MySQL transactions persist the aggregate and outbox message together. Provider callback consumers use the shared inbox/event-delivery tracker before invoking a command.

## Provider Adapters and Failure Handling

`ProviderPaymentControlPort` maps the normalised actions to a provider's actual API/webhook model. It enforces timeout, correlation/reference, secret handling, idempotency, signature validation and safe retry rules. Provider models, account identifiers and credentials remain in infrastructure.

If a provider control call times out, the decision becomes `PENDING_PROVIDER`; it is not represented as a successful block or freeze. `ControlExpiryScheduler` reconciles outstanding actions, expires held instructions according to policy, and delegates any change to a command handler. Failure notifications describe the actual enforcement state to both merchant and authorized lender users.

## Presentation, Authorization and Audit

`SubmitControlEvidenceRequest` contains only evidence references/description for the caller's own control case. It cannot choose action, facility, organization or lender. Review endpoints require a distinct authorized reviewer when the lender/product policy requires maker-checker; self-approval rules remain inside the relevant domain aggregate.

Audit evidence includes policy/rule version, payment context hash, evaluated/enforced action, rule/capability reasons, actor/system, provider request/reference, callbacks and every release/review resolution. Sensitive bank/payment data is redacted outside encrypted records.

## Test and Verification Plan

- Unit-test each control mode, category/beneficiary/threshold rules, capability downgrade, aggregate transitions, release/expiry and anomaly-to-control escalation.
- Integration-test MySQL idempotency uniqueness, optimistic locks, outbox/inbox processing, ledger reservation, transfer pre-submission blocking, and provider callback reconciliation.
- Contract-test each provider capability/action adapter and timeout/failure mapping with fixtures.
- Security-test tenant/environment isolation, unauthorized evidence/case access, falsified provider callbacks, redacted logs, and that unsupported external accounts never report a freeze.
- End-to-end-test ordinary allowed spend, a review case, a blocked transfer and a supported controlled-funds freeze/release with Financing monitoring events.

## Complete Planned Artifact List

```
atlashub-pay/controls/src/main/java/com/atlashub/pay/controls/
├── domain/
│   ├── entities/ [ControlledFundsPolicy, PaymentControlDecision, ControlCase]
│   ├── valueobject/ [ControlMode, ControlAction, ProviderControlCapability, PermittedCategory, RuleSet, PaymentContext, ControlReason, ControlStatus]
│   ├── services/ [FundUsageEvaluationService, ProviderCapabilityService, ControlEscalationService]
│   ├── ports/ [ProviderPaymentControlPort, ControlledLedgerPort, FinancingPolicyQueryPort]
│   ├── repositories/ [ControlledFundsPolicyRepository, PaymentControlDecisionRepository, ControlCaseRepository]
│   ├── events/ [ControlledFundsPolicyActivated, PaymentControlEvaluated, PaymentHeld, PaymentBlocked, ControlledFundsFrozen, ControlledFundsUnfrozen, ControlCaseResolved]
│   └── exceptions/ [PolicyNotActiveException, UnsupportedControlCapabilityException, InvalidControlTransitionException, ControlCaseNotFoundException]
├── application/
│   ├── commands/ [ActivateControlledFundsPolicy, UpdateControlledFundsPolicy, EvaluatePaymentInstruction, ApplyPaymentControlDecision, ReleaseControlledPayment, FreezeControlledFunds, UnfreezeControlledFunds, OpenControlCase, ResolveControlCase]
│   └── queries/ [GetActiveControlledFundsPolicy, GetPaymentControlDecision, ListControlCases, ListControlDecisions]
├── infrastructure/
│   ├── provider/adapters/ [provider-specific payment-control adapters]
│   ├── messaging/listeners/ [FinancingFacilityListener, FinancingAnomalyListener, PayoutRequestedListener, ProviderControlWebhookListener]
│   ├── messaging/schedulers/ [ControlExpiryScheduler]
│   └── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [ControlledFundsPolicyResponse, PaymentControlDecisionResponse, SubmitControlEvidenceRequest, ControlCaseResponse]
    └── rest/ [PaymentControlController]
```
