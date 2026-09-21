# WebSocket — Real-Time UI Push

> **Source**: *Designing Event-Driven Systems* — Ben Stopford, Ch. 5; *Building Microservices* — Sam Newman, Ch. 4 (Communication Styles); *WebSocket RFC 6455*; Spring Framework STOMP documentation.

---

## The Wrong Mental Model

A common mistake is to treat WebSocket as an internal event bus and forward every domain event to it. This is wrong for several reasons:

1. **Domain events are system-to-system signals.** They carry technical identifiers, are schema-versioned, and are intended for consumers that know how to react to them. A browser client should never be a domain event consumer.

2. **Most events have no real-time UI relevance.** A `JournalEntryPostedEvent`, a `BalanceSnapshotCreatedEvent`, or a `StockTransferDispatched` event have no human waiting for them in a browser. Pushing them wastes bandwidth and creates noise.

3. **Real-time push is expensive.** Each WebSocket message wakes up JavaScript event handlers in potentially hundreds of connected tabs. The threshold for pushing should be: *"Is a human actively waiting for this outcome right now?"*

---

## Principle: Push When a Human Is Waiting

WebSocket is the right channel when:
- A user initiated an action and is staring at a loading spinner
- A user is monitoring a live operational view (kitchen display, delivery tracker)
- An event requires **immediate human attention** (approval required, alert)

WebSocket is the **wrong** channel when:
- The event updates a background counter or a report
- The data is available on the next page load anyway
- No user is actively watching for it

---

## Push-Worthy Events vs. Background Events

| Event | Push via WebSocket? | Reason |
|---|---|---|
| `ChargeSuccessfulEvent` | **Yes** — to the cashier's terminal | Cashier is watching the POS screen for payment confirmation |
| `ChargeFailedEvent` | **Yes** — to the cashier's terminal | Cashier needs to know immediately |
| `KotReadyEvent` | **Yes** — to kitchen display + cashier | Time-critical kitchen operation |
| `ShipmentLocationUpdatedEvent` | **Yes** — to public tracking page | Recipient is watching live tracking |
| `ShipmentAssignedEvent` | **Yes** — to the rider's mobile app | Rider must acknowledge the assignment |
| `PayrollPendingApprovalEvent` | **Yes** — to approvers' sessions | Approver needs to be alerted immediately |
| `JournalEntryPendingApprovalEvent` | **Yes** — to accounting approvers | Approver is needed before close of business |
| `SupportTicketCreatedEvent` | **Yes** — to support agent queue view | Support agent is monitoring open tickets |
| `JournalEntryPostedEvent` | **No** — background accounting | No human waiting; page will reload on next visit |
| `BalanceSnapshotCreatedEvent` | **No** — infrastructure event | No human relevance |
| `StockAdjustedEvent` | **No** — inventory update | Inventory report updated on next query |
| `EmployeeOnboardedEvent` | **No** — async notification | Email is sufficient; no live viewer |
| `PayrollDisbursedEvent` | **No** — async notification | SMS/email is the right channel for this |
| `PurchaseOrderSentEvent` | **No** — background operation | No one watching for this |
| `AnalyticsProjectionUpdatedEvent` | **No** — polling is fine | Dashboard can poll every 30s |

---

## Channel Architecture

WebSocket channels in AtlasHub follow three structural patterns:

### 1. User Private Channel
```
/user/{userId}/queue/notifications
```
Delivered only to WebSocket sessions authenticated as `{userId}`. Used for personal, actionable alerts.

**Examples:**
- Approval required (payroll, journal entry)
- Payment confirmed or failed on the cashier's own sale
- Leave application approved

### 2. Entity Real-Time Feed
```
/topic/outlet/{outletId}/kitchen
/topic/shipment/{trackingNumber}
```
A live feed scoped to a specific business entity. Any authenticated session that subscribes receives updates.

**Examples:**
- Kitchen display subscribes to `/topic/outlet/{outletId}/kitchen` — receives all KOT events for that outlet
- A recipient page subscribes to `/topic/shipment/{trackingNumber}` — no auth required, public feed

### 3. Operational Context Broadcast
```
/topic/org/{orgId}/payroll
/topic/org/{orgId}/support
```
Broadcast to all sessions from the same organization watching a specific operational context.

---

## Spring STOMP Configuration

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
            .setAllowedOriginPatterns(
                "https://*.atlashub.io",         // production
                "http://localhost:*"              // local dev only
            )
            .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketAuthInterceptor());
    }
}
```

---

## Authentication on CONNECT

The STOMP `CONNECT` frame carries the JWT access token. The `WebSocketAuthInterceptor` validates it before allowing the session to subscribe to any channel.

```java
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtVerifier jwtVerifier;
    private final TokenRevocationChecker revocationChecker;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (!StompCommand.CONNECT.equals(accessor.getCommand())) return message;

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MessagingException("WebSocket CONNECT rejected: missing Authorization header");
        }

        JwtClaims claims = jwtVerifier.verify(authHeader.substring(7));

        if (revocationChecker.isRevoked(claims.jti())) {
            throw new MessagingException("WebSocket CONNECT rejected: token revoked");
        }

        // Spring uses this Principal for /user/{userId} routing
        accessor.setUser(new AtlasHubPrincipal(claims.userId(), claims.activeOrganizationId()));

        return message;
    }
}
```

Public channels like `/topic/shipment/{trackingNumber}` are accessible without authentication. A separate endpoint `/ws-public` allows anonymous connections — it has no `WebSocketAuthInterceptor`.

---

## Selective Event Broadcaster

A single `SelectiveWebSocketBroadcaster` Kafka listener handles all WebSocket push decisions. It is explicit about which events it cares about, and it maps each event to a *user-facing notification* — not a raw domain event payload.

```java
@Slf4j
@Component
public class SelectiveWebSocketBroadcaster extends BaseKafkaEventListener {

    private static final String GROUP_ID = "websocket-broadcaster";

    private final SimpMessagingTemplate ws;

    @KafkaListener(
        topics = {"pay-events", "commerce-events", "hr-events", "logistics-events",
                  "accounting-events", "support-events"},
        groupId = GROUP_ID
    )
    public void onEvent(String payload) {

        // — CASHIER: payment result —
        processEventIfMatches(payload, "ChargeSuccessfulEvent", ChargeSuccessfulEvent.class,
            log, GROUP_ID, event ->
                ws.convertAndSendToUser(
                    String.valueOf(event.payload().cashierId()),
                    "/queue/notifications",
                    PushNotification.of("PAYMENT_CONFIRMED",
                        "Payment of " + event.payload().formattedAmount() + " confirmed")
                )
        );

        processEventIfMatches(payload, "ChargeFailedEvent", ChargeFailedEvent.class,
            log, GROUP_ID, event ->
                ws.convertAndSendToUser(
                    String.valueOf(event.payload().cashierId()),
                    "/queue/notifications",
                    PushNotification.of("PAYMENT_FAILED", "Payment failed: " + event.payload().reason())
                )
        );

        // — KITCHEN DISPLAY: KOT ready —
        processEventIfMatches(payload, "KotReadyEvent", KotReadyEvent.class,
            log, GROUP_ID, event ->
                ws.convertAndSend(
                    "/topic/outlet/" + event.payload().outletId() + "/kitchen",
                    PushNotification.of("KOT_READY",
                        "Table " + event.payload().tableNumber() + " order is ready",
                        Map.of("kotId", event.payload().kotId()))
                )
        );

        // — SHIPMENT TRACKING: live location —
        processEventIfMatches(payload, "ShipmentLocationUpdatedEvent", ShipmentLocationUpdatedEvent.class,
            log, GROUP_ID, event ->
                ws.convertAndSend(
                    "/topic/shipment/" + event.payload().trackingNumber(),
                    PushNotification.of("LOCATION_UPDATE",
                        event.payload().statusDescription(),
                        Map.of("location", event.payload().location(),
                               "status", event.payload().status()))
                )
        );

        // — APPROVERS: payroll waiting —
        processEventIfMatches(payload, "PayrollPendingApprovalEvent", PayrollPendingApprovalEvent.class,
            log, GROUP_ID, event ->
                event.payload().approverUserIds().forEach(approverId ->
                    ws.convertAndSendToUser(
                        String.valueOf(approverId),
                        "/queue/notifications",
                        PushNotification.of("APPROVAL_REQUIRED",
                            "Payroll run for " + event.payload().period() + " needs your approval",
                            Map.of("entityType", "PAYROLL_RUN", "entityId", event.payload().payrollRunId()))
                    )
                )
        );

        // — ACCOUNTING APPROVERS —
        processEventIfMatches(payload, "JournalEntryPendingApprovalEvent",
            JournalEntryPendingApprovalEvent.class, log, GROUP_ID, event ->
                event.payload().approverUserIds().forEach(approverId ->
                    ws.convertAndSendToUser(
                        String.valueOf(approverId),
                        "/queue/notifications",
                        PushNotification.of("APPROVAL_REQUIRED",
                            "Manual journal entry " + event.payload().entryNumber() + " requires approval",
                            Map.of("entityType", "JOURNAL_ENTRY", "entityId", event.payload().entryId()))
                    )
                )
        );

        // — SUPPORT AGENT QUEUE —
        processEventIfMatches(payload, "TicketCreatedEvent", TicketCreatedEvent.class,
            log, GROUP_ID, event ->
                ws.convertAndSend(
                    "/topic/org/" + event.payload().organizationId() + "/support",
                    PushNotification.of("TICKET_CREATED",
                        "New " + event.payload().priority() + " ticket: " + event.payload().subject())
                )
        );
    }
}
```

### The `PushNotification` Transfer Object
The client receives a simplified, UI-ready payload — never a raw domain event:

```java
public record PushNotification(
    String type,
    String message,
    Map<String, Object> data,
    String timestamp
) {
    public static PushNotification of(String type, String message) {
        return new PushNotification(type, message, Map.of(), ZonedDateTime.now().toString());
    }

    public static PushNotification of(String type, String message, Map<String, Object> data) {
        return new PushNotification(type, message, data, ZonedDateTime.now().toString());
    }
}
```

---

## Production Scaling

The in-memory simple broker works on a single node. For horizontal scaling (multiple JVM instances behind a load balancer), WebSocket sessions are distributed across nodes. A user's session may be on Node A, but the Kafka consumer that processes their event may be on Node B. The in-memory broker cannot deliver cross-node.

**Solution**: Replace the simple broker with a STOMP relay pointing to a dedicated message broker (RabbitMQ with STOMP plugin). All nodes relay through it, and a user's session receives messages regardless of which node processed the Kafka event:

```java
@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableStompBrokerRelay("/topic", "/queue")
        .setRelayHost("rabbitmq")
        .setRelayPort(61613)
        .setClientLogin(env.getProperty("RABBITMQ_LOGIN"))
        .setClientPasscode(env.getProperty("RABBITMQ_PASSCODE"));
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
}
```

For MVP (single Docker container), the in-memory simple broker is correct. Introduce RabbitMQ relay when horizontal scaling is needed.
