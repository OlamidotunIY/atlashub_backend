# Analytics Module Design (`atlashub-analytics`)

## Role & Purpose

The `analytics` module provides **advanced, real-time business intelligence** across all AtlasHub product modules. It gives organizations a live view of their business performance — sales by cashier, revenue by product category, delivery success rates, payroll cost trends — without ever touching the OLTP database.

Analytics is built on two complementary foundations:

1. **CQRS Event-Driven Projections**: Every domain event updates dedicated read models (materialized views). Dashboard queries hit these pre-computed models — not the operational database. Query latency is O(1), regardless of transaction volume.

2. **TimescaleDB Time-Series Database**: A PostgreSQL extension that treats time as a first-class dimension. Revenue per hour, orders per day, delivery times, payroll cost per month — all stored as time-series data with automatic compression and continuous aggregation.

This design is how Stripe, Square, and Shopify build their analytics products. It scales to billions of events without degrading query performance.

---

## 1. Design Principles

### No Direct Joins to Operational Tables
Analytics queries never join against `sales_orders`, `payroll_runs`, or `ledger_transactions`. Those tables are owned by their respective modules. Analytics maintains its own projections — updated asynchronously by consuming events.

### Eventual Consistency is Acceptable
Analytics projections are eventually consistent — there may be a 1–5 second lag between a transaction completing and the dashboard updating. This is the correct trade-off: strong consistency for money movement, eventual consistency for reporting.

### Append-Only Time-Series
All time-series data is append-only. Historical data is never mutated — corrections are modeled as compensating events. This mirrors how accounting works: you don't erase an incorrect entry, you post a reversing entry.

---

## 2. Submodules

### `projections` — CQRS Read Models
Event-driven aggregates that maintain pre-computed totals and summaries.

### `timeseries` — TimescaleDB
Raw time-series metric rows — one row per event, indexed by time. Continuous aggregates compute hourly, daily, and monthly rollups automatically.

### `dashboards` — Query Layer
Use cases that compose data from projections + timeseries into dashboard payloads.

---

## 3. Domain Entities

### Commerce Analytics Projections

**`DailySalesProjection` (Read Model)**
```
DailySalesProjection
├── organizationId: Long
├── outletId: Long
├── date: LocalDate
├── totalTransactions: Integer
├── totalGross: Money
├── totalDiscounts: Money
├── totalTax: Money
├── totalNet: Money
├── cashSales: Money
├── cardSales: Money
├── creditSales: Money
└── updatedAt: ZonedDateTime
```
Updated by: `PosSaleCompletedEvent`, `PosSaleRefundedEvent`

---

**`ProductSalesProjection` (Read Model)**
```
ProductSalesProjection
├── organizationId: Long
├── productId: Long
├── month: YearMonth
├── quantitySold: Integer
├── totalRevenue: Money
└── updatedAt: ZonedDateTime
```
Updated by: `PosSaleCompletedEvent` (per line item)

---

**`CashierPerformanceProjection` (Read Model)**
```
CashierPerformanceProjection
├── organizationId: Long
├── cashierId: Long
├── date: LocalDate
├── totalTransactions: Integer
├── totalRevenue: Money
└── averageTransactionValue: Money
```

---

**`InventoryValueProjection` (Read Model)**
```
InventoryValueProjection
├── organizationId: Long
├── outletId: Long
├── totalSkus: Integer
├── totalStock: Integer
├── totalStockValue: Money           ← at cost price
├── lowStockCount: Integer
└── updatedAt: ZonedDateTime
```
Updated by: `StockAdjustedEvent`, `PosSaleCompletedEvent`, `PurchaseOrderReceivedEvent`

---

### Pay Analytics Projections

**`TransactionVolumeProjection` (Read Model)**
```
TransactionVolumeProjection
├── organizationId: Long
├── month: YearMonth
├── totalCharges: Long
├── totalVolume: Money
├── successfulCharges: Long
├── failedCharges: Long
├── totalPayouts: Long
├── totalPayoutAmount: Money
├── platformFeeCollected: Money
└── updatedAt: ZonedDateTime
```
Updated by: `ChargeSuccessfulEvent`, `ChargeFailedEvent`, `PayoutCompletedEvent`

---

**`WalletBalanceSummaryProjection` (Read Model)**
```
WalletBalanceSummaryProjection
├── organizationId: Long
├── operatingBalance: Money
├── escrowBalance: Money
├── payrollReserveBalance: Money
├── taxHoldingBalance: Money
└── updatedAt: ZonedDateTime
```
Updated by: `LedgerTransactionPostedEvent`

---

### HR Analytics Projections

**`PayrollCostProjection` (Read Model)**
```
PayrollCostProjection
├── organizationId: Long
├── month: YearMonth
├── totalGrossPayroll: Money
├── totalDeductions: Money
├── totalNetPayroll: Money
├── headcount: Integer
└── updatedAt: ZonedDateTime
```
Updated by: `PayrollDisbursedEvent`

---

**`HeadcountProjection` (Read Model)**
```
HeadcountProjection
├── organizationId: Long
├── total: Integer
├── active: Integer
├── onLeave: Integer
├── suspended: Integer
├── terminated: Integer
├── newHiresThisMonth: Integer
└── updatedAt: ZonedDateTime
```
Updated by: `EmployeeOnboardedEvent`, `EmployeeTerminatedEvent`, `EmployeeSuspendedEvent`

---

### Logistics Analytics Projections

**`DeliveryPerformanceProjection` (Read Model)**
```
DeliveryPerformanceProjection
├── organizationId: Long
├── month: YearMonth
├── totalShipments: Integer
├── delivered: Integer
├── failed: Integer
├── returned: Integer
├── deliverySuccessRate: BigDecimal  ← percentage
├── avgDeliveryMinutes: Integer
└── updatedAt: ZonedDateTime
```
Updated by: `ShipmentDeliveredEvent`, `ShipmentFailedEvent`

---

## 4. TimescaleDB Time-Series Tables

Every significant event writes a raw metric row into TimescaleDB. These rows are the immutable history. Continuous aggregates compute rollups automatically.

### `analytics_events` (Hypertable — partitioned by `occurred_at`)
```sql
CREATE TABLE analytics_events (
    id          BIGSERIAL,
    org_id      BIGINT        NOT NULL,
    event_type  TEXT          NOT NULL,   -- 'CHARGE_SUCCESSFUL', 'POS_SALE_COMPLETED', etc.
    module      TEXT          NOT NULL,   -- 'pay', 'commerce', 'hr', 'logistics'
    amount      NUMERIC(19,4),            -- monetary value if applicable
    currency    CHAR(3),
    metadata    JSONB,                    -- arbitrary context (outletId, productId, cashierId, etc.)
    occurred_at TIMESTAMPTZ   NOT NULL
);
SELECT create_hypertable('analytics_events', 'occurred_at');
```

### Continuous Aggregates

```sql
-- Hourly revenue by org
CREATE MATERIALIZED VIEW hourly_revenue
WITH (timescaledb.continuous) AS
SELECT
    org_id,
    time_bucket('1 hour', occurred_at) AS bucket,
    SUM(amount) AS total_revenue,
    COUNT(*) AS event_count,
    currency
FROM analytics_events
WHERE event_type IN ('CHARGE_SUCCESSFUL', 'POS_SALE_COMPLETED')
GROUP BY org_id, bucket, currency;

-- Daily sales by outlet
CREATE MATERIALIZED VIEW daily_sales_by_outlet
WITH (timescaledb.continuous) AS
SELECT
    org_id,
    (metadata->>'outletId')::BIGINT AS outlet_id,
    time_bucket('1 day', occurred_at) AS bucket,
    SUM(amount) AS revenue,
    COUNT(*) AS transactions
FROM analytics_events
WHERE event_type = 'POS_SALE_COMPLETED'
GROUP BY org_id, outlet_id, bucket;
```

See [setup/timescaledb-analytics.md](../setup/timescaledb-analytics.md) for the full hypertable schema and continuous aggregate definitions.

---

## 5. Domain Events Consumed

| Source Event | Analytics Action |
|---|---|
| `PosSaleCompletedEvent` | Update `DailySalesProjection`, `ProductSalesProjection`, `CashierPerformanceProjection`; insert row into `analytics_events` |
| `PosSaleRefundedEvent` | Subtract from `DailySalesProjection` |
| `ChargeSuccessfulEvent` | Update `TransactionVolumeProjection`; insert into `analytics_events` |
| `ChargeFailedEvent` | Increment failed count in `TransactionVolumeProjection` |
| `PayoutCompletedEvent` | Update `TransactionVolumeProjection` |
| `LedgerTransactionPostedEvent` | Update `WalletBalanceSummaryProjection` |
| `PayrollDisbursedEvent` | Update `PayrollCostProjection`; insert into `analytics_events` |
| `EmployeeOnboardedEvent` | Update `HeadcountProjection` |
| `EmployeeTerminatedEvent` | Update `HeadcountProjection` |
| `ShipmentDeliveredEvent` | Update `DeliveryPerformanceProjection`; insert into `analytics_events` |
| `ShipmentFailedEvent` | Update `DeliveryPerformanceProjection` |
| `StockAdjustedEvent` | Update `InventoryValueProjection` |
| `PurchaseOrderReceivedEvent` | Update `InventoryValueProjection` |
| `JournalEntryPostedEvent` | Insert into `analytics_events` for financial time-series |

---

## 6. Queries (Dashboard API)

### Commerce Dashboards
- `GetSalesSummaryQuery(orgId, outletId, dateFrom, dateTo, groupBy)` → `SalesSummaryResult`
  - `groupBy`: DAY, WEEK, MONTH, CASHIER, PRODUCT_CATEGORY, PAYMENT_METHOD
- `GetTopSellingProductsQuery(orgId, month, limit)` → `List<TopProductResult>`
- `GetCashierLeaderboardQuery(orgId, date)` → `List<CashierPerformanceResult>`
- `GetInventoryValueQuery(orgId, outletId)` → `InventoryValueResult`
- `GetHourlyRevenueQuery(orgId, date)` → `List<HourlyRevenueResult>` ← uses TimescaleDB aggregate

### Pay Dashboards
- `GetTransactionVolumeQuery(orgId, month)` → `TransactionVolumeResult`
- `GetWalletOverviewQuery(orgId)` → `WalletBalanceSummaryResult`
- `GetRevenueTimeSeriesQuery(orgId, dateFrom, dateTo, granularity)` → `List<RevenueDataPoint>`
  - `granularity`: HOURLY, DAILY, MONTHLY

### HR Dashboards
- `GetPayrollCostTrendQuery(orgId, year)` → `List<MonthlyPayrollCostResult>`
- `GetHeadcountSummaryQuery(orgId)` → `HeadcountResult`
- `GetLeaveUtilizationQuery(orgId, year)` → `LeaveUtilizationResult`

### Logistics Dashboards
- `GetDeliveryPerformanceQuery(orgId, month)` → `DeliveryPerformanceResult`
- `GetRiderLeaderboardQuery(orgId, month)` → `List<RiderPerformanceResult>`

### Platform-Wide (AtlasHub Admin)
- `GetPlatformTransactionVolumeQuery(dateFrom, dateTo)` → `PlatformVolumeResult`
- `GetPlatformFeeRevenueQuery(month)` → `PlatformFeeResult`
- `GetActiveOrganizationsQuery()` → `Int`

---

## 7. Distributed Architecture

### Projection Update Pattern
Each projection is updated idempotently using `INSERT ... ON CONFLICT DO UPDATE` (upsert). A duplicate event delivery will overwrite with the same values — no accumulation error.

### Write Path (Event → Projection)
```
Domain Event → Kafka → Analytics Kafka Listener → @Transactional {
    timeseries: INSERT INTO analytics_events (...)
    projection: UPDATE daily_sales_projection SET total_net = total_net + :amount WHERE ...
}
```

Both the TimescaleDB insert and the projection update happen in the same transaction. Either both succeed or both roll back.

### Inbox Pattern
All analytics listeners use `EventDeliveryTracker` to ensure idempotency. A Kafka retry must not increment a counter twice.
