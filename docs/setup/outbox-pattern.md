# Transactional Outbox & Inbox Pattern

> **Source**: *Designing Data-Intensive Applications* — Martin Kleppmann, Ch. 11 (Stream Processing); *Implementing Domain-Driven Design* — Vaughn Vernon, Ch. 8 (Domain Events); *Enterprise Integration Patterns* — Hohpe & Woolf (Guaranteed Delivery, Idempotent Receiver).

---

## The Dual-Write Problem

A use case that saves an aggregate and then publishes an event has a fundamental atomicity problem. Two independent systems — the database and the message broker — cannot participate in the same ACID transaction. Any approach that treats them as separate steps creates windows of failure:

- Save to DB → publish to Kafka → crash before publish: **event is lost**
- Publish to Kafka → save to DB fails → rollback: **phantom event exists with no corresponding state**

Both scenarios corrupt the system's integrity.

The **Transactional Outbox** solves this by reducing the problem to a single atomic write: the database. Events are written to an `outbox_messages` table *within the same DB transaction as the aggregate*. A background relay process reads from the outbox and publishes to Kafka. If the relay crashes, it restarts and publishes again — Kafka receives the event at least once, and the Inbox pattern on the consumer side handles the duplicate.

---

## Where Does Outbox Writing Belong?

This is a critical architectural question. **The answer from DDD literature is unambiguous:**

> *"Repositories are the boundary between the domain model and the infrastructure layer. They are the natural place to handle the persistence side-effects of aggregate changes — including event publication."*
> — Vaughn Vernon, IDDD Ch. 12

Publishing events is an **infrastructure concern**. It belongs in the repository adapter, not in the application layer. The use case orchestrates domain logic; it should be completely unaware of how events are stored or relayed.

**The wrong way (common in naive implementations):**
```java
// ❌ Application layer is aware of infrastructure concerns
@Service
public class InitiateChargeUseCase extends BaseUseCase<...> {
    @Transactional
    public Void execute(InitiateChargeCommand command) {
        Charge charge = Charge.initiate(...);
        chargeRepository.save(charge);
        publishEvents(charge, publisher);   // ← wrong: use case knows about outbox
        return null;
    }
}
```

**The correct way (repository handles it transparently):**
```java
// ✅ Use case is pure business orchestration
@Service
public class InitiateChargeUseCase extends BaseUseCase<...> {
    @Transactional
    public Void execute(InitiateChargeCommand command) {
        Charge charge = Charge.initiate(...);
        chargeRepository.save(charge);   // repository handles event extraction + outbox write
        return null;
    }
}
```

The `@Transactional` annotation *does* belong on the use case — the transaction boundary is a business decision (what constitutes one unit of work). But *what happens inside* the repository during save is an infrastructure decision.

---

## Base Repository Adapter (Infrastructure Layer)

All repository adapters extend a single `BaseJpaRepositoryAdapter` that implements the four common operations **once** and handles outbox writing for every `save()` call transparently.

```java
// In atlashub-infrastructure
public abstract class BaseJpaRepositoryAdapter<
    DOMAIN extends AggregateRoot<ID>,
    ENTITY,
    ID,
    JPA extends JpaRepository<ENTITY, ID>
> {
    protected final JPA jpaRepository;
    protected final BaseMapper<DOMAIN, ENTITY> mapper;
    private final OutboxWriter outboxWriter;

    protected BaseJpaRepositoryAdapter(JPA jpaRepository,
                                        BaseMapper<DOMAIN, ENTITY> mapper,
                                        OutboxWriter outboxWriter) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.outboxWriter = outboxWriter;
    }

    /**
     * Saves the aggregate, maps it back to the domain, and writes any pending
     * domain events to the outbox — all within the caller's transaction.
     * The calling use case never needs to know this happened.
     */
    public DOMAIN save(DOMAIN aggregate) {
        ENTITY entity = mapper.toEntity(aggregate);
        ENTITY saved = jpaRepository.save(entity);

        // Pull and write events atomically — within the same transaction
        List<DomainEvent<?>> events = aggregate.pullDomainEvents();
        outboxWriter.writeAll(events, aggregate.getClass().getSimpleName());

        return mapper.toDomain(saved);
    }

    public Optional<DOMAIN> findById(ID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    public boolean existsById(ID id) {
        return jpaRepository.existsById(id);
    }

    public void deleteById(ID id) {
        jpaRepository.deleteById(id);
    }
}
```

**A concrete module repository adapter then only adds its specific queries:**

```java
// In atlashub-pay:adapter/out/persistence/repository
@Component
public class ChargeRepositoryAdapter
    extends BaseJpaRepositoryAdapter<Charge, ChargeJpaEntity, Long, SpringDataChargeRepository>
    implements ChargeRepository {

    private final DomainSequenceGenerator sequenceGenerator;

    public ChargeRepositoryAdapter(SpringDataChargeRepository jpa,
                                    ChargeMapper mapper,
                                    OutboxWriter outboxWriter,
                                    DomainSequenceGenerator sequenceGenerator) {
        super(jpa, mapper, outboxWriter);
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("charge_seq");
    }

    @Override
    public Optional<Charge> findByReference(String reference) {
        return jpaRepository.findByReference(reference).map(mapper::toDomain);
    }

    @Override
    public List<Charge> findByOrganizationIdAndStatus(Long orgId, ChargeStatus status) {
        return jpaRepository.findByOrganizationIdAndStatus(orgId, status)
            .stream().map(mapper::toDomain).toList();
    }
}
```

No module ever duplicates `save`, `findById`, `existsById`, or `deleteById`. They are defined once in the base class.

---

## OutboxWriter (Infrastructure Component)

```java
// In atlashub-infrastructure:eventbus
@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxMessageRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Writes all domain events from the aggregate to the outbox_messages table.
     * This method is called within the same @Transactional context as the aggregate save —
     * guaranteed by the BaseJpaRepositoryAdapter.
     */
    public void writeAll(List<DomainEvent<?>> events, String aggregateType) {
        for (DomainEvent<?> event : events) {
            EnvelopedDomainEvent<?> envelope = EnvelopedDomainEvent.wrap(event, aggregateType);
            String payload = serialize(envelope);
            String topic = TopicRegistry.topicFor(aggregateType);

            outboxRepository.save(new OutboxMessageJpaEntity(
                envelope.eventId(),
                envelope.eventType(),
                aggregateType,
                envelope.aggregateId(),
                topic,
                payload
            ));
        }
    }
}
```

---

## outbox_messages Table Schema

```sql
CREATE TABLE outbox_messages (
    id              BIGSERIAL       PRIMARY KEY,
    event_id        UUID            NOT NULL,
    event_type      VARCHAR(200)    NOT NULL,
    aggregate_type  VARCHAR(100)    NOT NULL,
    aggregate_id    VARCHAR(100)    NOT NULL,
    topic           VARCHAR(200)    NOT NULL,
    payload         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMPTZ,
    failure_reason  TEXT,
    retry_count     INTEGER         NOT NULL DEFAULT 0
);

-- Partial index: only unprocessed messages scanned by the poller
CREATE INDEX idx_outbox_pending ON outbox_messages (created_at ASC)
    WHERE status = 'PENDING';
```

---

## Outbox Relay (Infrastructure)

The relay is a lightweight scheduled process — it reads pending outbox messages in small batches and publishes them to Kafka. It runs continuously with a short poll interval.

```java
@Component
@Slf4j
public class OutboxRelay {

    private final OutboxMessageRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 200)   // runs every 200ms — low-latency relay
    @Transactional
    public void relay() {
        // Batch: max 50 messages per cycle to cap memory and latency
        List<OutboxMessageJpaEntity> batch = outboxRepository
            .findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxMessageJpaEntity message : batch) {
            try {
                kafkaTemplate
                    .send(message.getTopic(),
                          message.getAggregateId(),  // partition key: ensures order per aggregate
                          message.getPayload())
                    .get(5, TimeUnit.SECONDS);        // block for broker ACK before marking published

                message.markPublished(ZonedDateTime.now());

            } catch (Exception ex) {
                log.error("Outbox relay failed for message {}: {}", message.getId(), ex.getMessage());
                message.recordFailure(ex.getMessage());
            }
        }

        // Batch save all status updates in one round-trip
        outboxRepository.saveAll(batch);
    }
}
```

**Why the aggregate ID as Kafka message key?** Events for the same aggregate (e.g., all events for Charge #42) land in the same partition and are consumed in the order they were written. This preserves causality without requiring global ordering (which kills throughput).

---

## The Inbox (Idempotent Consumer)

Kafka provides at-least-once delivery. The Inbox pattern ensures business logic executes **exactly once** per event, regardless of how many times Kafka delivers it. This is the *Idempotent Receiver* pattern from EIP (Hohpe & Woolf, p. 549).

### event_inbox Table

```sql
CREATE TABLE event_inbox (
    event_id        VARCHAR(100)    NOT NULL,
    consumer_group  VARCHAR(200)    NOT NULL,
    processed_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (event_id, consumer_group)
);
```

The `(event_id, consumer_group)` primary key is the deduplication key. An insert that would violate it means the event has already been processed — the consumer skips it silently.

### BaseKafkaEventListener — Inbox Check Is Automatic

`processEventIfMatches()` in `BaseKafkaEventListener` (shared infrastructure) checks and records the inbox entry atomically within the same database transaction that runs the business logic:

```java
// In atlashub-shared:BaseKafkaEventListener
protected <T> void processEventIfMatches(String rawPayload,
                                          String expectedType,
                                          Class<T> payloadClass,
                                          Logger log,
                                          String consumerGroup,
                                          Consumer<EnvelopedDomainEvent<T>> handler) {
    EnvelopedDomainEvent<?> envelope = deserialize(rawPayload);
    if (!expectedType.equals(envelope.eventType())) return;

    // Inbox check: INSERT OR SKIP
    if (inboxRepository.existsByEventIdAndConsumerGroup(envelope.eventId(), consumerGroup)) {
        log.debug("Skipping already-processed event {} for group {}", envelope.eventId(), consumerGroup);
        return;
    }
    inboxRepository.save(new EventInboxEntry(envelope.eventId(), consumerGroup));

    // Business logic runs only if we won the inbox insert race
    T typed = objectMapper.convertValue(envelope.payload(), payloadClass);
    handler.accept(new EnvelopedDomainEvent<>(envelope, typed));
}
```

---

## Guarantees

| Failure Scenario | Outbox Guarantee | Inbox Guarantee |
|---|---|---|
| App crashes after DB commit, before relay publishes | Relay picks up on restart — event not lost | N/A |
| Relay publishes twice (retry after timeout) | Kafka receives duplicate | Inbox key conflict — second delivery silently skipped |
| Consumer crashes after business logic, before offset commit | Kafka redelivers | Inbox already written — duplicate skip |
| DB transaction rolls back | Outbox row rolled back with it — no phantom event | N/A |
| Aggregate saved but no events registered | `pullDomainEvents()` returns empty list — no outbox write | N/A |
