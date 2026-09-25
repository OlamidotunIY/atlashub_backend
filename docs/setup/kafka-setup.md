# Kafka Setup & Event Bus Design

> **Source**: *Designing Data-Intensive Applications* — Martin Kleppmann, Ch. 11; *Kafka: The Definitive Guide* — Shapira, Palino, Sivaram, Petty; *Enterprise Integration Patterns* — Hohpe & Woolf.

---

## Architectural Role

Kafka in AtlasHub is a **durable commit log**, not a message queue. The distinction matters:

- A message queue delivers a message and forgets it. If the consumer was down, the message is gone.
- A commit log persists every message for a configured retention period. Consumers can replay, catch up, or start from any offset. A new analytics module added six months from now can reprocess the last 90 days of `pay-events` from the beginning.

This property — **log replayability** — is what makes event sourcing, CQRS projections, and analytics projections safe to build. The Outbox ensures events reach Kafka; Kafka ensures downstream consumers eventually receive them.

---

## Topic Design Philosophy

Per-module topics rather than per-event-type topics. The rationale:

| Design | Pros | Cons |
|---|---|---|
| Per-event-type (`charge-successful`, `payout-completed`) | Easy consumer filtering | Hundreds of topics at scale; partition pool wasted; Kafka admin overhead |
| Per-module (`pay-events`, `commerce-events`) | Simple topic roster; consumers filter by `eventType` field; all events for a module in one ordered log | Consumers receive events they don't care about — negligible overhead |

**Per-module is the correct choice at AtlasHub's scale.** Kafka is efficient at filtering within a consumer group; it is inefficient at managing thousands of topics.

---

## Topic Inventory

| Topic | Owned By | Partition Count | Retention |
|---|---|---|---|
| `pay-events` | `atlashub-pay` | 12 | 90 days |
| `commerce-events` | `atlashub-commerce` | 12 | 90 days |
| `hr-events` | `atlashub-hr` | 6 | 90 days |
| `logistics-events` | `atlashub-logistics` | 6 | 90 days |
| `accounting-events` | `atlashub-accounting` | 6 | 90 days |
| `identity-events` | `atlashub-accounts` | 3 | 30 days |
| `compliance-events` | `atlashub-compliance` | 3 | 30 days |
| `billing-events` | `atlashub-billing` | 3 | 30 days |
| `iam-events` | `atlashub-iam` | 3 | 30 days |
| `support-events` | `atlashub-support` | 3 | 30 days |

**Partition count reasoning**: Pay and Commerce are the highest-volume modules. 12 partitions support up to 12 parallel consumer threads. HR, Logistics, Accounting are medium-volume. Platform modules (identity, compliance, billing, iam) are low-volume.

---

## Event Envelope

Every event published to Kafka is wrapped in a standard envelope. The consumer reads `eventType` to dispatch — no separate topic per event type is needed.

```json
{
  "eventId":      "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "eventType":    "ChargeSuccessfulEvent",
  "aggregateType":"Charge",
  "aggregateId":  "12345",
  "module":       "pay",
  "occurredAt":   "2026-09-17T14:23:00+01:00",
  "schemaVersion": 1,
  "payload": {
    "chargeId": 12345,
    "organizationId": 67890,
    "reference": "CHG-abc123",
    "amount": 25000.0000,
    "currency": "NGN",
    "sourceSystem": "COMMERCE_CHECKOUT",
    "sourceReferenceId": "1042"
  }
}
```

`schemaVersion` allows additive schema evolution: consumers check the version and handle new fields gracefully.

---

## Consumer Groups

Each module registers one consumer group per topic it subscribes to. The format is `{module}-{source}-group`:

```
commerce-pay-group         (Commerce consuming pay-events)
hr-pay-group               (HR consuming pay-events)
accounting-pay-group       (Accounting consuming pay-events)
analytics-pay-group        (Analytics consuming pay-events)
notifications-pay-group    (Notifications consuming pay-events)
websocket-broadcaster      (WebSocket broadcaster — all topics)
```

Each group tracks its own offset independently. Analytics falling behind does not affect commerce. This is the consumer group isolation guarantee of Kafka.

---

## Producer Configuration (Strongest Durability)

```yaml
spring:
  kafka:
    producer:
      acks: all                           # wait for leader + all in-sync replicas
      retries: 5
      properties:
        enable.idempotence: true          # exactly-once semantics on the producer side
        max.in.flight.requests.per.connection: 5  # max with idempotence enabled
        delivery.timeout.ms: 120000       # 2 minutes total timeout
```

`acks=all` + `enable.idempotence=true` prevents message loss even if the broker leader fails mid-write.

---

## Consumer Configuration

```yaml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest         # new consumer groups start from the beginning
      enable-auto-commit: false           # manual commit — commit only after successful processing
      max-poll-records: 50               # batch size per poll
      properties:
        isolation.level: read_committed   # only read messages from committed transactions
    listener:
      ack-mode: MANUAL_IMMEDIATE          # commit offset immediately after handler returns
      concurrency: 3                      # 3 threads per listener (up to partition count)
```

`isolation.level: read_committed` means a consumer will not read messages from transactions that were later rolled back — important for the Outbox relay which uses Kafka producer transactions.

---

## Retry and Dead Letter Queue

Every listener is decorated with `@RetryableTopic`. Spring Kafka automatically creates retry and DLQ shadow topics:

```java
@RetryableTopic(
    attempts = "4",                     // 1 original + 3 retries
    backoff = @Backoff(
        delay = 1_000,                  // 1 second
        multiplier = 3.0,               // 1s → 3s → 9s
        maxDelay = 60_000               // cap at 60 seconds
    ),
    dltStrategy = DltStrategy.FAIL_ON_ERROR,
    autoCreateTopics = "false",         // topics are created explicitly in infra setup
    topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
)
@KafkaListener(topics = "pay-events", groupId = "commerce-pay-group")
public void onPayEvents(String payload) { ... }
```

This creates: `pay-events-retry-0`, `pay-events-retry-1`, `pay-events-retry-2`, `pay-events-dlt`.

### DLQ Monitor
A dedicated consumer listens to all `*-dlt` topics. On each dead-letter message:
1. Records to `failed_events` table with full context
2. Alerts the AtlasHub operations team

```java
@KafkaListener(topicPattern = ".*-dlt$", groupId = "dlq-monitor")
public void onDeadLetter(String payload,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.EXCEPTION_FQCN) String exceptionType) {
    failedEventRepository.record(topic, payload, exceptionType);
    operationsAlerter.alert("DLQ message in " + topic + ": " + exceptionType);
}
```

---

## Schema Evolution Rules

Kafka topics are long-lived. Events published today may be consumed by a new module a year from now. Follow these rules:

1. **Add new fields with defaults only** — never remove or rename fields in a payload
2. **Increment `schemaVersion`** when the payload structure changes
3. **Consumers must handle unknown fields gracefully** — use `FAIL_ON_UNKNOWN_PROPERTIES=false` in Jackson
4. **Never change the semantic meaning of an existing field** — add a new field instead

---

## Local Development (Docker Compose)

```yaml
zookeeper:
  image: confluentinc/cp-zookeeper:7.6.0
  environment:
    ZOOKEEPER_CLIENT_PORT: 2181
    ZOOKEEPER_TICK_TIME: 2000

kafka:
  image: confluentinc/cp-kafka:7.6.0
  depends_on: [zookeeper]
  ports:
    - "9092:9092"
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"

kafka-setup:
  image: confluentinc/cp-kafka:7.6.0
  depends_on: [kafka]
  entrypoint: ["/bin/bash", "-c"]
  command: |
    "
    cub kafka-ready -b kafka:9092 1 30
    kafka-topics --bootstrap-server kafka:9092 --create --topic pay-events --partitions 6 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic commerce-events --partitions 6 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic hr-events --partitions 3 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic logistics-events --partitions 3 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic accounting-events --partitions 3 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic identity-events --partitions 3 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic compliance-events --partitions 3 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic billing-events --partitions 3 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic iam-events --partitions 3 --replication-factor 1
    kafka-topics --bootstrap-server kafka:9092 --create --topic support-events --partitions 3 --replication-factor 1
    "
```
