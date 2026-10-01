# Notifications Module Design (`atlashub-platform:notifications`)

## Role & Purpose

The `notifications` module is the **multi-channel communication engine** of AtlasHub. It is a **pure consumer module** — it never originates a notification on its own initiative. It reacts to domain events published by every other module and delivers the appropriate message through the appropriate channel to the appropriate recipient.

This module owns:
- Multi-channel delivery adapters (Email via SMTP, SMS via Termii/Twilio, Push via FCM)
- Notification template management (Mustache-style variable substitution)
- Delivery tracking and exponential-backoff retry logic
- User notification preferences (per-channel, per-event-type opt-in/opt-out)
- The `SelectiveWebSocketBroadcaster` — the single component responsible for deciding which domain events warrant a real-time WebSocket push to the frontend

It does **not** own any business logic. The decision of *what* notification to send is determined by the incoming event type. The decision of *how* and *to whom* is determined by the template configuration and the recipient data carried in the event.

---

## 1. Features

### Multi-Channel Delivery
| Channel | Provider | Use Case |
|---|---|---|
| **Email** | SMTP (primary, e.g. SendGrid SMTP relay) | Transactional emails: receipts, invoices, KYC updates, welcome |
| **SMS** | Termii (primary), Twilio (fallback) | Time-sensitive alerts: payment confirmations, OTPs, salary paid |
| **Push Notification** | Firebase FCM | Mobile app alerts: delivery updates, order confirmations |
| **In-App WebSocket** | Spring WebSocket (STOMP) via `SelectiveWebSocketBroadcaster` | Real-time in-platform notifications: payment confirmed, KOT ready, shipment assigned |

### Template Engine
All notification content is stored as `NotificationTemplate` records. Templates support Mustache-style variable substitution (`{{recipientName}}`, `{{amount}}`). The `version` counter on the template increments on every update. Deliveries record the `templateVersion` used at send time.

### Delivery Tracking
Every send attempt is recorded as a `NotificationDelivery` aggregate, which tracks channel, provider, rendered content, delivery status, retry count, and the next retry timestamp.

### Retry with Exponential Backoff
Failed deliveries are retried up to 5 times with exponential backoff (5 s → 30 s → 5 min → 30 min → 2 h). After 5 failures the delivery transitions to `PERMANENTLY_FAILED`.

### Channel Provider Failover
Channel adapters implement a primary→fallback pattern: if the primary provider fails (timeout or 5xx), the request is retried on the fallback provider. Both failures cause the delivery to be marked `FAILED` and scheduled for retry.

### User Notification Preferences
Users configure which channels they prefer per event type (e.g., payroll notifications via Email only, not SMS). Preferences are consulted before dispatch — channels not opted-in are skipped.

### WebSocket Real-Time Push
The `SelectiveWebSocketBroadcaster` subscribes to multiple Kafka topics and selectively pushes only those events where a human is actively waiting for a result in the browser or mobile app. It converts raw domain event payloads into lightweight `PushNotification` transfer objects before sending over STOMP.

---

## 2. Domain Layer

### 2.1 Aggregates & Entities

#### `NotificationDelivery` (Aggregate Root)

```
NotificationDelivery
├── id: Long
├── templateCode: String
├── templateVersion: Integer
├── recipientType: RecipientType    ← USER | ORGANIZATION | EXTERNAL_EMAIL | PHONE_NUMBER
├── recipientId: String             ← userId, orgId, or raw email/phone
├── channel: NotificationChannel
├── provider: String                ← "SMTP", "TERMII", "TWILIO", "FCM"
├── variables: Map<String, String>  ← template substitution values
├── renderedSubject: String         ← nullable, for email channel
├── renderedBody: String
├── status: DeliveryStatus          ← PENDING | DELIVERED | FAILED | RETRYING | PERMANENTLY_FAILED
├── attemptCount: Integer
├── providerMessageId: String       ← nullable, returned by provider on success
├── failureReason: String           ← nullable
├── nextRetryAt: ZonedDateTime      ← nullable
├── deliveredAt: ZonedDateTime      ← nullable
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `markDelivered(String providerMessageId)` → transitions to `DELIVERED`, records `deliveredAt`
- `markFailed(String failureReason)` → transitions to `FAILED`, increments `attemptCount`
- `retry(ZonedDateTime nextRetryAt)` → transitions to `RETRYING`, increments `attemptCount`
- `markPermanentlyFailed(String failureReason)` → transitions to `PERMANENTLY_FAILED`

---

#### `NotificationTemplate` (Aggregate Root)

```
NotificationTemplate
├── id: Long
├── code: String              ← unique, e.g. "PAYMENT_CONFIRMED", "KYC_APPROVED"
├── category: NotificationCategory  ← TRANSACTIONAL | ALERT | MARKETING
├── channel: NotificationChannel
├── subject: String           ← nullable, email only
├── bodyTemplate: String      ← Mustache template string
├── version: Integer          ← increments on each updateTemplate() call
├── isActive: Boolean
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `updateTemplate(String subject, String bodyTemplate)` → updates content, increments `version`, touches `updatedAt`
- `activate()` → sets `isActive = true`
- `deactivate()` → sets `isActive = false`

---

#### `UserNotificationPreference` (Entity)

```
UserNotificationPreference
├── id: Long
├── userId: Long
├── eventType: String           ← matches a template code category, e.g. "PAYROLL", "PAYMENT"
├── channel: NotificationChannel
├── enabled: Boolean
└── updatedAt: ZonedDateTime
```

---

### 2.2 Value Objects

| Type | Kind | Values / Notes |
|---|---|---|
| `NotificationChannel` | Enum | `EMAIL`, `SMS`, `PUSH` |
| `DeliveryStatus` | Enum | `PENDING`, `DELIVERED`, `FAILED`, `RETRYING`, `PERMANENTLY_FAILED` |
| `NotificationCategory` | Enum | `TRANSACTIONAL`, `ALERT`, `MARKETING` |
| `RecipientType` | Enum | `USER`, `ORGANIZATION`, `EXTERNAL_EMAIL`, `PHONE_NUMBER` |

---

### 2.3 Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `NotificationDeliveredEvent` | Delivery marked `DELIVERED` | Internal audit only |
| `NotificationPermanentlyFailedEvent` | Delivery marked `PERMANENTLY_FAILED` | `admin` (platform audit log alert) |

> [!NOTE]
> This module is almost entirely a consumer. It publishes very few domain events — its primary output is external channel delivery.

---

### 2.4 Domain Exceptions

```java
public class TemplateNotFoundException extends NotFoundException {
    public TemplateNotFoundException(String code) {
        super("Notification template not found: " + code);
    }
}

public class TemplateRenderException extends DomainException {
    public TemplateRenderException(String templateCode, String cause) {
        super("Failed to render template '" + templateCode + "': " + cause);
    }
    public TemplateRenderException(String message, Throwable cause) { super(message, cause); }
}

public class DeliveryFailedException extends DomainException {
    public DeliveryFailedException(String message) { super(message); }
    public DeliveryFailedException(String message, Throwable cause) { super(message, cause); }
}

public class ProviderUnavailableException extends DomainException {
    public ProviderUnavailableException(String provider) {
        super("Notification provider unavailable: " + provider);
    }
    public ProviderUnavailableException(String message, Throwable cause) { super(message, cause); }
}

public class InvalidRecipientException extends ValidationException {
    public InvalidRecipientException(String message) { super(message); }
}
```

---

## 3. Application Layer

### 3.1 Commands

#### `SendNotificationCommand` — listener-triggered, void response

```java
public record SendNotificationCommand(
    String templateCode,
    RecipientType recipientType,
    String recipientId,
    Map<String, String> variables
) {}
```

**Handler:** `SendNotificationHandler extends Command<SendNotificationCommand, Void>`
- Loads template by `templateCode` → throws `TemplateNotFoundException` if absent or inactive
- Consults `UserNotificationPreference` for the recipient — skips channels not opted-in
- Renders Mustache template → throws `TemplateRenderException` on failure
- Persists `NotificationDelivery` (status = PENDING)
- Calls the appropriate channel adapter (`SmtpEmailSenderAdapter`, `SmsGatewayAdapter`, `PushNotificationAdapter`)
- On success: calls `delivery.markDelivered()`; on failure: calls `delivery.markFailed()` and schedules retry

---

#### `RetryFailedDeliveryCommand` — scheduler-triggered, void response

```java
public record RetryFailedDeliveryCommand(
    Long deliveryId
) {}
```

**Handler:** `RetryFailedDeliveryHandler extends Command<RetryFailedDeliveryCommand, Void>`
- Loads `NotificationDelivery` by `deliveryId`
- If `attemptCount >= 5`, calls `delivery.markPermanentlyFailed()`
- Otherwise computes next exponential backoff delay and calls `delivery.retry(nextRetryAt)`
- Re-invokes the channel adapter

---

#### `UpdateNotificationPreferenceCommand` — self-service, void response

```java
public record UpdateNotificationPreferenceCommand(
    Long userId,
    String eventType,
    NotificationChannel channel,
    boolean enabled
) {}
```

**Handler:** `UpdateNotificationPreferenceHandler extends Command<UpdateNotificationPreferenceCommand, Void>`
- Upserts `UserNotificationPreference` for `(userId, eventType, channel)`
- No special RBAC — users manage their own preferences

---

#### `CreateNotificationTemplateCommand` — admin-only

```java
public record CreateNotificationTemplateCommand(
    String code,
    NotificationCategory category,
    NotificationChannel channel,
    String subject,
    String bodyTemplate
) {}
```

**Handler:** `CreateNotificationTemplateHandler extends Command<CreateNotificationTemplateCommand, CreateNotificationTemplateResponse>`
- Checks no active template exists for `code` → throws `ConflictException` if duplicate
- Calls `NotificationTemplate.create()`
- Persists and returns `CreateNotificationTemplateResponse(Long templateId)`
- **RBAC:** `@PreAuthorize("hasAuthority('notifications:templates:manage')")`

---

#### `UpdateNotificationTemplateCommand` — admin-only

```java
public record UpdateNotificationTemplateCommand(
    Long templateId,
    String subject,
    String bodyTemplate
) {}
```

**Handler:** `UpdateNotificationTemplateHandler extends Command<UpdateNotificationTemplateCommand, Void>`
- Loads template → throws `TemplateNotFoundException` if not found
- Calls `template.updateTemplate(subject, bodyTemplate)`
- **RBAC:** `@PreAuthorize("hasAuthority('notifications:templates:manage')")`

---

### 3.2 Queries

#### `GetDeliveryStatusQuery`

```java
public record GetDeliveryStatusQuery(Long deliveryId) {}
```

**Handler:** `GetDeliveryStatusHandler extends Query<GetDeliveryStatusQuery, DeliveryStatusResult>`
**Result:** `DeliveryStatusResult(Long deliveryId, DeliveryStatus status, Integer attemptCount, ZonedDateTime deliveredAt, String failureReason)`
**Returns:** single object — delivery lookup by ID.

---

#### `ListFailedDeliveriesQuery`

```java
public record ListFailedDeliveriesQuery(
    ZonedDateTime dateFrom,
    ZonedDateTime dateTo
) {}
```

**Handler:** `ListFailedDeliveriesHandler extends Query<ListFailedDeliveriesQuery, List<FailedDeliveryResult>>`
**Returns:** `List<FailedDeliveryResult>` — admin monitoring view. Bounded by time range; not paginated because it is an ops tool for bounded time windows, not a consumer-facing infinite scroll.

---

#### `GetUserPreferencesQuery`

```java
public record GetUserPreferencesQuery(Long userId) {}
```

**Handler:** `GetUserPreferencesHandler extends Query<GetUserPreferencesQuery, List<NotificationPreferenceResult>>`
**Returns:** `List<NotificationPreferenceResult>` — the number of preferences per user is naturally bounded (finite event types × channels), so `List<T>` is appropriate.

---

#### `ListTemplatesQuery`

```java
public record ListTemplatesQuery(
    NotificationChannel channel,
    NotificationCategory category
) {}
```

**Handler:** `ListTemplatesHandler extends Query<ListTemplatesQuery, List<NotificationTemplateResult>>`
**Returns:** `List<NotificationTemplateResult>` — platform-defined templates are a small bounded set; admins manage them through a table, not a pageable stream.

---

### 3.3 Application Port

```java
// com.atlashub.notifications.application.port
public interface NotificationDispatchPort {
    void dispatch(String templateCode, RecipientType recipientType,
                  String recipientId, Map<String, String> variables);
}
```

Implemented in `infrastructure/services/NotificationDispatchAdapter`. Used by `SendNotificationHandler` as an internal seam for testability.

---

## 4. Infrastructure Layer

### 4.1 Persistence

#### JPA Entities

| JPA Entity | Domain Type | Table |
|---|---|---|
| `NotificationDeliveryJpaEntity` | `NotificationDelivery` | `notification_deliveries` |
| `NotificationTemplateJpaEntity` | `NotificationTemplate` | `notification_templates` |
| `UserNotificationPreferenceJpaEntity` | `UserNotificationPreference` | `user_notification_preferences` |

All JPA entities use `@Version` for optimistic locking.

#### Spring Data Repositories

```java
// infrastructure/persistence/repositories/
NotificationDeliveryJpaRepository extends JpaRepository<NotificationDeliveryJpaEntity, Long> {
    List<NotificationDeliveryJpaEntity> findByStatusAndNextRetryAtBefore(DeliveryStatus, ZonedDateTime);
    List<NotificationDeliveryJpaEntity> findByStatusInAndCreatedAtBetween(List<DeliveryStatus>, ZonedDateTime, ZonedDateTime);
}
NotificationTemplateJpaRepository extends JpaRepository<NotificationTemplateJpaEntity, Long> {
    Optional<NotificationTemplateJpaEntity> findByCodeAndIsActiveTrue(String code);
    List<NotificationTemplateJpaEntity> findByChannelAndCategory(NotificationChannel, NotificationCategory);
}
UserNotificationPreferenceJpaRepository extends JpaRepository<UserNotificationPreferenceJpaEntity, Long> {
    List<UserNotificationPreferenceJpaEntity> findByUserId(Long userId);
    Optional<UserNotificationPreferenceJpaEntity> findByUserIdAndEventTypeAndChannel(Long, String, NotificationChannel);
}
```

#### Persistence Adapters

```java
// infrastructure/persistence/adapters/
NotificationDeliveryPersistenceAdapter  // implements NotificationDeliveryRepository (domain port)
NotificationTemplatePersistenceAdapter  // implements NotificationTemplateRepository (domain port)
UserPreferencePersistenceAdapter        // implements UserPreferenceRepository (domain port)
```

#### Mappers

```java
// infrastructure/persistence/mappers/
NotificationDeliveryMapper
NotificationTemplateMapper
UserNotificationPreferenceMapper
```

---

### 4.2 Kafka Listeners

All listeners extend `BaseKafkaEventListener` and follow the `processEventIfMatches` pattern.

#### `AuthVerificationListener`

| Property | Value |
|---|---|
| Topic | `auth-events` |
| Group ID | `notifications-auth-group` |
| Events handled | `UserCreatedEvent`, `PasswordResetInitiatedEvent`, `AccountLockedEvent` |
| Commands invoked | `SendNotificationCommand` |

**Flow:** On `UserCreatedEvent` → sends template `WELCOME_EMAIL` to the new user's email. On `PasswordResetInitiatedEvent` → sends `PASSWORD_RESET` via Email. On `AccountLockedEvent` → sends `ACCOUNT_LOCKED` via Email + SMS.

---

#### `ComplianceNotificationListener`

| Property | Value |
|---|---|
| Topic | `compliance-events` |
| Group ID | `notifications-compliance-group` |
| Events handled | `ComplianceSubmittedEvent`, `OrganizationComplianceApprovedEvent`, `OrganizationComplianceRejectedEvent` |
| Commands invoked | `SendNotificationCommand` |

**Flow:** On approval → sends `KYC_APPROVED` via Email + SMS. On rejection → sends `KYC_REJECTED` via Email including `rejectionReason`.

---

#### `BillingNotificationListener`

| Property | Value |
|---|---|
| Topic | `billing-events` |
| Group ID | `notifications-billing-group` |
| Events handled | `InvoiceIssuedEvent`, `InvoiceOverdueEvent`, `SubscriptionSuspendedEvent`, `SubscriptionActivatedEvent` |
| Commands invoked | `SendNotificationCommand` |

**Flow:** On `InvoiceIssuedEvent` → sends `BILLING_INVOICE` via Email to org owner. On `InvoiceOverdueEvent` → sends `INVOICE_OVERDUE` via Email + SMS.

---

#### `CommerceReceiptListener`

| Property | Value |
|---|---|
| Topic | `commerce-events` |
| Group ID | `notifications-commerce-group` |
| Events handled | `PosSaleCompletedEvent`, `OnlineOrderCreatedEvent` |
| Commands invoked | `SendNotificationCommand` |

**Flow:** `PosSaleCompletedEvent` → sends `SALES_RECEIPT` via Email (and SMS if preference set). `OnlineOrderCreatedEvent` → sends `ORDER_CONFIRMED` via Email + SMS.

---

#### `LogisticsTrackingListener`

| Property | Value |
|---|---|
| Topic | `logistics-events` |
| Group ID | `notifications-logistics-group` |
| Events handled | `ShipmentCreatedEvent`, `ShipmentDispatchedEvent`, `ShipmentDeliveredEvent`, `ShipmentFailedEvent` |
| Commands invoked | `SendNotificationCommand` |

---

#### `HrPayrollListener`

| Property | Value |
|---|---|
| Topic | `hr-events` |
| Group ID | `notifications-hr-group` |
| Events handled | `EmployeeOnboardedEvent`, `PayrollDisbursedEvent`, `LeaveApprovedEvent`, `LeaveRejectedEvent`, `EmployeeTerminatedEvent`, `EmployeeSuspendedEvent` |
| Commands invoked | `SendNotificationCommand` |

**Flow:**
- `PayrollDisbursedEvent` → sends `SALARY_PAID` via SMS + Email to each employee with payslip summary
- `LeaveApprovedEvent` → sends `LEAVE_APPROVED` via Email + Push
- `EmployeeTerminatedEvent` → sends `EMPLOYMENT_TERMINATED` email to employee and HR manager
- `EmployeeSuspendedEvent` → sends `EMPLOYMENT_SUSPENDED` email to employee and HR manager

---

#### `PayEventListener`

| Property | Value |
|---|---|
| Topic | `pay-events` |
| Group ID | `notifications-pay-group` |
| Events handled | `WalletFundedEvent`, `MandateChargedEvent`, `MandatePausedEvent`, `MandateResumedEvent`, `MandateRevokedEvent`, `MandateExpiredEvent`, `SettlementConfirmedEvent` |
| Commands invoked | `SendNotificationCommand` |

**Flow:**
- `WalletFundedEvent` → sends `WALLET_FUNDED` email + push to org admin (`orgAdminEmail`) with sender name, amount, and new balance
- `MandateChargedEvent` → sends `MANDATE_CHARGED` receipt email to customer (`email` field in payload)
- `MandatePausedEvent` / `MandateResumedEvent` / `MandateRevokedEvent` → sends status update email to customer
- `MandateExpiredEvent` → sends `MANDATE_EXPIRED` email to customer and org admin
- `SettlementConfirmedEvent` → sends `SETTLEMENT_CONFIRMED` email to org admin with settlement amount and date

---

#### `AdminEventListener`

| Property | Value |
|---|---|
| Topic | `admin-events` |
| Group ID | `notifications-admin-group` |
| Events handled | `OrganizationBannedEvent`, `OrganizationUnbannedEvent` |
| Commands invoked | `SendNotificationCommand` |

**Flow:**
- `OrganizationBannedEvent` → sends `ACCOUNT_SUSPENDED` email to org owner with reason and next steps
- `OrganizationUnbannedEvent` → sends `ACCOUNT_REINSTATED` email to org owner

---

### 4.3 Scheduler

#### `RetryDeliveryScheduler`

```java
@Scheduled(cron = "0 */5 * * * *")   // every 5 minutes
public void retryFailedDeliveries()
```

- Queries `NotificationDeliveryJpaRepository.findByStatusAndNextRetryAtBefore(RETRYING, now())`
- For each delivery: invokes `RetryFailedDeliveryHandler.execute(new RetryFailedDeliveryCommand(delivery.getId()))`

---

### 4.4 Infrastructure Services (Channel Adapters)

```java
// infrastructure/services/

SmtpEmailSenderAdapter    // sends via SMTP relay; implements EmailSenderPort
SmsGatewayAdapter         // Termii primary, Twilio fallback; implements SmsSenderPort
PushNotificationAdapter   // Firebase FCM; implements PushSenderPort
```

Each adapter:
1. Attempts the primary provider
2. On failure, attempts the fallback provider (Email/SMS only)
3. Returns `ProviderResult(success, providerMessageId, failureReason)`

---

### 4.5 WebSocket Infrastructure

#### `SelectiveWebSocketBroadcaster`

Lives in `infrastructure/messaging/broadcasters/`. It is the **sole WebSocket push component** — no other module pushes to WebSocket directly.

```java
@Slf4j
@Component
public class SelectiveWebSocketBroadcaster extends BaseKafkaEventListener {

    private static final String GROUP_ID = "websocket-broadcaster";

    @KafkaListener(
        topics = {"pay-events", "commerce-events", "hr-events",
                  "logistics-events", "accounting-events", "support-events"},
        groupId = GROUP_ID
    )
    public void onEvent(String payload) { ... }
}
```

**Subscription Table:**

| Event | Topic | WebSocket Destination | Push Type |
|---|---|---|---|
| `ChargeSuccessfulEvent` | `pay-events` | `/user/{cashierId}/queue/notifications` | `PAYMENT_CONFIRMED` |
| `ChargeFailedEvent` | `pay-events` | `/user/{cashierId}/queue/notifications` | `PAYMENT_FAILED` |
| `KotReadyEvent` | `commerce-events` | `/topic/outlet/{outletId}/kitchen` | `KOT_READY` |
| `ShipmentLocationUpdatedEvent` | `logistics-events` | `/topic/shipment/{trackingNumber}` | `LOCATION_UPDATE` |
| `ShipmentAssignedEvent` | `logistics-events` | `/user/{riderId}/queue/notifications` | `SHIPMENT_ASSIGNED` |
| `PayrollPendingApprovalEvent` | `hr-events` | `/user/{approverId}/queue/notifications` | `APPROVAL_REQUIRED` |
| `JournalEntryPendingApprovalEvent` | `accounting-events` | `/user/{approverId}/queue/notifications` | `APPROVAL_REQUIRED` |
| `TicketCreatedEvent` | `support-events` | `/topic/org/{orgId}/support` | `TICKET_CREATED` |
| `InvoiceIssuedEvent` | `billing-events` | `/user/{orgOwnerId}/queue/notifications` | `INVOICE_ISSUED` |

**`PushNotification` transfer object** (what the browser/mobile app receives):
```java
public record PushNotification(
    String type,          // e.g. "PAYMENT_CONFIRMED"
    String message,       // human-readable, e.g. "Payment of ₦5,000 confirmed"
    Map<String, Object> data,   // entity IDs, extra context
    String timestamp      // ISO-8601 ZonedDateTime string
)
```

---

#### `WebSocketConfig`

Lives in `infrastructure/config/`. Configures the STOMP broker:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("https://*.atlashub.io", "http://localhost:*")
            .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketAuthInterceptor());
    }
}
```

---

#### `WebSocketAuthInterceptor`

Lives in `infrastructure/security/`. Validates the JWT `Bearer` token on the STOMP `CONNECT` frame. Rejects connections with missing, expired, or revoked tokens. Sets `AtlasHubPrincipal(userId, orgId)` on the session for `/user/{userId}/queue` routing.

---

## 5. Presentation Layer

### 5.1 Controllers

#### `NotificationPreferenceController` — `/api/v1/notifications/preferences`

```java
@RestController
@RequestMapping("/api/v1/notifications/preferences")
@Tag(name = "Notification Preferences")
public class NotificationPreferenceController { ... }
```

| Method | Path | Auth | RBAC | Description |
|---|---|---|---|---|
| `GET` | `/` | Bearer | self-service | Get caller's notification preferences |
| `PUT` | `/` | Bearer | self-service | Update a single preference (eventType + channel + enabled) |

---

#### `NotificationTemplateController` — `/api/v1/admin/notifications/templates`

```java
@RestController
@RequestMapping("/api/v1/admin/notifications/templates")
@Tag(name = "Notification Templates (Admin)")
public class NotificationTemplateController { ... }
```

| Method | Path | Auth | RBAC | Description |
|---|---|---|---|---|
| `POST` | `/` | Bearer | `notifications:templates:manage` | Create a new template |
| `PUT` | `/{templateId}` | Bearer | `notifications:templates:manage` | Update template body/subject |
| `GET` | `/` | Bearer | `notifications:templates:manage` | List templates (filterable by channel/category) |

---

#### `NotificationDeliveryController` — `/api/v1/admin/notifications/deliveries`

```java
@RestController
@RequestMapping("/api/v1/admin/notifications/deliveries")
@Tag(name = "Notification Deliveries (Admin)")
public class NotificationDeliveryController { ... }
```

| Method | Path | Auth | RBAC | Description |
|---|---|---|---|---|
| `GET` | `/{deliveryId}` | Bearer | `notifications:templates:manage` | Get single delivery status |
| `GET` | `/failed` | Bearer | `notifications:templates:manage` | List failed deliveries in a date range |

---

### 5.2 Request/Response DTOs

#### `UpdatePreferenceRequest`
```java
public record UpdatePreferenceRequest(
    @NotBlank String eventType,
    @NotNull NotificationChannel channel,
    boolean enabled
) {}
```

#### `NotificationPreferenceDto`
```java
public record NotificationPreferenceDto(
    Long id,
    String eventType,
    NotificationChannel channel,
    boolean enabled
) {}
```

#### `CreateTemplateRequest`
```java
public record CreateTemplateRequest(
    @NotBlank String code,
    @NotNull NotificationCategory category,
    @NotNull NotificationChannel channel,
    String subject,           // required for EMAIL channel
    @NotBlank String bodyTemplate
) {}
```

#### `UpdateTemplateRequest`
```java
public record UpdateTemplateRequest(
    String subject,
    @NotBlank String bodyTemplate
) {}
```

#### `NotificationTemplateDto`
```java
public record NotificationTemplateDto(
    Long id,
    String code,
    NotificationCategory category,
    NotificationChannel channel,
    String subject,
    String bodyTemplate,
    Integer version,
    Boolean isActive,
    ZonedDateTime updatedAt
) {}
```

#### `CreateNotificationTemplateResponse`
```java
public record CreateNotificationTemplateResponse(Long templateId) {}
```

#### `DeliveryStatusResult`
```java
public record DeliveryStatusResult(
    Long deliveryId,
    String templateCode,
    DeliveryStatus status,
    Integer attemptCount,
    ZonedDateTime deliveredAt,
    String failureReason,
    ZonedDateTime nextRetryAt
) {}
```

#### `FailedDeliveryResult`
```java
public record FailedDeliveryResult(
    Long deliveryId,
    String templateCode,
    RecipientType recipientType,
    String recipientId,
    NotificationChannel channel,
    String provider,
    Integer attemptCount,
    String failureReason,
    ZonedDateTime createdAt
) {}
```

---

## 6. RBAC Table

| Operation | Permission Required | Notes |
|---|---|---|
| Update own notification preferences | none — self-service | Caller's `userId` from JWT |
| Get own preferences | none — self-service | |
| Create notification template | `notifications:templates:manage` | Admin staff only |
| Update notification template | `notifications:templates:manage` | Admin staff only |
| List templates | `notifications:templates:manage` | Admin staff only |
| Get delivery status | `notifications:templates:manage` | Admin monitoring |
| List failed deliveries | `notifications:templates:manage` | Admin monitoring |

---

## 7. Maker-Checker

This module has no Maker-Checker operations. Notification template creation and updates are single-approver admin actions.

---

## 8. Socket Events

All WebSocket push decisions are made exclusively by `SelectiveWebSocketBroadcaster`. Modules publish domain events to Kafka; the broadcaster decides whether to push.

| Event Trigger | WebSocket Topic | `PushNotification.type` | `data` Fields |
|---|---|---|---|
| `ChargeSuccessfulEvent` | `/user/{cashierId}/queue/notifications` | `PAYMENT_CONFIRMED` | `amount`, `reference` |
| `ChargeFailedEvent` | `/user/{cashierId}/queue/notifications` | `PAYMENT_FAILED` | `reason`, `reference` |
| `KotReadyEvent` | `/topic/outlet/{outletId}/kitchen` | `KOT_READY` | `kotId`, `tableNumber` |
| `ShipmentLocationUpdatedEvent` | `/topic/shipment/{trackingNumber}` | `LOCATION_UPDATE` | `location`, `status` |
| `ShipmentAssignedEvent` | `/user/{riderId}/queue/notifications` | `SHIPMENT_ASSIGNED` | `shipmentId`, `trackingNumber` |
| `PayrollPendingApprovalEvent` | `/user/{approverId}/queue/notifications` | `APPROVAL_REQUIRED` | `entityType: "PAYROLL_RUN"`, `entityId` |
| `JournalEntryPendingApprovalEvent` | `/user/{approverId}/queue/notifications` | `APPROVAL_REQUIRED` | `entityType: "JOURNAL_ENTRY"`, `entityId` |
| `TicketCreatedEvent` | `/topic/org/{orgId}/support` | `TICKET_CREATED` | `priority`, `subject` |
| `InvoiceIssuedEvent` | `/user/{orgOwnerId}/queue/notifications` | `INVOICE_ISSUED` | `invoiceId`, `amount`, `dueDate` |

---

## 9. Domain Events Table

| Event | Published When | Consumed By |
|---|---|---|
| `NotificationDeliveredEvent` | Delivery transitions to `DELIVERED` | Internal audit only |
| `NotificationPermanentlyFailedEvent` | 5th retry exhausted | `admin` (platform audit log) |

---

## 10. Distributed Architecture

### Idempotency (Inbox)
All Kafka listeners use `EventDeliveryTracker` with `(eventId, groupId)` as the idempotency key. A notification is sent **at most once** per event instance, even if Kafka delivers the event multiple times (at-least-once guarantee).

### Outbox
`NotificationPermanentlyFailedEvent` is written via the Outbox pattern to ensure the audit log entry is delivered even if the application crashes mid-processing.

### Locking
`NotificationDelivery` uses `@Version` (optimistic locking). The retry scheduler may select the same delivery record across concurrent scheduler firings — the version check ensures only one wins.

### Provider Circuit Breaker
Channel adapters implement primary→fallback failover. Both failure paths are logged with structured fields (`provider`, `channel`, `templateCode`, `deliveryId`) for alerting.

### Retry Scheduler — Exponential Backoff Delays

| Attempt | Delay |
|---|---|
| 1 | 5 seconds |
| 2 | 30 seconds |
| 3 | 5 minutes |
| 4 | 30 minutes |
| 5 | 2 hours → `PERMANENTLY_FAILED` |

### WebSocket Horizontal Scaling
The in-memory STOMP broker works on a single node. For multi-node deployments, replace `enableSimpleBroker` with `enableStompBrokerRelay` pointing to a RabbitMQ instance with the STOMP plugin enabled. This ensures WebSocket messages are delivered regardless of which JVM node handles the Kafka consumer.

---

## 11. Complete File List

```
atlashub-platform/notifications/src/main/java/com/atlashub/notifications/
│
├── domain/
│   ├── entities/
│   │   ├── NotificationDelivery.java
│   │   ├── NotificationTemplate.java
│   │   └── UserNotificationPreference.java
│   ├── events/
│   │   ├── NotificationDeliveredEvent.java
│   │   └── NotificationPermanentlyFailedEvent.java
│   ├── exceptions/
│   │   ├── DeliveryFailedException.java
│   │   ├── InvalidRecipientException.java
│   │   ├── ProviderUnavailableException.java
│   │   ├── TemplateNotFoundException.java
│   │   └── TemplateRenderException.java
│   ├── repositories/
│   │   ├── NotificationDeliveryRepository.java
│   │   ├── NotificationTemplateRepository.java
│   │   └── UserPreferenceRepository.java
│   └── valueobject/
│       ├── DeliveryStatus.java
│       ├── NotificationCategory.java
│       ├── NotificationChannel.java
│       └── RecipientType.java
│
├── application/
│   ├── commands/
│   │   ├── SendNotification/
│   │   │   ├── SendNotificationCommand.java
│   │   │   └── SendNotificationHandler.java
│   │   ├── RetryFailedDelivery/
│   │   │   ├── RetryFailedDeliveryCommand.java
│   │   │   └── RetryFailedDeliveryHandler.java
│   │   ├── UpdateNotificationPreference/
│   │   │   ├── UpdateNotificationPreferenceCommand.java
│   │   │   └── UpdateNotificationPreferenceHandler.java
│   │   ├── CreateNotificationTemplate/
│   │   │   ├── CreateNotificationTemplateCommand.java
│   │   │   ├── CreateNotificationTemplateHandler.java
│   │   │   └── CreateNotificationTemplateResponse.java
│   │   └── UpdateNotificationTemplate/
│   │       ├── UpdateNotificationTemplateCommand.java
│   │       └── UpdateNotificationTemplateHandler.java
│   ├── queries/
│   │   ├── GetDeliveryStatus/
│   │   │   ├── GetDeliveryStatusQuery.java
│   │   │   ├── GetDeliveryStatusHandler.java
│   │   │   └── DeliveryStatusResult.java
│   │   ├── ListFailedDeliveries/
│   │   │   ├── ListFailedDeliveriesQuery.java
│   │   │   ├── ListFailedDeliveriesHandler.java
│   │   │   └── FailedDeliveryResult.java
│   │   ├── GetUserPreferences/
│   │   │   ├── GetUserPreferencesQuery.java
│   │   │   ├── GetUserPreferencesHandler.java
│   │   │   └── NotificationPreferenceResult.java
│   │   └── ListTemplates/
│   │       ├── ListTemplatesQuery.java
│   │       ├── ListTemplatesHandler.java
│   │       └── NotificationTemplateResult.java
│   └── port/
│       └── NotificationDispatchPort.java
│
├── infrastructure/
│   ├── config/
│   │   └── WebSocketConfig.java
│   ├── messaging/
│   │   ├── broadcasters/
│   │   │   └── SelectiveWebSocketBroadcaster.java
│   │   └── listeners/
│   │       ├── AuthVerificationListener.java
│   │       ├── BillingNotificationListener.java
│   │       ├── CommerceReceiptListener.java
│   │       ├── ComplianceNotificationListener.java
│   │       ├── HrPayrollListener.java
│   │       └── LogisticsTrackingListener.java
│   ├── persistence/
│   │   ├── adapters/
│   │   │   ├── NotificationDeliveryPersistenceAdapter.java
│   │   │   ├── NotificationTemplatePersistenceAdapter.java
│   │   │   └── UserPreferencePersistenceAdapter.java
│   │   ├── entities/
│   │   │   ├── NotificationDeliveryJpaEntity.java
│   │   │   ├── NotificationTemplateJpaEntity.java
│   │   │   └── UserNotificationPreferenceJpaEntity.java
│   │   ├── mappers/
│   │   │   ├── NotificationDeliveryMapper.java
│   │   │   ├── NotificationTemplateMapper.java
│   │   │   └── UserNotificationPreferenceMapper.java
│   │   └── repositories/
│   │       ├── NotificationDeliveryJpaRepository.java
│   │       ├── NotificationTemplateJpaRepository.java
│   │       └── UserNotificationPreferenceJpaRepository.java
│   ├── schedulers/
│   │   └── RetryDeliveryScheduler.java
│   ├── security/
│   │   └── WebSocketAuthInterceptor.java
│   └── services/
│       ├── PushNotificationAdapter.java
│       ├── SmtpEmailSenderAdapter.java
│       └── SmsGatewayAdapter.java
│
└── presentation/
    ├── dto/
    │   ├── CreateTemplateRequest.java
    │   ├── FailedDeliveryResult.java
    │   ├── NotificationPreferenceDto.java
    │   ├── NotificationTemplateDto.java
    │   ├── UpdatePreferenceRequest.java
    │   └── UpdateTemplateRequest.java
    └── rest/
        ├── NotificationDeliveryController.java
        ├── NotificationPreferenceController.java
        └── NotificationTemplateController.java
```
