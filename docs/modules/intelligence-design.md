# Atlas Intelligence Module Design (`atlashub-intelligence`)

## Role and Boundaries

Atlas Intelligence is AtlasHub's production-grade decision-support and conversational-AI capability. It turns authorized AtlasHub operational and accounting data into explanations, forecasts, recommendations, and structured answers. It does not own sales, inventory, payments, accounting entries, customer records, AtlasScore, financing applications, or lender decisions.

It uses deterministic tool results for authoritative metrics. Statistical/ML services forecast or detect patterns. Generative AI selects authorized tools, explains the returned facts, and communicates uncertainty; it never has database credentials, never generates authoritative financial calculations, and never bypasses organization permissions.

**Gradle module:** `atlashub-intelligence`  
**Root package:** `com.atlashub.intelligence`

## Domain Layer

### Aggregates

| Aggregate | Ownership and essential state | Key business methods |
|---|---|---|
| `IntelligenceConversation` | Organization, requesting user, environment, status, retained messages/turn metadata | `startTurn`, `recordToolInvocation`, `completeTurn`, `failTurn`, `close` |
| `IntelligenceAnalysis` | Organization, analysis type, deterministic input snapshot references, model/prompt versions, output/evidence, status | `begin`, `attachEvidence`, `recordForecast`, `recordRecommendation`, `complete`, `fail` |
| `ModelRelease` | Approved ML model/version, feature schema, evaluation metrics, lifecycle state | `approve`, `deprecate`, `markDrifting` |

`Recommendation`, `EvidenceReference`, `ToolInvocation`, `PromptVersion`, `ModelVersion`, `Forecast`, `Confidence`, `AnalysisType`, and `ConversationStatus` are value objects/enums. Raw prompts and model responses are retained only under a documented retention/redaction policy.

### Domain Services

- `IntelligenceToolPolicyService` resolves allowed tools from the authenticated principal, organization/environment, and requested intent.
- `EvidenceGroundingService` rejects a final answer lacking attributable structured evidence for a factual financial/operational claim.
- `RecommendationPolicyService` ranks deterministic and ML-produced candidate recommendations; a model cannot issue an instruction beyond its permitted recommendation class.

### Domain Errors

`UnauthorizedIntelligenceToolException`, `UnsupportedAnalysisIntentException`, `UngroundedResponseException`, `ModelReleaseUnavailableException`, `CrossOrganizationEvidenceException`, and `ConversationStateException` follow the existing module-owned domain-error convention.

## Application Layer

### Commands

| Command | Response | Notes |
|---|---|---|
| `AskBusinessQuestion` | `IntelligenceAnswerResult` | Starts/continues a conversation, invokes only authorized structured tools, records evidence and response. |
| `GenerateBusinessSummary` | `IntelligenceAnalysisResult` | Generates an auditable periodic business-health analysis. |
| `GenerateGrowthRecommendations` | `IntelligenceAnalysisResult` | Uses performance, inventory, accounting, and forecast evidence. |
| `GenerateFinancingExplanation` | `IntelligenceAnalysisResult` | Explains a provided score/facility/risk signal; it never recalculates the score. |
| `RunForecast` | `ForecastResult` | Invoked by a user or scheduler for an approved model release. |
| `EvaluateModelRelease` | `void` | Records evaluation, data-quality and drift outcomes. |
| `ApproveModelRelease` | `void` | Restricted AtlasHub administrative command; no self-approval where governance requires review. |

Commands take organization/user/environment from `AuthenticatedPrincipal`; they do not accept them as client-controlled context.

### Queries

| Query | Return |
|---|---|
| `GetConversation` | single `ConversationResult` |
| `ListConversations` | `PageResult<ConversationSummaryResult>` |
| `GetIntelligenceAnalysis` | single `IntelligenceAnalysisResult` |
| `ListRecommendations` | `PageResult<RecommendationResult>` |
| `GetForecast` | single `ForecastResult` |
| `ListModelReleases` | `List<ModelReleaseResult>` |

## Controlled Tool Contracts

The module calls shared read-only ports. Each call is evaluated against the current organization, environment and permission set. Initial tool families are:

| Tool family | Owning source | Uses |
|---|---|---|
| `BusinessPerformanceQueryPort` | Analytics | Revenue, sales trend, outlet/product performance, inventory health, customer metrics. |
| `FinancialHealthQueryPort` | Accounting/Analytics adapter | P&L, margin, cash-flow ratios, expense and payroll burden, receivable/payable status. |
| `ForecastQueryPort` | Intelligence ML adapter | Demand, stock-coverage and trend forecasts. |
| `FinancingInsightQueryPort` | Financing adapter | AtlasScore explanation inputs, facility health, consent-safe monitoring signals. |

Contracts live under `atlashub-shared/src/main/java/com/atlashub/shared/application/port`; implementations remain in their owning modules. The LLM sees normalized, permitted DTOs only.

## Infrastructure Layer

### Provider and ML Adapters

- `GenerativeAiPort`: chat/streaming completions, structured-output validation, embeddings only when approved.
- `ForecastingModelPort`: prediction and confidence against a named `ModelRelease`.
- `ModelRegistryPort`: approved model artifact/version metadata; provider formats do not leak past the adapter.
- `PromptSafetyPort`: prompt-injection detection, content filtering and tool-call validation.

Adapters use bounded timeouts, idempotency/correlation IDs, encrypted credentials, retry only safe operations, and record provider/model/prompt versions. A provider failure returns a grounded deterministic summary when available; it must not fabricate a result.

### ML Lifecycle

Feature snapshots are produced in MySQL by Analytics. Training/evaluation may execute in a controlled worker environment, but the module persists `ModelRelease`, feature schema hash, training window, evaluation metrics, approval, drift and rollback state in MySQL. Production inference is allowed only from an approved release. Training data is tenant-isolated; cross-organization training requires an explicit, separately governed aggregation policy and is not implied by product access.

### Messaging and Schedulers

- Listeners consume `AnalyticsFeatureSnapshotReady`, `AtlasScoreChanged`, and `FinancingRiskChanged` to schedule or invalidate analyses.
- Schedulers under `infrastructure/messaging/schedulers` run model-drift checks, forecast refreshes and analysis refreshes. They delegate only to command handlers.
- The module publishes `IntelligenceAnalysisCompleted`, `RecommendationCreated`, `ForecastGenerated`, and `ModelDriftDetected` events through the outbox.

### Persistence

All tables are MySQL tables with `organization_id`, environment, timestamps and composite tenant indexes. Conversation/analysis writes use optimistic locking. Evidence and snapshots use JSON columns only for immutable structured payloads, not provider-owned models. MySQL upserts use `INSERT ... ON DUPLICATE KEY UPDATE`.

## Presentation and Security

| Method | Path | Permission | Purpose |
|---|---|---|---|
| `POST` | `/api/v1/intelligence/conversations` | `intelligence:ask` | Ask a grounded business question; streaming is supported where configured. |
| `GET` | `/api/v1/intelligence/conversations` | `intelligence:read` | Page prior conversations. |
| `GET` | `/api/v1/intelligence/analyses/{id}` | `intelligence:read` | View analysis, inputs, evidence, confidence and limitations. |
| `GET` | `/api/v1/intelligence/recommendations` | `intelligence:read` | Page active recommendations. |
| `GET` | `/api/v1/intelligence/forecasts/{id}` | `intelligence:read` | View an authorized forecast. |

Every response includes data-as-of time, evidence references, model/prompt version where applicable, confidence/limitations, and an auditable analysis ID. Prompt context is minimized, sensitive values are redacted before provider calls, audit logs retain tool names and result hashes rather than raw secrets, and organization isolation is enforced before tool execution.

## Detailed Domain Model

### `IntelligenceConversation` (Aggregate Root)

```
IntelligenceConversation
├── id: Long
├── organizationId: Long
├── environment: Environment
├── initiatedBy: Long
├── status: ConversationStatus        ← ACTIVE | CLOSED | FAILED
├── turns: List<IntelligenceTurn>
├── lastActivityAt: ZonedDateTime
├── createdAt: ZonedDateTime
├── updatedAt: ZonedDateTime
└── version: Long
```

`startTurn(question, intent)`, `recordToolInvocation(...)`, `completeTurn(...)`, `failTurn(...)`, and `close()` are the only state transitions. A turn records a redacted question, selected intent, tool/result hashes, analysis ID, response hash, evidence IDs, model/prompt version, confidence and limitations. It never stores credentials, raw database results, or another organization's context.

### `IntelligenceAnalysis` (Aggregate Root)

```
IntelligenceAnalysis
├── id: Long
├── organizationId: Long
├── environment: Environment
├── type: AnalysisType                 ← BUSINESS_SUMMARY | GROWTH_RECOMMENDATION | FORECAST | FINANCING_EXPLANATION
├── status: AnalysisStatus             ← PENDING | RUNNING | COMPLETED | FAILED | STALE
├── requestedBy: Long                  ← nullable for scheduler
├── featureSnapshotId: Long
├── evidence: List<EvidenceReference>
├── recommendations: List<Recommendation>
├── forecast: Forecast                 ← nullable
├── promptVersion: String              ← nullable for deterministic-only analysis
├── modelVersion: String               ← nullable for deterministic-only analysis
├── generatedAt: ZonedDateTime
├── validUntil: ZonedDateTime
├── failureCode: String                ← nullable
├── createdAt: ZonedDateTime
└── version: Long
```

`begin()`, `attachEvidence(...)`, `recordForecast(...)`, `addRecommendation(...)`, `complete(...)`, `markStale(...)`, and `fail(...)` enforce that a completed analysis has an authorized feature snapshot and each factual claim has evidence. `validUntil` prevents a stale forecast or recommendation from being returned as current.

### `ModelRelease` (Aggregate Root)

```
ModelRelease
├── id: Long
├── modelType: ModelType               ← DEMAND_FORECAST | ANOMALY_DETECTION | RISK_PREDICTION
├── version: String                    ← unique with modelType
├── featureSchemaHash: String
├── trainingWindowStart: ZonedDateTime
├── trainingWindowEnd: ZonedDateTime
├── evaluation: ModelEvaluation
├── status: ModelReleaseStatus         ← CANDIDATE | APPROVED | DEPRECATED | DRIFTING | RETIRED
├── approvedBy: Long                   ← nullable until approved
├── approvedAt: ZonedDateTime          ← nullable
└── createdAt: ZonedDateTime
```

`approve(approvedBy)`, `deprecate()`, `markDrifting(...)`, and `retire()` make model governance explicit. Inference refuses a release whose feature schema does not match the referenced feature snapshot.

### Data Quality and Safety Invariants

- An analysis may only reference feature snapshots owned by the same organization and environment.
- Tool authorization is evaluated per invocation, not merely when the conversation starts.
- `FINANCING_EXPLANATION` may explain a persisted score/facility signal but cannot create score factors or capacity values.
- A forecast must include its model release, prediction window, confidence/interval, source data cutoff and known limitations.
- A recommendation distinguishes an evidence-based recommendation from a model hypothesis; neither is represented as a lender decision.
- A provider's natural-language response cannot be persisted as `COMPLETED` until `EvidenceGroundingService` validates the cited structured evidence.

## Intent Routing and Tool Execution

### Supported Intents

| Intent | Required tools | Example output |
|---|---|---|
| `BUSINESS_HEALTH` | business performance, financial health, inventory/customer/outlet performance | Summary of revenue, margin, trend, key changes and limitations. |
| `PROFIT_EXPLANATION` | financial health, outlet/product performance | Evidence-led causes for a change in profit. |
| `RESTOCK_RECOMMENDATION` | inventory health, demand forecast | Suggested products, quantity range, stock cover and confidence. |
| `OUTLET_PERFORMANCE` | outlet performance, financial health | Underperforming outlet factors and actions. |
| `FINANCING_READINESS` | persisted AtlasScore, financial health | Score/capacity explanation and data-quality gaps. |
| `FACILITY_HEALTH` | financing insight, financial health, behaviour signal | Health change, anomaly reasons and merchant actions. |

### Tool Invocation Sequence

```
AskBusinessQuestion
  1. Authenticate principal and resolve active organization/environment.
  2. Classify intent with a constrained intent schema; fall back to clarification on ambiguity.
  3. IntelligenceToolPolicyService permits only tools and fields allowed for that principal.
  4. Call shared query ports using the active organization/environment.
  5. Persist evidence references and hashes before invoking the generative provider.
  6. Give the provider a structured, minimized tool-result context and output schema.
  7. Validate citations, numeric claims and required limitations.
  8. Persist the answer/analysis and return response plus analysis ID.
```

The provider is not allowed to choose arbitrary SQL, invoke a URL, access a repository, or call a tool omitted from the allowlist. Prompt injection text from user-provided records is treated as untrusted content, never as a tool instruction.

## ML and Forecasting Design

### Features and Inference

Analytics creates MySQL `BusinessFeatureSnapshot` records from event projections. The snapshot has a schema version, source time window, quality flags and data-as-of time. The Intelligence forecasting adapter receives only that normalized feature payload. It returns a `Forecast` containing point prediction, interval/quantiles, horizon, confidence, model version and limitations.

Initial model types:

| Model | Input examples | Output | Fallback |
|---|---|---|---|
| Demand forecast | product/outlet sales, stock, seasonality, returns | demand range by product/outlet/horizon | deterministic moving average with explicit low confidence |
| Trend detector | revenue, margin, customer activity | material positive/negative trend | rule-based percentage/window comparison |
| Behaviour anomaly detector | amount, category, beneficiary, frequency, baseline | anomaly score/reasons | facility threshold/rule comparison |
| Financing-risk predictor | only after validated historical outcomes | calibrated risk signal | deterministic facility-health rules |

Training, evaluation and release approval are distinct from online inference. Feature leakage, cross-tenant aggregation, poor calibration, drift and missing-data bias are recorded as evaluation failures. A model that is drifting or fails its quality gate cannot be selected for new analysis.

## MySQL Persistence and Indexes

| Table | Keys and constraints |
|---|---|
| `intelligence_conversations` | `(organization_id, environment, id)` tenant index; optimistic-lock `version` |
| `intelligence_turns` | unique `(conversation_id, turn_number)`; tool/response hashes |
| `intelligence_analyses` | `(organization_id, environment, type, generated_at)` index; feature snapshot FK/reference |
| `intelligence_evidence` | unique `(analysis_id, evidence_type, evidence_id)` |
| `intelligence_recommendations` | `(organization_id, status, valid_until)` index |
| `intelligence_model_releases` | unique `(model_type, version)`; approved-release lookup index |
| `intelligence_model_evaluations` | `(model_release_id, evaluated_at)` index |

Provider content and structured payloads are encrypted/redacted as required. JSON columns hold validated structured evidence or schema-versioned provider output only; searchable and authorization-critical values remain normalized columns. No Intelligence table stores a raw payment instrument, BVN, secret, or unrestricted customer record.

## Listener, Scheduler, and Provider Failure Behaviour

| Trigger | Handler | Result |
|---|---|---|
| `AnalyticsFeatureSnapshotReady` | `GenerateBusinessSummaryHandler` / forecast handler | Refreshes analyses only for eligible snapshots. |
| `AtlasScoreChanged` | `GenerateFinancingExplanationHandler` | Produces a new score explanation, never a new score. |
| `FinancingRiskChanged` | `GenerateGrowthRecommendationsHandler` | Creates a merchant-facing recommendation set. |
| Forecast refresh scheduler | `RunForecastHandler` | Recomputes current forecast with approved release. |
| Model drift scheduler | `EvaluateModelReleaseHandler` | Marks drift, emits event and triggers fallback. |

Provider timeout, malformed output, safety rejection, or tool failure records a bounded failure code and keeps the deterministic evidence. The API returns a safe degraded response only when the deterministic result itself is complete; otherwise it reports that the analysis is unavailable. Retries are idempotent by analysis/correlation ID.

## Presentation DTO Requirements and RBAC

`AskQuestionRequest` accepts only `conversationId` (optional), `question`, and an optional bounded display preference. It never accepts organization ID, a raw tool name, model version, or lender-only data request. `IntelligenceAnswerResponse` includes `answer`, `analysisId`, `evidence`, `dataAsOf`, `confidence`, `limitations`, and optional follow-up suggestions.

| Permission | Grants |
|---|---|
| `intelligence:ask` | Start a permitted organization conversation. |
| `intelligence:read` | Read own-organization conversations, analyses, forecasts and recommendations. |
| `intelligence:models:manage` | AtlasHub staff model-release evaluation/approval only. |
| `intelligence:audits:read` | Restricted operations review of redacted tool/provider audit data. |

Streaming uses an authenticated, organization-scoped connection. Partial tokens are never treated as the final persisted answer; the completed structured response must pass grounding validation.

## Test and Verification Plan

- Unit-test aggregate transitions, tool policy, evidence grounding, prompt/schema validation, model-release gating and fallback behaviour.
- Integration-test MySQL tenant filtering/indexed queries, optimistic locking, encrypted/redacted persistence, Kafka inbox idempotency, scheduled command invocation, and provider adapter timeout/error mapping.
- Security-test prompt injection, cross-organization IDs in tool results, unauthorized intent/tool selection, environment isolation, sensitive-data redaction, and streamed-session authorization.
- Contract-test provider structured output and model adapters with fixed fixtures; no test may depend on a live LLM response.

## Event Contracts and Consumer Rules

| Event | Required payload | Consumers |
|---|---|---|
| `IntelligenceAnalysisCompleted` | analysis ID, org/environment, type, feature snapshot ID, data-as-of, confidence, evidence IDs | Notifications, dashboard refresh, audit |
| `RecommendationCreated` | recommendation ID, analysis ID, org/environment, category, priority, valid-until | Notifications, merchant dashboard |
| `ForecastGenerated` | forecast ID, org/environment, model/version, horizon, confidence, feature snapshot ID | Analytics-aware dashboards, financing monitoring where permitted |
| `ModelDriftDetected` | model type/version, evaluation ID, drift metrics, action | Operations, release governance |

Event payloads never contain full prompts, raw provider messages, customer PII, bank details, or complete feature snapshots. Consumers query a redacted, authorized result when they need display content. Event consumers are idempotent by event ID and group ID.

## Conversation Experience and Operational Controls

Conversation contexts are organization/environment bound. A user can continue only a conversation that belongs to the active organization and for which they retain `intelligence:read`. A conversation can be closed by the user, retention policy, organization suspension, or a safety incident. Closing prevents new turns but preserves the redacted audit evidence required by policy.

The API supports non-streaming and server-sent/streaming response delivery behind the same `AskBusinessQuestionHandler`. The handler persists the selected tools and evidence before delivery; if the client disconnects, the analysis can finish and be retrieved by ID. Rate limits apply per organization/user/API key and provider budget limits apply before a model call. Long-running forecast/analysis work returns an analysis ID and is completed asynchronously through the existing event/outbox pattern.

## Prompt, Retrieval, and Data-Minimization Controls

- Prompt templates are versioned, reviewed assets; code selects a template by approved intent, not free-form user instruction.
- Retrieval is from explicitly indexed, organization-scoped AtlasHub knowledge/evidence collections only. No open internet retrieval is implied for financial answers.
- Tool results are transformed to the smallest data shape needed for the request: aggregate customer metrics, not customer-level history, unless a permitted tool specifically requires an identified customer.
- System instructions prohibit disclosure of system prompts, credentials, other organizations, ungrounded calculations and financial/legal certainty claims.
- Provider request/response metadata records token use, latency, model/prompt version, safety outcome and hashes; operations staff access redacted audit records through a dedicated permission.
- A response with low confidence, incomplete accounting reconciliation, stale features or missing business history must say so and offer the next permitted action rather than filling gaps.

## Hackathon and Production Exit Criteria

The hackathon requires one working production-style conversation path for business-health, score explanation and anomaly explanation, with every displayed financial claim traceable to the feature/score/risk snapshot. Production exit additionally requires provider failover policy, load/rate tests, retention/deletion schedule, safety incident procedures, model/prompt release governance, feedback evaluation, observability dashboards, and security review of every allowed tool.

## Complete Planned Artifact List

```
atlashub-intelligence/src/main/java/com/atlashub/intelligence/
├── domain/
│   ├── entities/ [IntelligenceConversation, IntelligenceAnalysis, ModelRelease]
│   ├── valueobject/ [AnalysisType, EvidenceReference, ToolInvocation, Forecast, Confidence, PromptVersion, ModelVersion]
│   ├── services/ [IntelligenceToolPolicyService, EvidenceGroundingService, RecommendationPolicyService]
│   ├── ports/ [GenerativeAiPort, ForecastingModelPort, ModelRegistryPort, PromptSafetyPort]
│   ├── repositories/ [ConversationRepository, IntelligenceAnalysisRepository, ModelReleaseRepository]
│   ├── events/ [IntelligenceAnalysisCompleted, RecommendationCreated, ForecastGenerated, ModelDriftDetected]
│   └── exceptions/ [UnauthorizedIntelligenceToolException, UnsupportedAnalysisIntentException, UngroundedResponseException, ModelReleaseUnavailableException, CrossOrganizationEvidenceException, ConversationStateException]
├── application/
│   ├── commands/ [AskBusinessQuestion, GenerateBusinessSummary, GenerateGrowthRecommendations, GenerateFinancingExplanation, RunForecast, EvaluateModelRelease, ApproveModelRelease]
│   └── queries/ [GetConversation, ListConversations, GetIntelligenceAnalysis, ListRecommendations, GetForecast, ListModelReleases]
├── infrastructure/
│   ├── ai/adapters/ [OpenAiGenerativeAdapter or approved provider adapter, PromptSafetyAdapter]
│   ├── ml/adapters/ [ForecastingModelAdapter, ModelRegistryAdapter]
│   ├── messaging/listeners/ [AnalyticsFeatureSnapshotReadyListener, AtlasScoreChangedListener, FinancingRiskChangedListener]
│   ├── messaging/schedulers/ [ForecastRefreshScheduler, ModelDriftScheduler]
│   └── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [AskQuestionRequest, ConversationResponse, IntelligenceAnalysisResponse, RecommendationResponse, ForecastResponse]
    └── rest/ [IntelligenceConversationController, IntelligenceAnalysisController]
```
