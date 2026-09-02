# Infrastructure Module Design (`atlashub-infrastructure`)

## Role & Purpose

The Infrastructure module houses the **cross-cutting technical concerns** shared by every other module in the platform. It has no business domain of its own — it exists purely to solve platform-wide engineering problems: reliable message delivery, notification dispatch, security audit trails, and abuse prevention.

Every module in AtlasHub that publishes or consumes domain events does so through the primitives provided by this module. Notifications sent to users (email, SMS, push) are handled exclusively here. Every security-critical action is logged here. Rate limits are enforced here.

This module is a **dependency of all other modules** — it is the lowest layer in the dependency graph.

---

## 1. `eventbus` Submodule

### Role
Implements the **Transactional Outbox** and **Inbox (Idempotency)** patterns that guarantee reliable, exactly-once event delivery between modules.

### Why This Exists
In a distributed system (or even a modular monolith with separate DB schemas), simply publishing an event in code is not safe — if the event bus (Kafka/RabbitMQ) is temporarily unavailable, or if the application crashes between saving to DB and publishing the event, the event is lost. The Outbox pattern solves this.

### Outbox Pattern
When a use case saves an aggregate and wants to publish events, it writes the events to an `OutboxMessage` table **within the same database transaction** as the aggregate save. A background `OutboxPollingScheduler` then reads `PENDING` messages and pushes them to the event bus, updating them to `PROCESSED`. This guarantees atomicity — either both the DB write and the event publication happen, or neither does.

**Entities**:
- **`OutboxMessage`**: `id`, `topic`, `aggregateType`, `aggregateId`, `payload` (JSON), `status` (PENDING, PROCESSED, DLQ), `createdAt`, `processedAt`, `retryCount`

**Process**:
1. Use case writes aggregate + `OutboxMessage` in one transaction.
2. `OutboxPollingScheduler` polls `OutboxMessage WHERE status = 'PENDING'` every second.
3. Pushes to Kafka/RabbitMQ topic.
4. Updates `OutboxMessage.status = 'PROCESSED'`.
5. On repeated failure, moves to DLQ (`status = 'DLQ'`) for manual inspection.

### Inbox Pattern (Idempotency)
When a module consumer receives an event, it checks the `EventDeliveryTracker` before processing. If the `(eventId, consumerId)` pair already exists and status is `SUCCESS`, the message is acknowledged and discarded — preventing double processing.

**Entities**:
- **`EventDeliveryTracker`**: `eventId`, `consumerId`, `status` (PENDING, SUCCESS, DLQ), `processedAt`

**Critical Consumers Using Inbox**:
| Consumer | Event | Why |
|---|---|---|
| `billing.PaymentSuccessfulListener` | `PaymentSuccessfulEvent` | Prevent double subscription renewal |
| `hr.BulkPayoutCompletedListener` | `BulkPayoutCompletedEvent` | Prevent marking payroll disbursed twice |
| `commerce.PaymentSuccessfulListener` | `PaymentSuccessfulEvent` | Prevent double stock deduction |
| `pay.WebhookInboundAdapter` | Gateway webhooks | Prevent double ledger posting from duplicate Paystack webhooks |
| `auth.UserCreatedListener` | `UserCreated` | Prevent creating two `AuthAccount`s for one user |
| `accounting.*Listener` | All business events | Prevent duplicate journal entries |

---

## 2. `notifications` Submodule

### Role
Dispatches **Email**, **SMS**, and **In-App Push** notifications to users and organizations based on domain events consumed from other modules.

### Architecture
Notifications are **event-driven** — no module directly calls a notification service. Instead, domain events are published and notification listeners pick them up and dispatch the appropriate communication. This keeps business logic clean and notification dispatch entirely decoupled.

**Adapters**:
- **`SmtpEmailSenderAdapter`**: Integrates with SendGrid or AWS SES for transactional emails.
- **`SmsGatewayAdapter`**: Integrates with Termii or Twilio for SMS OTPs and alerts.
- **`PushNotificationAdapter`**: Integrates with Firebase Cloud Messaging (FCM) for in-app push.

**Template Engine**: Thymeleaf / Freemarker templates for HTML emails with dynamic variables (user name, invoice amount, tracking number, etc.).

### Notification Listeners

| Listener | Event | Notification Sent |
|---|---|---|
| `AuthVerificationListener` | `AuthVerificationCreatedEvent` | OTP email, password reset link, account setup link |
| `AuthSecurityListener` | `AuthNewDeviceLoginEvent`, `AuthAccountLockedEvent` | Security alert email |
| `IdentityInvitationListener` | `InvitationCreated` | "You've been invited to join [org]" email |
| `IdentityComplianceListener` | `OrganizationComplianceApproved`, `OrganizationComplianceRejected` | Compliance decision email |
| `BillingNotificationListener` | `BillingInvoiceGeneratedEvent`, `SubscriptionRenewedEvent`, `SubscriptionSuspendedEvent` | Invoice email, renewal confirmation, suspension warning |
| `CommerceReceiptListener` | `PosSaleCompletedEvent` | Digital receipt email/SMS to customer |
| `LogisticsTrackingListener` | `ShipmentDispatchedEvent`, `ShipmentDeliveredEvent`, `ShipmentFailedEvent` | Shipment tracking updates to recipient |
| `HrPayrollListener` | `PayrollDisbursedEvent` | "Your salary has been paid" SMS to each employee |
| `HrLeaveListener` | `LeaveApplicationApprovedEvent`, `LeaveApplicationRejectedEvent` | Leave decision notification |
| `PayAccountListener` | `VirtualAccountActivatedEvent` | "Your virtual account is ready" email |

### Idempotency
All notification listeners use the Inbox pattern. A duplicate event will not send a duplicate email or SMS.

---

## 3. `audit` Submodule

### Role
Maintains an **immutable, append-only audit log** of all security-critical and compliance-relevant actions across the platform. This log cannot be modified or deleted — it is the definitive record of what happened, who did it, and when.

### What Gets Audited
The audit module is not a generic catch-all — specific domain events are explicitly subscribed to for audit. Priority events:

| Category | Events Audited |
|---|---|
| **Authentication** | `AuthSessionCreatedEvent`, `AuthSessionRevokedEvent`, `AuthAccountLockedEvent`, `AuthPasswordChangedEvent` |
| **Compliance** | `OrganizationComplianceSubmitted`, `OrganizationComplianceApproved`, `OrganizationComplianceRejected`, `OrganizationBanned` |
| **Identity** | `InvitationAccepted`, `OrganizationMemberAdded` |
| **Admin Actions** | `AdminCreatedEvent`, compliance approve/reject decisions |
| **HR** | `EmployeeTerminatedEvent`, `InfractionLoggedEvent`, `PayrollApprovedEvent` |
| **Inventory** | `StockAdjustedEvent` (HIGH risk — potential shrinkage) |
| **Finance** | `JournalEntryPostedEvent` (manual entries), `CashEvacuatedEvent` |

**Entities**:
- **`AuditLog`**: `id`, `userId` (nullable — null for system events), `organizationId`, `action`, `resourceType`, `resourceId`, `previousState` (JSON, nullable), `newState` (JSON, nullable), `ipAddress`, `timestamp`

### Rules
- `AuditLog` records are **insert-only** — no updates, no deletes, ever.
- Previous and new state are stored as JSON snapshots for forensic investigation.
- Dedicated, specific listeners per event category (no single generic listener for all events per architecture rules).

---

## 4. `rate-limiter` Submodule

### Role
Prevents abuse and denial-of-service attacks on sensitive public APIs.

### What It Protects
- **Authentication endpoint** (`POST /auth/authenticate`): Max 10 requests per minute per IP address. Prevents brute-force attacks.
- **Webhook endpoints** (`POST /webhooks/paystack`, `/webhooks/anchor`): Max 500 requests per minute per IP. Prevents webhook replay floods.
- **Public API endpoints**: Max 1000 requests per minute per `organizationId` (derived from API key). Prevents one org from consuming all resources.
- **OTP/Verification endpoints**: Max 5 requests per 15 minutes per user. Prevents OTP enumeration.

### Implementation
- Backed by **Redis** with sliding window counters.
- On limit breach: HTTP 429 Too Many Requests returned with `Retry-After` header.
- Configurable limits per endpoint via config file (no code change needed to adjust limits).

---

## 5. `file-storage` Submodule (Future)

### Role
Handles upload, storage, and retrieval of user-uploaded files:
- Organization logo uploads (from identity compliance)
- Proof of delivery photos (from logistics)
- KYC document uploads (NIN, government ID scans)
- Payslip PDF generation and storage

### Implementation
- Backed by AWS S3 or Google Cloud Storage.
- Files are stored with a unique path: `/{orgId}/{module}/{resourceId}/{filename}`.
- Presigned URLs issued for direct client uploads (no file passes through the API server).
- URLs returned to the calling module for storage in the relevant aggregate field (e.g., `Shipment.proofOfDeliveryUrl`).

---

## 6. Cross-Module Dependency Summary

```
All Modules
    ↓ use
atlashub-infrastructure
    ├── eventbus         (Outbox/Inbox — used by every module that publishes or consumes events)
    ├── notifications    (Email/SMS/Push — triggered by events from all modules)
    ├── audit            (Immutable log — subscribes to security/compliance events from all modules)
    └── rate-limiter     (Redis — enforced at API gateway / filter layer)
```

No business module should depend on `infrastructure` for domain logic. Infrastructure is a technical utility layer only.
