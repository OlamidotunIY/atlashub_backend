# Notifications Module Design (`atlashub-platform:notifications`)

## Role & Purpose

The `notifications` module is the **multi-channel communication engine** of AtlasHub. It is a **pure consumer module** — it never originates a notification on its own initiative. It reacts to events from every other module and delivers the appropriate message through the appropriate channel to the appropriate recipient.

This module owns:
- Channel adapters (Email, SMS, Push, WhatsApp, WebSocket in-app)
- Notification template management
- Delivery tracking and retry logic
- User notification preferences

It does **not** own any business logic. The decision of *what* notification to send is determined by the event type. The decision of *how* and *to whom* is determined by this module's configuration and the recipient information carried in the event.

---

## 1. Channels

| Channel | Provider | Use Case |
|---|---|---|
| **Email** | SendGrid (primary), AWS SES (fallback) | Transactional emails: receipts, invoices, payroll notifications, KYC updates |
| **SMS** | Termii (primary), Twilio (fallback) | Time-sensitive alerts: payment confirmations, OTPs, salary paid |
| **Push Notification** | Firebase FCM | Mobile app alerts: delivery updates, order confirmations |
| **WhatsApp** | WhatsApp Business API | High-engagement transactional messages: receipts, OTPs for users who prefer WhatsApp |
| **In-App WebSocket** | Spring WebSocket (STOMP) | Real-time in-platform notifications: payment confirmed, KOT ready, shipment assigned |

---

## 2. Features

### Template Engine
All notification content is defined as templates stored in the database. Templates support variable substitution using Mustache-style syntax (`{{recipientName}}`, `{{amount}}`, etc.).

Each `NotificationTemplate` is versioned — changing a template creates a new version; active notifications reference the version used at send time.

### Delivery Tracking
Every notification attempt is recorded as a `NotificationDelivery`. The delivery tracks:
- Which channel was used
- Which provider handled it
- Whether it was delivered, bounced, or failed
- Retry count and next retry timestamp

### Retry with Exponential Backoff
Failed deliveries are retried up to 5 times with exponential backoff (5s, 30s, 5m, 30m, 2h). After 5 failures, the delivery is marked PERMANENTLY_FAILED and the sender is alerted via the platform audit log.

### User Notification Preferences
Users can configure which channels they prefer for each notification category (e.g., "I want payroll notifications via email only, not SMS").

### Channel Fallback
If the primary provider fails (e.g., SendGrid is down), the module automatically falls back to the secondary provider (AWS SES). SMS falls back from Termii to Twilio.

### WebSocket In-App Notifications
Real-time in-platform alerts are delivered via WebSocket to the user's current browser/app session. Each user has a dedicated WebSocket channel at `/user/{userId}/queue/notifications`. Organization-wide broadcasts go to `/topic/org/{orgId}/events`.

---

## 3. Domain Entities & Aggregates

### `NotificationTemplate` (Aggregate Root)

```
NotificationTemplate
├── id: Long
├── code: String                        ← unique, e.g., "PAYMENT_CONFIRMED", "PAYROLL_DISBURSED"
├── category: NotificationCategory      ← TRANSACTIONAL, ALERT, MARKETING
├── channel: NotificationChannel        ← EMAIL, SMS, PUSH, WHATSAPP
├── subject: String                     ← nullable, for email
├── bodyTemplate: String                ← Mustache template string
├── version: Integer                    ← increments on each update
├── isActive: Boolean
└── updatedAt: ZonedDateTime
```

---

### `NotificationDelivery` (Aggregate Root)

```
NotificationDelivery
├── id: Long
├── templateCode: String
├── templateVersion: Integer
├── recipientType: RecipientType        ← USER | ORGANIZATION | EXTERNAL_EMAIL | PHONE_NUMBER
├── recipientId: String                 ← userId, orgId, or raw email/phone
├── channel: NotificationChannel
├── provider: String                    ← "SENDGRID", "SES", "TERMII", "TWILIO", "FCM", "WHATSAPP"
├── variables: Map<String, String>      ← template substitution values
├── renderedSubject: String             ← nullable
├── renderedBody: String
├── status: DeliveryStatus              ← PENDING, DELIVERED, FAILED, RETRYING, PERMANENTLY_FAILED
├── attemptCount: Integer
├── providerMessageId: String           ← nullable, returned by provider on success
├── failureReason: String               ← nullable
├── nextRetryAt: ZonedDateTime          ← nullable
├── deliveredAt: ZonedDateTime          ← nullable
└── createdAt: ZonedDateTime
```

---

### `UserNotificationPreference` (Entity)

```
UserNotificationPreference
├── id: Long
├── userId: Long
├── category: NotificationCategory
├── enabledChannels: Set<NotificationChannel>
└── updatedAt: ZonedDateTime
```

---

## 4. Notification Catalogue

Every system notification is documented here. Modules publish events; this module maps events to templates and recipients.

### Identity & Auth
| Trigger Event | Template Code | Channels |
|---|---|---|
| `UserCreatedEvent` | `WELCOME_EMAIL` | Email |
| `EmailVerifiedEvent` | `EMAIL_VERIFIED` | Email |
| `PasswordResetInitiatedEvent` | `PASSWORD_RESET` | Email |
| `AccountLockedEvent` | `ACCOUNT_LOCKED` | Email, SMS |
| `SuspiciousLoginDetectedEvent` | `SUSPICIOUS_LOGIN` | Email, Push |

### Compliance
| Trigger Event | Template Code | Channels |
|---|---|---|
| `ComplianceSubmittedEvent` | `KYC_SUBMITTED_CONFIRMATION` | Email |
| `OrganizationComplianceApprovedEvent` | `KYC_APPROVED` | Email, SMS |
| `OrganizationComplianceRejectedEvent` | `KYC_REJECTED` | Email |

### Payments
| Trigger Event | Template Code | Channels |
|---|---|---|
| `ChargeSuccessfulEvent` | `PAYMENT_CONFIRMED` | Email, SMS, Push, WhatsApp |
| `ChargeFailedEvent` | `PAYMENT_FAILED` | Email, SMS |
| `VirtualAccountActivatedEvent` | `ACCOUNT_ACTIVATED` | Email |
| `WalletFundedEvent` | `WALLET_FUNDED` | Push, SMS |
| `PayoutCompletedEvent` | `PAYOUT_COMPLETED` | Email |
| `PayoutFailedEvent` | `PAYOUT_FAILED` | Email, SMS |
| `InvoiceIssuedEvent` | `BILLING_INVOICE` | Email |
| `InvoiceOverdueEvent` | `INVOICE_OVERDUE` | Email, SMS |
| `SubscriptionSuspendedEvent` | `SUBSCRIPTION_SUSPENDED` | Email |

### HR & Payroll
| Trigger Event | Template Code | Channels |
|---|---|---|
| `EmployeeOnboardedEvent` | `EMPLOYEE_WELCOME` | Email |
| `PayrollApprovedEvent` | `PAYROLL_APPROVED` | Push (to payroll initiator) |
| `PayrollDisbursedEvent` | `SALARY_PAID` | SMS (to each employee) |
| `LeaveApprovedEvent` | `LEAVE_APPROVED` | Email, Push |
| `LeaveRejectedEvent` | `LEAVE_REJECTED` | Email |
| `LoanApprovedEvent` | `LOAN_APPROVED` | Email, SMS |

### Logistics
| Trigger Event | Template Code | Channels |
|---|---|---|
| `ShipmentCreatedEvent` | `SHIPMENT_CREATED` | SMS, Email |
| `ShipmentDispatchedEvent` | `SHIPMENT_DISPATCHED` | SMS, Push |
| `ShipmentDeliveredEvent` | `SHIPMENT_DELIVERED` | SMS, Push |
| `ShipmentFailedEvent` | `SHIPMENT_FAILED` | SMS, Email |

### Commerce
| Trigger Event | Template Code | Channels |
|---|---|---|
| `PosSaleCompletedEvent` | `SALES_RECEIPT` | Email, WhatsApp |
| `KotReadyEvent` | `KOT_READY` | WebSocket (to cashier terminal) |
| `OnlineOrderCreatedEvent` | `ORDER_CONFIRMED` | Email, SMS |

---

## 5. WebSocket Real-Time Notifications

The following events are broadcast via WebSocket in addition to (or instead of) the channels above:

| Event | WebSocket Topic | Recipient |
|---|---|---|
| `KotReadyEvent` | `/topic/outlet/{outletId}/kitchen` | Kitchen display + cashier terminal |
| `ChargeSuccessfulEvent` | `/user/{cashierId}/queue/notifications` | Cashier who initiated the payment |
| `ShipmentAssignedEvent` | `/user/{riderId}/queue/notifications` | The assigned rider |
| `ShipmentLocationUpdatedEvent` | `/topic/shipment/{trackingNumber}` | Anyone tracking (customer-facing) |
| `JournalEntryPendingApprovalEvent` | `/user/{approverId}/queue/notifications` | Designated journal entry approvers |
| `PayrollApprovedEvent` | `/topic/org/{orgId}/payroll` | HR team members in the org |
| `InvoiceIssuedEvent` | `/user/{orgOwnerId}/queue/notifications` | Organization owner/admin |

---

## 6. Outbound Port (Used by Other Modules)

Modules that need to trigger ad-hoc notifications (e.g., `auth` sending an OTP) call this port:

```java
// In atlashub-shared
public interface NotificationPort {
    void send(NotificationRequest request);
}

// NotificationRequest
public record NotificationRequest(
    String templateCode,
    RecipientType recipientType,
    String recipientId,
    NotificationChannel preferredChannel,
    Map<String, String> variables
) {}
```

This port is implemented in `notifications` and injected by modules that need synchronous notification sending (e.g., OTP delivery must be synchronous — the user is waiting).

---

## 7. Exceptions & Errors

**`NotificationsErrorCode`**:
- `TEMPLATE_NOT_FOUND`, `TEMPLATE_RENDER_FAILED`
- `DELIVERY_FAILED_PERMANENTLY`
- `PROVIDER_UNAVAILABLE`
- `INVALID_RECIPIENT`

---

## 8. Commands & Use Cases

- `SendNotificationCommand(templateCode, recipientType, recipientId, variables)` → `SendNotificationUseCase`
- `RetryFailedDeliveryCommand(deliveryId)` → `RetryDeliveryUseCase`
- `UpdateNotificationPreferenceCommand(userId, category, enabledChannels)` → `UpdatePreferenceUseCase`
- `CreateNotificationTemplateCommand(code, category, channel, subject, body)` → `CreateTemplateUseCase` ← admin only
- `UpdateNotificationTemplateCommand(templateId, subject, body)` → `UpdateTemplateUseCase` ← admin only

---

## 9. Queries

- `GetDeliveryStatusQuery(deliveryId)` → `DeliveryStatusResult`
- `ListFailedDeliveriesQuery(dateFrom, dateTo)` → `List<FailedDeliveryResult>` ← admin monitoring
- `GetUserPreferencesQuery(userId)` → `List<NotificationPreferenceResult>`
- `ListTemplatesQuery(channel, category)` → `List<NotificationTemplateResult>`

---

## 10. Listeners

This module is **listener-heavy** — it subscribes to events from every other module. See the Notification Catalogue in Section 4 for the complete mapping.

All listeners use the Inbox pattern to ensure each notification is sent exactly once, even if the event is delivered multiple times (which is normal with Kafka's at-least-once guarantee).

---

## 11. Distributed Architecture

### Idempotency
All listeners use `EventDeliveryTracker` with `(eventId, "notifications-consumer")` as the key. A notification is sent at most once per event instance.

### Provider Failover
Channel adapters implement the circuit-breaker pattern:
1. First attempt goes to primary provider
2. If primary fails (timeout or 5xx response), retry on secondary provider
3. Both failures → mark FAILED, schedule retry

### Delivery Order
Notifications are best-effort ordered. For critical notifications (OTPs, payment confirmations), SMS is attempted simultaneously with email to maximize delivery speed.
