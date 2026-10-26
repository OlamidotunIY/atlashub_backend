# Architecture proposal: Atlas Intelligence agent, text/voice and speaker-risk signals

**Shared contract update (2026-10-26):** [atlashub_contracts](https://github.com/OlamidotunIY/atlashub_contracts) now owns initial Protobuf `v1alpha1` Java/Python [authorized tool gateway](https://github.com/OlamidotunIY/atlashub_contracts/blob/main/proto/atlashub/intelligence/v1alpha1/tool_gateway.proto), to be implemented as a native gRPC Java adapter and Python client. The separate Python repository already exists; neither service nor generated SDK is implemented. Browser voice uses WebRTC/WebSocket gateway. Java maps generated DTOs into DDD application use cases and enforces org/approval/provider capabilities.


Status: PROPOSED — not implemented. Date: 2026-10-24.
Companion repository [atlashub_intelligence](https://github.com/OlamidotunIY/atlashub_intelligence) exists (architecture docs only).
Related: [global integrations](./global-provider-integrations-proposal.md), [delivery plan](./global-expansion-delivery-plan.md).

## Ownership and protocols
Java AtlasHub remains authoritative for authenticated identities, organization membership, authorization/RBAC, entitlements, business command/query handlers, approval controls, audit and persistence. A separately deployed Python intelligence service owns conversation processing, tool planning, voice streaming, speech-to-text, speech synthesis, self-hosted model inference, optional speaker similarity, and model governance. gRPC/protobuf is preferred for private Java/Python service-to-service contracts; public client-facing APIs remain HTTPS and streamed browser/mobile transports (WebSocket/WebRTC where appropriate). Internal RPC traffic is mutually authenticated, authorized and audited.

Python does **not** receive raw unrestricted database credentials or bypass Java business command handlers. Prompts and model outputs are untrusted. Authorized tool schemas are versioned; requests are tenant-scoped and validated again by Java at execution time.

## Functional command tiers
- Read-only queries: authorized query tools; attributable evidence for numerical/financial claims.
- Low-risk operations: validate identity/session/permissions, arguments, business invariants and idempotency; provide visible acknowledgment based on actual response.
- High-risk/irreversible operations: dry-run preview followed by explicit step-up auth and maker-checker/approval where relevant (payouts, refunds, payroll, bulk inventory transfers, bank detail changes, destructive edits).
- No requested connection or no capability: explain unavailability without fabricating success.

Example transfer: `recognize intent -> resolve inventory/product/outlets -> confirm exact proposal -> Java RequestStockTransferHandler -> read persisted outcome -> report truthful status`.

## Speech modalities
Speech-to-text (ASR) determines what was said. Speaker verification (ASV) estimates similarity to an enrolled speaker. Anti-spoofing/presentation attack detection estimates risk from replay, synthesis or other injected audio. Speaker diarization can detect multiple speakers. These are different systems and must not be conflated.

Voice may be a convenience/risk signal only — NOT a standalone authentication factor. NIST SP 800-63B-4 explicitly says biometric comparison based on voice SHALL NOT be used; use sessions, passkeys/MFA and risk-based step-up as authentication authority. Speech confidence and ASV scores must never override RBAC. See https://pages.nist.gov/800-63-4/sp800-63b.html and https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html.

## Optional voice-profile enrollment
1. User authenticates and performs strong step-up authentication, gives separate, revocable biometric-processing consent.
2. Capture several varied guided phrases with quality checks. Store encrypted speaker embeddings/model version and minimum necessary metadata; default to no long-term raw-audio retention unless essential and consented.
3. Associate profile with individual user ID, never organization ID. Keep encryption keys outside application database, enforce deletion/retention/export policies.
4. Require step-up authentication to enroll additional samples, replace or delete voice profiles. Notify on enrollment changes.
5. Test across accents, microphones, noise, changed voice, illness, aging, synthetic/cloned audio and replay; measure false acceptance/rejection by population and environment before enabling production risk decisions.

## Runtime outcomes
NORMAL: with valid session and permissions, allow eligible routine voice operations.
UNCERTAIN: pause voice command execution and offer passkey/MFA confirmation or text UI continuation.
SUSPICIOUS: deny voice-only execution, log a minimal risk event, require secure reauthentication for resumption.
NEVER: conclude identity solely from high voice similarity, automatically lock an account solely for a voice mismatch, or claim speaker identification is infallible.

A cold or other temporary voice change can degrade scores; use quality-aware retry and non-voice recovery. Enrollment template updates must be separately authenticated to prevent attacker-induced adaptation/poisoning. Set risk thresholds from actual test metrics; do not invent universal confidence percentages.

## Conversation/session design
Conversation, ConversationTurn, ToolCall, ToolAuthorizationResult, ActionProposal, ActionApproval, VoiceSession, VoiceProfileReference, VoiceRiskAssessment, ModelRelease and PromptRelease are proposed concepts. Stream incremental ASR and assistant speech; support barge-in, cancellation, resumed context and synchronized UI actions. Reconfirm authorization and user identity server-side before every tool call. Never treat instructions in product records, web pages or transcriptions as higher-priority authority.

## Suggested Python boundaries
`domain`: conversation, action proposals, model/risk policies and interfaces.
`application`: command-handling orchestrators, conversation pipeline, evidence validation, evaluation.
`infrastructure`: model-serving adapters, ASR/TTS, speaker-risk inference, encrypted storage, gRPC clients, telemetry.
`presentation`: protobuf/gRPC endpoints and streaming audio session handlers.
Avoid copying Java package names or forcing every speech chunk through heavyweight CQRS. Domain isolation and clean dependency direction matter more than mechanical folder symmetry.

## Milestones
0. Threat model, privacy/data protection decision, voice evaluation plan.
1. Text-only permitted read tools with audit and citations.
2. Low-risk state-changing tools with previews/approvals.
3. Self-hosted model inference and operational benchmarking.
4. Streaming ASR/TTS conversations and interruption handling.
5. Optional speaker consistency signals and anti-spoofing research/evaluation, secure enrollment and recovery.
6. Security review and staged rollout; never rely on voice as sole authenticator.
