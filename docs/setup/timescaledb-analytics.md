# TimescaleDB & Analytics Time-Series

> **Source**: *Designing Data-Intensive Applications* — Kleppmann, Ch. 3 (Storage Engines) and Ch. 10 (Batch Processing); TimescaleDB documentation — Continuous Aggregates and Hypertables.

---

## Why TimescaleDB for Analytics?

Analytics queries on operational PostgreSQL tables degrade as data grows. A query like *"total revenue per hour for the last 30 days grouped by outlet"* requires a full scan of `sales_orders` if no pre-computation exists. At 10 million rows, this query takes seconds; at 100 million rows, it times out.

TimescaleDB solves this as a PostgreSQL extension — no new database engine, no new query language, no operational complexity. It adds two capabilities on top of standard PostgreSQL:

1. **Hypertables**: Tables automatically partitioned by time. A hypertable with 90 days of data may have 90 internal chunks. A query filtered to "last 7 days" touches 7 chunks, not all 90. Query time is bounded by the time range, not the total row count.

2. **Continuous Aggregates**: Materialized rollup views that PostgreSQL incrementally refreshes as new data arrives. A query for "hourly revenue" reads a pre-computed view with one row per hour, not raw event rows.

---

## The `analytics_events` Hypertable

The single source of truth for all time-series analytics. Every domain event worth measuring writes one row here.

```sql
-- Enable TimescaleDB extension
CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE analytics_events (
    id              BIGSERIAL,
    org_id          BIGINT          NOT NULL,
    event_type      TEXT            NOT NULL,
    module          TEXT            NOT NULL,
    amount          NUMERIC(19, 4),
    currency        CHAR(3),
    dimension_1     TEXT,           -- outlet_id, rider_id, product_id, cashier_id, etc.
    dimension_1_key TEXT,           -- what dimension_1 represents: 'outlet_id', 'cashier_id'
    dimension_2     TEXT,
    dimension_2_key TEXT,
    metadata        JSONB,          -- arbitrary extra context — use sparingly
    occurred_at     TIMESTAMPTZ     NOT NULL
);

-- Convert to hypertable, partitioned by occurred_at, 1-day chunks
SELECT create_hypertable('analytics_events', 'occurred_at',
    chunk_time_interval => INTERVAL '1 day');

-- Required indexes for common filter patterns
CREATE INDEX ON analytics_events (org_id, occurred_at DESC);
CREATE INDEX ON analytics_events (org_id, event_type, occurred_at DESC);
CREATE INDEX ON analytics_events (org_id, dimension_1_key, dimension_1, occurred_at DESC);
```

### Why a Generic Schema Instead of Per-Event Tables?

Per-event tables (`pos_sale_analytics`, `charge_analytics`) seem cleaner but create operational problems:
- Adding a new trackable event requires a DDL migration
- Continuous aggregate definitions must be duplicated or joined across tables
- Dashboard queries require `UNION` across many tables

The generic `(event_type, dimension_1, dimension_1_key, dimension_2)` pattern allows any event to be recorded and queried uniformly. The `dimension_n` columns carry the most-queried attributes (outletId, cashierId, productId) as indexed columns. `metadata` JSONB catches everything else, but is not indexed — not suitable for WHERE clauses.

---

## Continuous Aggregates

Continuous aggregates are defined once and maintained automatically by TimescaleDB. They refresh incrementally — only processing new rows since the last refresh.

### Hourly Revenue by Organization
```sql
CREATE MATERIALIZED VIEW hourly_revenue
WITH (timescaledb.continuous) AS
SELECT
    org_id,
    time_bucket('1 hour', occurred_at)  AS bucket,
    currency,
    SUM(amount)                          AS total_revenue,
    COUNT(*)                             AS transaction_count
FROM analytics_events
WHERE event_type IN ('POS_SALE_COMPLETED', 'CHARGE_SUCCESSFUL')
  AND amount IS NOT NULL
GROUP BY org_id, bucket, currency
WITH NO DATA;

SELECT add_continuous_aggregate_policy('hourly_revenue',
    start_offset => INTERVAL '3 hours',
    end_offset   => INTERVAL '1 minute',
    schedule_interval => INTERVAL '5 minutes');
```

### Daily Sales by Outlet
```sql
CREATE MATERIALIZED VIEW daily_outlet_sales
WITH (timescaledb.continuous) AS
SELECT
    org_id,
    dimension_1                          AS outlet_id,
    time_bucket('1 day', occurred_at)   AS bucket,
    currency,
    SUM(amount)                          AS revenue,
    COUNT(*)                             AS transactions
FROM analytics_events
WHERE event_type = 'POS_SALE_COMPLETED'
  AND dimension_1_key = 'outlet_id'
GROUP BY org_id, outlet_id, bucket, currency
WITH NO DATA;

SELECT add_continuous_aggregate_policy('daily_outlet_sales',
    start_offset => INTERVAL '2 days',
    end_offset   => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour');
```

### Monthly Payroll Cost
```sql
CREATE MATERIALIZED VIEW monthly_payroll_cost
WITH (timescaledb.continuous) AS
SELECT
    org_id,
    time_bucket('1 month', occurred_at) AS bucket,
    currency,
    SUM(amount)                          AS total_net_payroll,
    COUNT(DISTINCT dimension_1)          AS headcount   -- dimension_1 = employee_id
FROM analytics_events
WHERE event_type = 'PAYROLL_DISBURSED'
  AND dimension_1_key = 'employee_id'
GROUP BY org_id, bucket, currency
WITH NO DATA;
```

### Delivery Performance (Non-Financial)
```sql
CREATE MATERIALIZED VIEW daily_delivery_performance
WITH (timescaledb.continuous) AS
SELECT
    org_id,
    time_bucket('1 day', occurred_at)   AS bucket,
    COUNT(*) FILTER (WHERE event_type = 'SHIPMENT_DELIVERED') AS delivered,
    COUNT(*) FILTER (WHERE event_type = 'SHIPMENT_FAILED')    AS failed,
    COUNT(*)                                                   AS total
FROM analytics_events
WHERE event_type IN ('SHIPMENT_DELIVERED', 'SHIPMENT_FAILED')
GROUP BY org_id, bucket
WITH NO DATA;
```

---

## Data Retention & Compression

```sql
-- Compress chunks older than 7 days (20x storage reduction)
SELECT add_compression_policy('analytics_events', INTERVAL '7 days');

-- Drop chunks older than 2 years
SELECT add_retention_policy('analytics_events', INTERVAL '2 years');
```

Compression uses TimescaleDB's columnar storage for old chunks — reads are still fast via the continuous aggregates, while raw storage is minimized.

---

## Writing Analytics Events (Application Layer)

```java
// In atlashub-analytics:adapter/out/persistence
@Component
@RequiredArgsConstructor
public class AnalyticsEventWriter {

    private final AnalyticsEventJdbcRepository jdbcRepository;

    public void record(AnalyticsEvent event) {
        jdbcRepository.insert(event);
    }
}

// AnalyticsEvent — domain object in analytics module
public record AnalyticsEvent(
    Long orgId,
    String eventType,
    String module,
    BigDecimal amount,
    String currency,
    String dimension1,
    String dimension1Key,
    String dimension2,
    String dimension2Key,
    Map<String, Object> metadata,
    ZonedDateTime occurredAt
) {}
```

**JDBC not JPA for analytics writes.** Hibernate is inappropriate here — it adds overhead (entity lifecycle, first-level cache, dirty checking) that has no value for append-only time-series writes. Spring's `NamedParameterJdbcTemplate` is the right tool: fast, zero overhead.

---

## Querying from the Application Layer

Dashboard queries read from continuous aggregate views, not from `analytics_events` directly:

```java
// GetHourlyRevenueQuery → hits the hourly_revenue continuous aggregate
@Repository
public class RevenueTimeSeriesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<HourlyRevenueRow> getHourlyRevenue(Long orgId, LocalDate date) {
        String sql = """
            SELECT bucket, total_revenue, transaction_count, currency
            FROM hourly_revenue
            WHERE org_id = :orgId
              AND bucket >= :from AND bucket < :to
            ORDER BY bucket ASC
            """;
        return jdbc.query(sql,
            Map.of("orgId", orgId,
                   "from",  date.atStartOfDay(ZoneId.of("Africa/Lagos")),
                   "to",    date.plusDays(1).atStartOfDay(ZoneId.of("Africa/Lagos"))),
            new HourlyRevenueRowMapper());
    }
}
```

---

## Docker Compose (TimescaleDB)

```yaml
timescaledb:
  image: timescale/timescaledb:2.14.2-pg16
  ports:
    - "5433:5432"                        # different host port to avoid collision with main PG
  environment:
    POSTGRES_DB: atlashub_analytics
    POSTGRES_USER: ${TIMESCALE_USER}
    POSTGRES_PASSWORD: ${TIMESCALE_PASSWORD}
  volumes:
    - timescale-data:/var/lib/postgresql/data
    - ./infra/timescaledb/init.sql:/docker-entrypoint-initdb.d/init.sql
```

The analytics database is a **separate PostgreSQL instance** from the operational database. They share the same host in Docker Compose for local development, but use different ports. In production, they run on separate database servers — analytics workloads must never compete with OLTP for I/O.
