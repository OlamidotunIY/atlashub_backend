# Outbound Webhook Infrastructure

## Overview

When significant events occur in AtlasHub — a payment is confirmed, a payout completes, a delivery is done — organizations can receive real-time notifications on their own servers via **outbound webhooks**. AtlasHub POSTs a JSON payload to the organization's registered URL with an HMAC-SHA256 signature for verification.

This is how Paystack, Stripe, and every major API platform notifies integrators.

---

## Webhook Subscription

### Registering a Webhook Endpoint
Organizations register via the API or dashboard:

```http
POST /api/v1/webhooks/subscriptions
Authorization: Bearer {jwt}

{
  "url": "https://myserver.com/atlashub/webhook",
  "events": ["charge.successful", "charge.failed", "payout.completed", "payout.failed"],
  "description": "Production payment events"
}
```

Response includes a `secretKey` shown once — the organization uses this to verify incoming webhook signatures.

### Available Events

| Event Name | Triggered When |
|---|---|
| `charge.successful` | Card/transfer payment confirmed |
| `charge.failed` | Payment failed or declined |
| `charge.refunded` | Refund processed |
| `payout.completed` | Bank transfer to recipient confirmed |
| `payout.failed` | Bank transfer failed |
| `mandate.charged` | Recurring mandate charge processed |
| `mandate.revoked` | Recurring authorization revoked |
| `settlement.confirmed` | Paystack/Moniepoint settles funds |
| `shipment.delivered` | Delivery confirmed with POD |
| `shipment.failed` | Delivery failed |
| `virtual_account.funded` | NUBAN received an inbound transfer |

---

## Webhook Payload Format

```json
{
  "event": "charge.successful",
  "eventId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "timestamp": "2026-09-17T14:23:00+01:00",
  "data": {
    "chargeId": 12345,
    "reference": "CHG-2026-abc123",
    "amount": 25000.00,
    "currency": "NGN",
    "channel": "CARD",
    "customerId": "cust_xyz",
    "metadata": {
      "orderId": "ORD-1042",
      "customField": "value"
    }
  }
}
```

---

## Delivery Architecture

### Delivery Flow

```
Domain Event (Kafka) 
    → WebhookDeliveryListener 
    → finds all WebhookSubscriptions for org + event type
    → creates WebhookDelivery record per subscription (status: PENDING)
    → WebhookDeliveryWorker (scheduled every 5s) picks up PENDING deliveries
    → HTTP POST to merchant URL with signed payload
    → On 2xx: marks DELIVERED
    → On non-2xx or timeout: marks RETRYING, schedules next retry
    → After 5 retries: marks PERMANENTLY_FAILED, alerts org
```

### Retry Schedule (Exponential Backoff)
```
Attempt 1: immediate
Attempt 2: 30 seconds later
Attempt 3: 5 minutes later
Attempt 4: 30 minutes later
Attempt 5: 2 hours later
After 5 failures: PERMANENTLY_FAILED — org notified via email
```

---

## HMAC Signature on Outbound Webhooks

The organization verifies that incoming webhook calls are genuinely from AtlasHub using the `X-AtlasHub-Signature` header:

### Signature Generation (AtlasHub side)
```java
@Component
public class WebhookDeliveryWorker {

    @Scheduled(fixedDelay = 5000)
    public void processDeliveries() {
        List<WebhookDelivery> pending = webhookDeliveryRepository.findPendingDue(ZonedDateTime.now());

        for (WebhookDelivery delivery : pending) {
            WebhookSubscription subscription = webhookSubscriptionRepository
                .findById(delivery.getSubscriptionId()).orElseThrow();

            String payload = delivery.getPayload();
            String signature = hmacSha256(payload, subscription.getSecretKey());

            try {
                HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                        .uri(URI.create(subscription.getUrl()))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .header("X-AtlasHub-Signature", "sha256=" + signature)
                        .header("X-AtlasHub-Event", delivery.getEventType())
                        .header("X-AtlasHub-Delivery", delivery.getId().toString())
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    delivery.markDelivered(response.statusCode());
                } else {
                    scheduleRetry(delivery, "HTTP " + response.statusCode());
                }

            } catch (Exception e) {
                scheduleRetry(delivery, e.getMessage());
            }

            webhookDeliveryRepository.save(delivery);
        }
    }
}
```

### Signature Verification (Merchant side)
```javascript
// Node.js example
const crypto = require('crypto');

function verifyWebhook(payload, signatureHeader, secretKey) {
    const expected = 'sha256=' + crypto
        .createHmac('sha256', secretKey)
        .update(payload)
        .digest('hex');

    return crypto.timingSafeEqual(
        Buffer.from(expected),
        Buffer.from(signatureHeader)
    );
}

// In Express handler:
app.post('/atlashub/webhook', (req, res) => {
    const rawBody = req.rawBody; // must be raw string, not parsed JSON
    const signature = req.headers['x-atlashub-signature'];

    if (!verifyWebhook(rawBody, signature, process.env.ATLASHUB_WEBHOOK_SECRET)) {
        return res.status(401).send('Invalid signature');
    }

    const event = JSON.parse(rawBody);
    switch (event.event) {
        case 'charge.successful':
            // handle payment
            break;
        case 'payout.completed':
            // handle payout
            break;
    }

    res.status(200).send('OK');  // must respond 2xx within 10 seconds
});
```

---

## Webhook Management API

```http
# List subscriptions
GET /api/v1/webhooks/subscriptions

# Update subscription events
PATCH /api/v1/webhooks/subscriptions/{id}
{ "events": ["charge.successful", "charge.failed"] }

# Disable a subscription
DELETE /api/v1/webhooks/subscriptions/{id}

# View delivery history
GET /api/v1/webhooks/deliveries?subscriptionId={id}&status=FAILED

# Manually retry a failed delivery
POST /api/v1/webhooks/deliveries/{id}/retry

# Test endpoint (sends a test event to verify your URL is reachable)
POST /api/v1/webhooks/subscriptions/{id}/test
```

---

## Idempotency for Merchants

Each webhook delivery has a unique `X-AtlasHub-Delivery` header. Merchants should record processed delivery IDs to deduplicate — AtlasHub may deliver the same event more than once (especially for retries).

```javascript
const processedDeliveries = new Set(); // use Redis in production

app.post('/atlashub/webhook', async (req, res) => {
    const deliveryId = req.headers['x-atlashub-delivery'];
    if (processedDeliveries.has(deliveryId)) {
        return res.status(200).send('Already processed');  // idempotent — return 200
    }
    processedDeliveries.add(deliveryId);
    // ... process event
});
```

---

## Security Recommendations for Merchants

1. **Always verify the HMAC signature** before processing the payload
2. **Respond with 2xx within 10 seconds** — if processing takes longer, acknowledge immediately and process asynchronously
3. **Idempotency-check the delivery ID** — AtlasHub may deliver the same event multiple times
4. **Only accept from AtlasHub IPs** (IP allowlist) — check AtlasHub's published IP range (future: AtlasHub will publish its outbound IP range)
5. **Use HTTPS** — AtlasHub will not deliver to HTTP endpoints (enforced by the URL validation on subscription creation)
