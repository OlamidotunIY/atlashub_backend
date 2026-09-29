# Pay Webhooks Design (`atlashub-pay:webhooks`)

## Role & Purpose

The `webhooks` subpackage delivers outbound payment event notifications to merchant servers. When payment events occur (charge successful, payout completed, etc.), AtlasHub calls the merchant's registered HTTPS endpoint with a signed payload. Delivery is guaranteed via retry with exponential backoff.

Gradle module: `atlashub-pay:webhooks`  
Package: `com.atlashub.pay.webhooks`

---

## Domain Layer

### `WebhookSubscription` (Aggregate Root)

**Package:** `com.atlashub.pay.webhooks.domain.entities`

```
WebhookSubscription
├── id              : Long
├── organizationId  : Long
├── url             : String                  ← HTTPS endpoint on merchant's server
├── events          : Set<WebhookEventType>   ← which events to receive
├── secretKey       : String                  ← merchant uses this to verify incoming signatures
├── status          : WebhookStatus           ← ACTIVE | DISABLED
├── createdAt       : ZonedDateTime
└── lastDeliveryAt  : ZonedDateTime           ← nullable
```

**Business methods:**
- `disable()` → ACTIVE → DISABLED
- `enable()` → DISABLED → ACTIVE
- `recordDelivery()` → updates `lastDeliveryAt`
- `updateUrl(newUrl)` → validates URL format; updates `url`
- `updateEvents(events)` → replaces event set

---

### `WebhookDelivery` (Entity)

```
WebhookDelivery
├── id             : Long
├── subscriptionId : Long
├── eventType      : WebhookEventType
├── payload        : String               ← JSON payload body
├── signature      : String               ← HMAC-SHA256(payload, secretKey)
├── httpStatus     : Integer              ← nullable; set after attempt
├── status         : DeliveryStatus       ← PENDING | DELIVERED | FAILED | RETRYING
├── attemptCount   : Integer
├── nextRetryAt    : ZonedDateTime        ← nullable; exponential backoff
└── createdAt      : ZonedDateTime
```

**Webhook event types:**
```
charge.successful
charge.failed
charge.refunded
payout.completed
payout.failed
mandate.charged
mandate.revoked
settlement.confirmed
```

**Delivery Guarantees:**
- Minimum once delivery with exponential backoff: 1s → 2s → 4s → 8s → 16s (max 5 retries)
- After 5 failed attempts: status = FAILED → org notified
- Merchant verifies authenticity via `X-AtlasHub-Signature` header = `HMAC-SHA256(payload, secretKey)`

---

### Domain Exceptions — `com.atlashub.pay.webhooks.domain.exceptions`

```java
public class WebhookSubscriptionNotFoundException extends NotFoundException {
    public WebhookSubscriptionNotFoundException(Long id) { super("Webhook subscription not found: " + id); }
}
public class InvalidWebhookUrlException extends ValidationException {
    public InvalidWebhookUrlException(String url) { super("Invalid webhook URL (must be HTTPS): " + url); }
}
public class WebhookAlreadyExistsException extends ConflictException {
    public WebhookAlreadyExistsException() { super("A webhook subscription already exists for this organization"); }
}
public class WebhookDeliveryNotFoundException extends NotFoundException {
    public WebhookDeliveryNotFoundException(Long id) { super("Webhook delivery not found: " + id); }
}
```

---

## Application Layer

### Commands — `com.atlashub.pay.webhooks.application.commands`

#### `CreateWebhookSubscriptionCommand`
```java
record CreateWebhookSubscriptionCommand(
    Long organizationId, String url, Set<WebhookEventType> events
)
```
**Handler:** `CreateWebhookSubscriptionHandler` | **Response:** `CreateWebhookSubscriptionResponse(Long subscriptionId, String secretKey)`  
**RBAC:** `@PreAuthorize("hasAuthority('pay:webhooks:manage')")`  
**Flow:** Validate URL format (must be HTTPS) → `InvalidWebhookUrlException`; check no existing subscription → `WebhookAlreadyExistsException`; generate `secretKey` (UUID-based); create `WebhookSubscription` → save; return `secretKey` (only shown once)

---

#### `UpdateWebhookSubscriptionCommand`
```java
record UpdateWebhookSubscriptionCommand(Long subscriptionId, String url, Set<WebhookEventType> events)
```
**Handler:** `UpdateWebhookSubscriptionHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('pay:webhooks:manage')")`

---

#### `DisableWebhookSubscriptionCommand`
```java
record DisableWebhookSubscriptionCommand(Long subscriptionId)
```
**Handler:** `DisableWebhookSubscriptionHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('pay:webhooks:manage')")`

---

#### `DeliverWebhookCommand`
```java
record DeliverWebhookCommand(Long subscriptionId, WebhookEventType eventType, String payloadJson)
```
**Handler:** `DeliverWebhookHandler` | **Response:** `void`  
**Invocation source:** Kafka listener (any pay event)  
**Flow:**
1. Load `WebhookSubscription` (must be ACTIVE and subscribed to this `eventType`)
2. Generate HMAC-SHA256 signature using `secretKey`
3. Create `WebhookDelivery` (status = PENDING)
4. Call `HttpWebhookDeliveryAdapter.deliver(url, payload, signature)`
5. If HTTP 2xx: `delivery.status = DELIVERED`; `subscription.recordDelivery()`
6. If failure: `delivery.status = RETRYING`; compute `nextRetryAt` (exponential backoff)
7. Save both `WebhookDelivery` and `WebhookSubscription`

---

#### `RetryWebhookDeliveryCommand`
```java
record RetryWebhookDeliveryCommand(Long deliveryId)
```
**Handler:** `RetryWebhookDeliveryHandler` | **Response:** `void`  
**Invocation source:** `WebhookRetryScheduler`  
**Flow:** Load `WebhookDelivery` → if `attemptCount >= 5` → mark FAILED → notify org; else: increment `attemptCount`, call `HttpWebhookDeliveryAdapter.deliver(...)`, update status

---

### Queries — `com.atlashub.pay.webhooks.application.queries`

#### `GetWebhookSubscriptionQuery`
```java
record GetWebhookSubscriptionQuery(Long organizationId)
```
**Handler:** `GetWebhookSubscriptionHandler` | **Result:** `WebhookSubscriptionResult`  
(One subscription per org — returns single)

---

#### `ListWebhookDeliveriesQuery`
```java
record ListWebhookDeliveriesQuery(Long subscriptionId, DeliveryStatus status, int page, int size)
```
**Handler:** `ListWebhookDeliveriesHandler` | **Result:** `PageResult<WebhookDeliveryResult>`  
**Justification:** Deliveries accumulate over time — pagination required.

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `WebhookSubscriptionJpaEntity` | `pay_webhook_subscriptions` | `@Version` optimistic |
| `WebhookDeliveryJpaEntity` | `pay_webhook_deliveries` | — |

**Spring Data:**
```
SpringDataWebhookSubscriptionRepository
  + findByOrganizationId(Long orgId): Optional<WebhookSubscriptionJpaEntity>

SpringDataWebhookDeliveryRepository
  + findBySubscriptionIdAndStatus(Long subscriptionId, DeliveryStatus status, Pageable p): Page<WebhookDeliveryJpaEntity>
  + findByStatusAndNextRetryAtBefore(DeliveryStatus status, ZonedDateTime now): List<WebhookDeliveryJpaEntity>
```

**Repository Adapters:**
- `WebhookSubscriptionRepositoryAdapter` → `pay_webhook_subscription_seq`
- `WebhookDeliveryRepositoryAdapter` → `pay_webhook_delivery_seq`

### Listeners — `infrastructure/messaging/listeners/`

All listeners consume from `pay-events` topic. Each looks up the org's `WebhookSubscription`, checks if the event type is subscribed, and calls `DeliverWebhookHandler`.

| Listener | Group ID | Event | Webhook Event Type |
|---|---|---|---|
| `ChargeSuccessfulWebhookListener` | `pay-webhook-charge-success` | `ChargeSuccessfulEvent` | `charge.successful` |
| `ChargeFailedWebhookListener` | `pay-webhook-charge-failed` | `ChargeFailedEvent` | `charge.failed` |
| `ChargeRefundedWebhookListener` | `pay-webhook-charge-refunded` | `ChargeRefundInitiatedEvent` | `charge.refunded` |
| `PayoutCompletedWebhookListener` | `pay-webhook-payout-completed` | `PayoutCompletedEvent` | `payout.completed` |
| `PayoutFailedWebhookListener` | `pay-webhook-payout-failed` | `PayoutFailedEvent` | `payout.failed` |
| `SettlementConfirmedWebhookListener` | `pay-webhook-settlement` | `SettlementConfirmedEvent` | `settlement.confirmed` |

### Infrastructure Services — `infrastructure/services/`

#### `HttpWebhookDeliveryAdapter`
```java
@Component
public class HttpWebhookDeliveryAdapter {
    // Makes outbound HTTP POST to merchant URL
    // Sets Content-Type: application/json
    // Sets X-AtlasHub-Signature: HMAC-SHA256(payload, secretKey)
    // Sets X-AtlasHub-Event: event type
    // Timeout: 10 seconds
    // Returns HTTP status code
    public WebhookDeliveryAttemptResult deliver(String url, String payload, String signature);
}
```

### Scheduler — `infrastructure/schedulers/`

#### `WebhookRetryScheduler`
| Attribute | Value |
|---|---|
| **Cron** | `*/1 * * * *` (every minute) |
| **Finds** | `WebhookDelivery` records with status `RETRYING` and `nextRetryAt` in the past |
| **Action** | Calls `RetryWebhookDeliveryHandler` for each |
| **Response** | `void` |

---

## Presentation Layer

### Controller: `WebhookController` — `/api/v1/pay/webhooks`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/webhooks` | `pay:webhooks:manage` | `CreateWebhookSubscriptionRequest` | `CreateWebhookSubscriptionResponse` |
| `PUT` | `/webhooks` | `pay:webhooks:manage` | `UpdateWebhookSubscriptionRequest` | `void` |
| `DELETE` | `/webhooks?id={subscriptionId}` | `pay:webhooks:manage` | — | `void` |
| `GET` | `/webhooks` | `pay:webhooks:manage` | `?orgId` | `WebhookSubscriptionResult` |
| `GET` | `/webhooks/{id}/deliveries` | `pay:webhooks:manage` | `?status&page&size` | `PageResult<WebhookDeliveryResult>` |

---

## RBAC Table

| Permission | Commands |
|---|---|
| `pay:webhooks:manage` | `CreateWebhookSubscriptionHandler`, `UpdateWebhookSubscriptionHandler`, `DisableWebhookSubscriptionHandler` |

---

## Distributed Architecture

### Idempotency
All webhook event listeners are keyed on `(eventId, groupId)` — duplicate events must not trigger duplicate webhook deliveries.

---

## Complete File List

```
atlashub-pay/webhooks/src/main/java/com/atlashub/pay/webhooks/
├── domain/
│   ├── entities/ [WebhookSubscription, WebhookDelivery]
│   ├── exceptions/ [WebhookSubscriptionNotFoundException, InvalidWebhookUrlException, WebhookAlreadyExistsException, WebhookDeliveryNotFoundException]
│   ├── repositories/ [WebhookSubscriptionRepository, WebhookDeliveryRepository]
│   └── valueobject/ [WebhookEventType, WebhookStatus, DeliveryStatus]
├── application/
│   ├── commands/ [CreateWebhookSubscription, UpdateWebhookSubscription, DisableWebhookSubscription, DeliverWebhook, RetryWebhookDelivery]
│   └── queries/ [GetWebhookSubscription, ListWebhookDeliveries]
├── infrastructure/
│   ├── messaging/
│   │   ├── events/ [ChargeSuccessfulPayload, ChargeFailedPayload, PayoutCompletedPayload, PayoutFailedPayload, SettlementConfirmedPayload]
│   │   └── listeners/ [ChargeSuccessfulWebhookListener, ChargeFailedWebhookListener, ChargeRefundedWebhookListener, PayoutCompletedWebhookListener, PayoutFailedWebhookListener, SettlementConfirmedWebhookListener]
│   ├── persistence/ [adapters, entities, mappers, repositories]
│   ├── schedulers/
│   │   └── WebhookRetryScheduler.java
│   └── services/
│       └── HttpWebhookDeliveryAdapter.java
└── presentation/
    ├── dto/ [CreateWebhookSubscriptionRequest, CreateWebhookSubscriptionResponse, UpdateWebhookSubscriptionRequest, WebhookSubscriptionResult, WebhookDeliveryResult]
    └── rest/
        └── WebhookController.java
```
