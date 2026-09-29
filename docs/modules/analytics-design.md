# Analytics Module Design (`atlashub-analytics`)

## Role & Purpose

The `analytics` module provides **advanced, real-time business intelligence** across all AtlasHub product modules. It gives organizations a live view of their business performance — sales by cashier, revenue by product category, delivery success rates, payroll cost trends — without ever touching the OLTP database.

This is a **pure read + listener module**. It has **no user-initiated commands**. All data mutations are triggered by Kafka event listeners that react to domain events published by other modules. The presentation layer exposes GET-only endpoints.

Analytics is built on two complementary foundations:

1. **CQRS Event-Driven Projections**: Every domain event updates dedicated read models (materialized projection tables). Dashboard queries hit these pre-computed models — not the operational database. Query latency is O(1), regardless of transaction volume.

2. **TimescaleDB Time-Series Database**: A PostgreSQL extension that treats time as a first-class dimension. Revenue per hour, orders per day, delivery times, payroll cost per month — all stored as time-series data with automatic compression and continuous aggregation.

---

## 1. Design Principles

### No Direct Joins to Operational Tables
Analytics queries **never** join against `sales_orders`, `payroll_runs`, or `ledger_transactions`. Those tables are owned by their respective modules. Analytics maintains its own projections — updated asynchronously by consuming events.

### Eventual Consistency is Acceptable
Analytics projections are eventually consistent — there may be a 1–5 second lag between a transaction completing and the dashboard updating. This is the correct trade-off: strong consistency for money movement, eventual consistency for reporting.

### Idempotent Projection Updates
All projection updates use `INSERT ... ON CONFLICT DO UPDATE` (upsert). A duplicate Kafka delivery rewrites with the same computed values — no double-counting.

### Append-Only Time-Series
All time-series data is append-only. Historical data is never mutated — corrections are modeled as compensating events.

---

## 2. Submodules (Java Packages)

| Package | Purpose |
|---|---|
| `com.atlashub.analytics.projections` | CQRS read models — pre-computed totals and summaries updated by listeners |
| `com.atlashub.analytics.timeseries` | TimescaleDB raw metric rows and continuous aggregates |
| `com.atlashub.analytics.dashboards` | Query handlers that compose data from projections + timeseries into dashboard payloads |

---

## 3. Domain Layer

Analytics has **no traditional domain aggregate roots** — it is a pure read side. Instead, it owns **projection entities** (read models) and **time-series event records**.

### Commerce Analytics Projections

#### `DailySalesProjection` (Read Model)
```
DailySalesProjection
├── organizationId: Long
├── outletId: Long
├── date: LocalDate
├── totalTransactions: Integer
├── totalGross: BigDecimal
├── totalDiscounts: BigDecimal
├── totalTax: BigDecimal
├── totalNet: BigDecimal
├── cashSales: BigDecimal
├── cardSales: BigDecimal
├── creditSales: BigDecimal
└── updatedAt: ZonedDateTime
```
Updated by: `PosSaleCompletedEvent`, `PosSaleRefundedEvent`
PK: `(organizationId, outletId, date)`

---

#### `ProductSalesProjection` (Read Model)
```
ProductSalesProjection
├── organizationId: Long
├── productId: Long
├── month: YearMonth
├── quantitySold: Integer
├── totalRevenue: BigDecimal
└── updatedAt: ZonedDateTime
```
Updated by: `PosSaleCompletedEvent` (per line item)
PK: `(organizationId, productId, month)`

---

#### `CashierPerformanceProjection` (Read Model)
```
CashierPerformanceProjection
├── organizationId: Long
├── cashierId: Long
├── date: LocalDate
├── totalTransactions: Integer
├── totalRevenue: BigDecimal
└── averageTransactionValue: BigDecimal
```
Updated by: `PosSaleCompletedEvent`
PK: `(organizationId, cashierId, date)`

---

#### `InventoryValueProjection` (Read Model)
```
InventoryValueProjection
├── organizationId: Long
├── outletId: Long
├── totalSkus: Integer
├── totalStock: Integer
├── totalStockValue: BigDecimal     ← at cost price
├── lowStockCount: Integer
└── updatedAt: ZonedDateTime
```
Updated by: `StockAdjustedEvent`, `PosSaleCompletedEvent`, `PurchaseOrderReceivedEvent`
PK: `(organizationId, outletId)`

---

### Pay Analytics Projections

#### `TransactionVolumeProjection` (Read Model)
```
TransactionVolumeProjection
├── organizationId: Long
├── month: YearMonth
├── totalCharges: Long
├── totalVolume: BigDecimal
├── successfulCharges: Long
├── failedCharges: Long
├── totalPayouts: Long
├── totalPayoutAmount: BigDecimal
├── platformFeeCollected: BigDecimal
└── updatedAt: ZonedDateTime
```
Updated by: `ChargeSuccessfulEvent`, `ChargeFailedEvent`, `PayoutCompletedEvent`
PK: `(organizationId, month)`

---

#### `WalletBalanceSummaryProjection` (Read Model)
```
WalletBalanceSummaryProjection
├── organizationId: Long
├── operatingBalance: BigDecimal
├── escrowBalance: BigDecimal
├── payrollReserveBalance: BigDecimal
├── taxHoldingBalance: BigDecimal
└── updatedAt: ZonedDateTime
```
Updated by: `LedgerTransactionPostedEvent`
PK: `(organizationId)` — one row per org, always overwritten

---

### HR Analytics Projections

#### `PayrollCostProjection` (Read Model)
```
PayrollCostProjection
├── organizationId: Long
├── month: YearMonth
├── totalGrossPayroll: BigDecimal
├── totalDeductions: BigDecimal
├── totalNetPayroll: BigDecimal
├── headcount: Integer
└── updatedAt: ZonedDateTime
```
Updated by: `PayrollDisbursedEvent`
PK: `(organizationId, month)`

---

#### `HeadcountProjection` (Read Model)
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
PK: `(organizationId)`

---

### Logistics Analytics Projections

#### `DeliveryPerformanceProjection` (Read Model)
```
DeliveryPerformanceProjection
├── organizationId: Long
├── month: YearMonth
├── totalShipments: Integer
├── delivered: Integer
├── failed: Integer
├── returned: Integer
├── deliverySuccessRate: BigDecimal   ← percentage (0.00 – 100.00)
├── avgDeliveryMinutes: Integer
└── updatedAt: ZonedDateTime
```
Updated by: `ShipmentDeliveredEvent`, `ShipmentFailedEvent`
PK: `(organizationId, month)`

---

### TimescaleDB — `BalanceSnapshot` (Read Model)
```
BalanceSnapshot
├── id: Long
├── organizationId: Long
├── accountId: Long
├── ledgerType: String                ← OPERATING, ESCROW, PAYROLL_RESERVE, TAX_HOLDING
├── balance: BigDecimal
└── snapshotAt: ZonedDateTime         ← hypertable partition column
```
Created by: `BalanceSnapshotScheduler` (daily)
PK: `(accountId, snapshotAt)` — append-only, never updated

---

### Domain Events (No Domain Exceptions)

Analytics has no domain-level invariants to enforce — all data is written idempotently. There are no domain exceptions in this module.

---

## 4. Application Layer

> [!IMPORTANT]
> **All commands in this module are listener-triggered and return `void`.** None of these commands are exposed via REST endpoints. The presentation layer is GET-only.

### Commands

#### `UpdateDailySalesProjection`
```
package com.atlashub.analytics.projections.application.commands.UpdateDailySalesProjection

UpdateDailySalesProjectionCommand(
    Long organizationId,
    Long outletId,
    LocalDate date,
    BigDecimal grossAmount,
    BigDecimal discountAmount,
    BigDecimal taxAmount,
    BigDecimal netAmount,
    String paymentMethod,   // CASH, CARD, CREDIT
    boolean isRefund        // true → subtract from projection
)
```
**Handler**: `UpdateDailySalesProjectionHandler extends Command<UpdateDailySalesProjectionCommand, Void>`
- Upserts `DailySalesProjection` via `INSERT ... ON CONFLICT DO UPDATE`.
- If `isRefund = true`, subtracts amounts from corresponding totals.

---

#### `UpdateProductSalesProjection`
```
UpdateProductSalesProjectionCommand(
    Long organizationId,
    Long productId,
    YearMonth month,
    Integer quantitySold,
    BigDecimal revenue,
    boolean isRefund
)
```
**Handler**: `UpdateProductSalesProjectionHandler extends Command<UpdateProductSalesProjectionCommand, Void>`

---

#### `UpdateCashierPerformanceProjection`
```
UpdateCashierPerformanceProjectionCommand(
    Long organizationId,
    Long cashierId,
    LocalDate date,
    BigDecimal saleAmount
)
```
**Handler**: `UpdateCashierPerformanceProjectionHandler extends Command<UpdateCashierPerformanceProjectionCommand, Void>`

---

#### `UpdateInventoryValueProjection`
```
UpdateInventoryValueProjectionCommand(
    Long organizationId,
    Long outletId,
    Integer skuDelta,
    Integer stockDelta,
    BigDecimal stockValueDelta,
    Integer lowStockDelta
)
```
**Handler**: `UpdateInventoryValueProjectionHandler extends Command<UpdateInventoryValueProjectionCommand, Void>`

---

#### `UpdateTransactionVolumeProjection`
```
UpdateTransactionVolumeProjectionCommand(
    Long organizationId,
    YearMonth month,
    boolean isCharge,
    boolean isSuccessful,
    boolean isPayout,
    BigDecimal amount,
    BigDecimal platformFee
)
```
**Handler**: `UpdateTransactionVolumeProjectionHandler extends Command<UpdateTransactionVolumeProjectionCommand, Void>`

---

#### `UpdateWalletBalanceSummaryProjection`
```
UpdateWalletBalanceSummaryProjectionCommand(
    Long organizationId,
    String ledgerType,
    BigDecimal newBalance
)
```
**Handler**: `UpdateWalletBalanceSummaryProjectionHandler extends Command<UpdateWalletBalanceSummaryProjectionCommand, Void>`
- Full replace (overwrite) of the relevant balance field — always reflects current state.

---

#### `UpdatePayrollCostProjection`
```
UpdatePayrollCostProjectionCommand(
    Long organizationId,
    YearMonth month,
    BigDecimal grossPayroll,
    BigDecimal deductions,
    BigDecimal netPayroll,
    Integer headcount
)
```
**Handler**: `UpdatePayrollCostProjectionHandler extends Command<UpdatePayrollCostProjectionCommand, Void>`

---

#### `UpdateHeadcountProjection`
```
UpdateHeadcountProjectionCommand(
    Long organizationId,
    String changeType   // HIRED, TERMINATED, SUSPENDED, REINSTATED
)
```
**Handler**: `UpdateHeadcountProjectionHandler extends Command<UpdateHeadcountProjectionCommand, Void>`

---

#### `UpdateDeliveryPerformanceProjection`
```
UpdateDeliveryPerformanceProjectionCommand(
    Long organizationId,
    YearMonth month,
    boolean isDelivered,
    Integer deliveryMinutes  // null if failed
)
```
**Handler**: `UpdateDeliveryPerformanceProjectionHandler extends Command<UpdateDeliveryPerformanceProjectionCommand, Void>`

---

#### `RecordAnalyticsEvent`
```
package com.atlashub.analytics.timeseries.application.commands.RecordAnalyticsEvent

RecordAnalyticsEventCommand(
    Long orgId,
    String eventType,    // 'CHARGE_SUCCESSFUL', 'POS_SALE_COMPLETED', etc.
    String module,       // 'pay', 'commerce', 'hr', 'logistics'
    BigDecimal amount,   // nullable
    String currency,     // nullable
    Map<String, Object> metadata,
    ZonedDateTime occurredAt
)
```
**Handler**: `RecordAnalyticsEventHandler extends Command<RecordAnalyticsEventCommand, Void>`
- Inserts a row into the TimescaleDB `analytics_events` hypertable.
- Uses the **TimescaleDB datasource** (not the primary OLTP datasource).
- This write path is always within the same Kafka listener transaction as the projection upsert.

---

### Queries

> [!NOTE]
> All analytics queries return `List<T>` (not `PageResult<T>`) because the data is **pre-aggregated**. Results are bounded by the query parameters (org + date range or month) and are small enough to return in full. Pagination would add complexity with no benefit on pre-computed projections.

#### Commerce Dashboard Queries

| Query | Handler | Return Type | Source |
|---|---|---|---|
| `GetSalesSummaryQuery(orgId, outletId, dateFrom, dateTo)` | `GetSalesSummaryHandler` | `List<DailySalesSummaryResult>` | `DailySalesProjection` |
| `GetTopSellingProductsQuery(orgId, month, limit)` | `GetTopSellingProductsHandler` | `List<TopProductResult>` | `ProductSalesProjection` |
| `GetCashierLeaderboardQuery(orgId, date)` | `GetCashierLeaderboardHandler` | `List<CashierPerformanceResult>` | `CashierPerformanceProjection` |
| `GetInventoryValueQuery(orgId, outletId)` | `GetInventoryValueHandler` | `InventoryValueResult` | `InventoryValueProjection` |
| `GetHourlyRevenueQuery(orgId, date)` | `GetHourlyRevenueHandler` | `List<HourlyRevenueResult>` | TimescaleDB `hourly_revenue` |

#### Pay Dashboard Queries

| Query | Handler | Return Type | Source |
|---|---|---|---|
| `GetTransactionVolumeQuery(orgId, month)` | `GetTransactionVolumeHandler` | `TransactionVolumeResult` | `TransactionVolumeProjection` |
| `GetWalletOverviewQuery(orgId)` | `GetWalletOverviewHandler` | `WalletBalanceSummaryResult` | `WalletBalanceSummaryProjection` |
| `GetRevenueTimeSeriesQuery(orgId, dateFrom, dateTo, granularity)` | `GetRevenueTimeSeriesHandler` | `List<RevenueDataPoint>` | TimescaleDB `hourly_revenue` / `daily_sales_by_outlet` |

#### HR Dashboard Queries

| Query | Handler | Return Type | Source |
|---|---|---|---|
| `GetPayrollCostTrendQuery(orgId, year)` | `GetPayrollCostTrendHandler` | `List<MonthlyPayrollCostResult>` | `PayrollCostProjection` |
| `GetHeadcountSummaryQuery(orgId)` | `GetHeadcountSummaryHandler` | `HeadcountResult` | `HeadcountProjection` |

#### Logistics Dashboard Queries

| Query | Handler | Return Type | Source |
|---|---|---|---|
| `GetDeliveryPerformanceQuery(orgId, month)` | `GetDeliveryPerformanceHandler` | `DeliveryPerformanceResult` | `DeliveryPerformanceProjection` |
| `GetRiderLeaderboardQuery(orgId, month)` | `GetRiderLeaderboardHandler` | `List<RiderPerformanceResult>` | `analytics_events` JSONB aggregation |

#### Platform Admin Queries (Cross-Org)

| Query | Handler | Return Type | Notes |
|---|---|---|---|
| `GetPlatformTransactionVolumeQuery(dateFrom, dateTo)` | `GetPlatformTransactionVolumeHandler` | `PlatformVolumeResult` | Aggregates across all orgs |
| `GetPlatformFeeRevenueQuery(month)` | `GetPlatformFeeRevenueHandler` | `PlatformFeeResult` | Aggregates `platformFeeCollected` across all orgs |

---

## 5. Infrastructure Layer

### TimescaleDB Configuration

Analytics uses a **dedicated secondary datasource** for TimescaleDB. This is separate from the primary OLTP PostgreSQL datasource.

```yaml
# application.yml
analytics:
  timescaledb:
    url: jdbc:postgresql://timescaledb-host:5432/atlashub_analytics
    username: ${TIMESCALE_USER}
    password: ${TIMESCALE_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

A dedicated `@Configuration` class (`TimescaleDbConfig`) creates a separate `DataSource`, `EntityManagerFactory`, and `TransactionManager` beans for the TimescaleDB schema. All repositories in `com.atlashub.analytics.timeseries.infrastructure.persistence` use this secondary transaction manager.

---

### TimescaleDB Schema

#### `analytics_events` Hypertable

```sql
CREATE TABLE analytics_events (
    id          BIGSERIAL,
    org_id      BIGINT        NOT NULL,
    event_type  TEXT          NOT NULL,
    module      TEXT          NOT NULL,
    amount      NUMERIC(19,4),
    currency    CHAR(3),
    metadata    JSONB,
    occurred_at TIMESTAMPTZ   NOT NULL
);
SELECT create_hypertable('analytics_events', 'occurred_at');
```

#### Continuous Aggregates

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

### Kafka Listeners

#### `CommerceAnalyticsListener`

```java
@Component
public class CommerceAnalyticsListener extends BaseKafkaEventListener {
    private static final String GROUP_ID = "analytics-commerce-group";

    @PostConstruct
    public void init() {
        registerSubscription("PosSaleCompletedEvent", GROUP_ID);
        registerSubscription("PosSaleRefundedEvent", GROUP_ID);
        registerSubscription("StockAdjustedEvent", GROUP_ID);
        registerSubscription("PurchaseOrderReceivedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "PosSaleCompletedEvent",
            PosSaleCompletedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
                // Update DailySalesProjection, ProductSalesProjection, CashierPerformanceProjection
                // Insert row into analytics_events (TimescaleDB)
            });

        processEventIfMatches(messagePayload, "PosSaleRefundedEvent",
            PosSaleRefundedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
                // Subtract from DailySalesProjection (isRefund = true)
            });

        processEventIfMatches(messagePayload, "StockAdjustedEvent",
            StockAdjustedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
                // Update InventoryValueProjection
            });

        processEventIfMatches(messagePayload, "PurchaseOrderReceivedEvent",
            PurchaseOrderReceivedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
                // Update InventoryValueProjection (stock received)
            });
    }
}
```

**Topic**: `commerce-events` | **Group**: `analytics-commerce-group`

| Inbound Event | Commands Invoked |
|---|---|
| `PosSaleCompletedEvent` | `UpdateDailySalesProjection`, `UpdateProductSalesProjection`, `UpdateCashierPerformanceProjection`, `RecordAnalyticsEvent` |
| `PosSaleRefundedEvent` | `UpdateDailySalesProjection` (isRefund=true) |
| `StockAdjustedEvent` | `UpdateInventoryValueProjection` |
| `PurchaseOrderReceivedEvent` | `UpdateInventoryValueProjection` |

---

#### `PayAnalyticsListener`

**Topic**: `pay-events` | **Group**: `analytics-pay-group`

| Inbound Event | Commands Invoked |
|---|---|
| `ChargeSuccessfulEvent` | `UpdateTransactionVolumeProjection` (isSuccessful=true), `RecordAnalyticsEvent` |
| `ChargeFailedEvent` | `UpdateTransactionVolumeProjection` (isSuccessful=false) |
| `PayoutCompletedEvent` | `UpdateTransactionVolumeProjection` (isPayout=true) |
| `LedgerTransactionPostedEvent` | `UpdateWalletBalanceSummaryProjection` |

---

#### `HrAnalyticsListener`

**Topic**: `hr-events` | **Group**: `analytics-hr-group`

| Inbound Event | Commands Invoked |
|---|---|
| `PayrollDisbursedEvent` | `UpdatePayrollCostProjection`, `RecordAnalyticsEvent` |
| `EmployeeOnboardedEvent` | `UpdateHeadcountProjection` (changeType=HIRED) |
| `EmployeeTerminatedEvent` | `UpdateHeadcountProjection` (changeType=TERMINATED) |
| `EmployeeSuspendedEvent` | `UpdateHeadcountProjection` (changeType=SUSPENDED) |

---

#### `LogisticsAnalyticsListener`

**Topic**: `logistics-events` | **Group**: `analytics-logistics-group`

| Inbound Event | Commands Invoked |
|---|---|
| `ShipmentDeliveredEvent` | `UpdateDeliveryPerformanceProjection` (isDelivered=true), `RecordAnalyticsEvent` |
| `ShipmentFailedEvent` | `UpdateDeliveryPerformanceProjection` (isDelivered=false) |

---

### Scheduler

#### `BalanceSnapshotScheduler`

```java
@Component
public class BalanceSnapshotScheduler {

    @Scheduled(cron = "0 0 2 * * *")   // daily at 02:00
    public void snapshotAllLedgerBalances() {
        // Fetches all active ledger accounts from WalletBalanceSummaryProjection
        // For each, inserts a BalanceSnapshot row into TimescaleDB
        // This is a read-from-projection → write-to-timescaledb operation
        // No domain commands are involved — direct TimescaleDB insert
    }
}
```

**Cron**: `0 0 2 * * *` (daily at 2:00 AM)
**Action**: Reads current balance from `WalletBalanceSummaryProjection`, inserts `BalanceSnapshot` rows into TimescaleDB for trend analysis.

---

### Persistence

#### OLTP Projection Tables (Primary PostgreSQL)

| Table | JPA Entity | Notes |
|---|---|---|
| `daily_sales_projections` | `DailySalesProjectionJpa` | PK: `(org_id, outlet_id, date)` |
| `product_sales_projections` | `ProductSalesProjectionJpa` | PK: `(org_id, product_id, month)` |
| `cashier_performance_projections` | `CashierPerformanceProjectionJpa` | PK: `(org_id, cashier_id, date)` |
| `inventory_value_projections` | `InventoryValueProjectionJpa` | PK: `(org_id, outlet_id)` |
| `transaction_volume_projections` | `TransactionVolumeProjectionJpa` | PK: `(org_id, month)` |
| `wallet_balance_summary_projections` | `WalletBalanceSummaryProjectionJpa` | PK: `org_id` |
| `payroll_cost_projections` | `PayrollCostProjectionJpa` | PK: `(org_id, month)` |
| `headcount_projections` | `HeadcountProjectionJpa` | PK: `org_id` |
| `delivery_performance_projections` | `DeliveryPerformanceProjectionJpa` | PK: `(org_id, month)` |

All projection tables use **native SQL upserts** (`@Modifying @Query("INSERT ... ON CONFLICT DO UPDATE")`). Spring Data `save()` is not used for projection updates — it cannot express upsert semantics.

#### TimescaleDB Tables (Secondary DataSource)

| Table | JPA Entity | Notes |
|---|---|---|
| `analytics_events` | `AnalyticsEventJpa` | Hypertable; insert-only |
| `balance_snapshots` | `BalanceSnapshotJpa` | Hypertable; insert-only |

---

### Infrastructure Services

- **`TimescaleDbConfig`** — `@Configuration` defining the secondary `DataSource`, `EntityManagerFactory`, and `PlatformTransactionManager` for TimescaleDB. Scans `com.atlashub.analytics.timeseries.infrastructure.persistence`.

---

## 6. Presentation Layer

### Controller

**`AnalyticsDashboardController`** — `@RequestMapping("/api/v1/analytics")`

All endpoints are GET-only and require Bearer JWT authentication.

---

#### Commerce Endpoints

| Method | Path | RBAC | Handler | Response |
|---|---|---|---|---|
| GET | `/analytics/sales/summary` | `analytics:reports:read` | `GetSalesSummaryHandler` | `List<DailySalesSummaryDto>` |
| GET | `/analytics/sales/top-products` | `analytics:reports:read` | `GetTopSellingProductsHandler` | `List<TopProductDto>` |
| GET | `/analytics/sales/cashier-leaderboard` | `analytics:reports:read` | `GetCashierLeaderboardHandler` | `List<CashierPerformanceDto>` |
| GET | `/analytics/inventory/value` | `analytics:reports:read` | `GetInventoryValueHandler` | `InventoryValueDto` |
| GET | `/analytics/revenue/hourly` | `analytics:reports:read` | `GetHourlyRevenueHandler` | `List<HourlyRevenueDto>` |

**Query params** (commerce): `outletId` (optional), `dateFrom`, `dateTo`, `month`, `limit`.

---

#### Pay Endpoints

| Method | Path | RBAC | Handler | Response |
|---|---|---|---|---|
| GET | `/analytics/pay/transaction-volume` | `analytics:reports:read` | `GetTransactionVolumeHandler` | `TransactionVolumeDto` |
| GET | `/analytics/pay/wallet-overview` | `analytics:reports:read` | `GetWalletOverviewHandler` | `WalletBalanceSummaryDto` |
| GET | `/analytics/revenue/timeseries` | `analytics:reports:read` | `GetRevenueTimeSeriesHandler` | `List<RevenueDataPointDto>` |

**Query params** (pay): `month`, `dateFrom`, `dateTo`, `granularity` (HOURLY / DAILY / MONTHLY).

---

#### HR Endpoints

| Method | Path | RBAC | Handler | Response |
|---|---|---|---|---|
| GET | `/analytics/hr/payroll-trend` | `analytics:reports:read` | `GetPayrollCostTrendHandler` | `List<MonthlyPayrollCostDto>` |
| GET | `/analytics/hr/headcount` | `analytics:reports:read` | `GetHeadcountSummaryHandler` | `HeadcountDto` |

**Query params** (hr): `year`.

---

#### Logistics Endpoints

| Method | Path | RBAC | Handler | Response |
|---|---|---|---|---|
| GET | `/analytics/logistics/delivery-performance` | `analytics:reports:read` | `GetDeliveryPerformanceHandler` | `DeliveryPerformanceDto` |
| GET | `/analytics/logistics/rider-leaderboard` | `analytics:reports:read` | `GetRiderLeaderboardHandler` | `List<RiderPerformanceDto>` |

**Query params** (logistics): `month`.

---

#### Platform Admin Endpoints

| Method | Path | RBAC | Handler | Response |
|---|---|---|---|---|
| GET | `/analytics/platform/transaction-volume` | `analytics:platform:read` | `GetPlatformTransactionVolumeHandler` | `PlatformVolumeDto` |
| GET | `/analytics/platform/fee-revenue` | `analytics:platform:read` | `GetPlatformFeeRevenueHandler` | `PlatformFeeDto` |

> [!CAUTION]
> `analytics:platform:read` endpoints return **cross-organization data**. This permission must only be granted to AtlasHub internal admin staff — never to org users.

---

### Response DTOs (Selected)

**`DailySalesSummaryDto`**: `date`, `totalTransactions`, `totalGross`, `totalDiscounts`, `totalTax`, `totalNet`, `cashSales`, `cardSales`, `creditSales`.

**`TopProductDto`**: `productId`, `productName`, `quantitySold`, `totalRevenue`, `month`.

**`CashierPerformanceDto`**: `cashierId`, `cashierName`, `date`, `totalTransactions`, `totalRevenue`, `averageTransactionValue`.

**`TransactionVolumeDto`**: `month`, `totalCharges`, `totalVolume`, `successfulCharges`, `failedCharges`, `totalPayouts`, `totalPayoutAmount`, `platformFeeCollected`.

**`HourlyRevenueDto`**: `bucket` (ZonedDateTime), `totalRevenue`, `eventCount`, `currency`.

**`RevenueDataPointDto`**: `timestamp` (ZonedDateTime), `revenue`, `granularity`.

**`HeadcountDto`**: `total`, `active`, `onLeave`, `suspended`, `terminated`, `newHiresThisMonth`.

**`DeliveryPerformanceDto`**: `month`, `totalShipments`, `delivered`, `failed`, `returned`, `deliverySuccessRate`, `avgDeliveryMinutes`.

---

## 7. RBAC Table

| Permission | Who Holds It | Endpoints |
|---|---|---|
| `analytics:reports:read` | Any active org user | All `/analytics/sales/*`, `/analytics/pay/*`, `/analytics/hr/*`, `/analytics/logistics/*` — scoped to own org |
| `analytics:platform:read` | AtlasHub admin staff only | `/analytics/platform/*` — cross-org aggregates |

> [!NOTE]
> Org-scoped endpoints extract `organizationId` from `@AuthenticationPrincipal`. Even with `analytics:reports:read`, a user cannot query data for a different organization — the query always uses the authenticated user's `activeOrganizationId`.

---

## 8. Maker-Checker

The analytics module does **not** participate in maker-checker workflows. All writes are system-initiated (listener-driven), not user-initiated financial decisions.

---

## 9. WebSocket Events

The analytics module does **not** push any WebSocket events. Dashboard clients poll the REST API on a timer (every 30 seconds is sufficient, given ~1–5s event lag). Pushing `AnalyticsProjectionUpdatedEvent` via WebSocket would create noise with no user value.

> [!TIP]
> For real-time dashboard experience, implement **client-side polling** with a 30-second interval on the dashboard page. This is simpler than WebSocket subscriptions and sufficient given the eventual-consistency model.

---

## 10. Domain Events Table

The analytics module **consumes** events from other modules and **produces no domain events of its own**. It is a terminal consumer in all event chains.

| Source Module | Event Consumed | Action |
|---|---|---|
| `commerce` | `PosSaleCompletedEvent` | Update `DailySalesProjection`, `ProductSalesProjection`, `CashierPerformanceProjection`; insert `analytics_events` |
| `commerce` | `PosSaleRefundedEvent` | Subtract from `DailySalesProjection` |
| `commerce` | `StockAdjustedEvent` | Update `InventoryValueProjection` |
| `commerce` | `PurchaseOrderReceivedEvent` | Update `InventoryValueProjection` |
| `pay` | `ChargeSuccessfulEvent` | Update `TransactionVolumeProjection`; insert `analytics_events` |
| `pay` | `ChargeFailedEvent` | Update `TransactionVolumeProjection` (failed count) |
| `pay` | `PayoutCompletedEvent` | Update `TransactionVolumeProjection` |
| `pay` | `LedgerTransactionPostedEvent` | Update `WalletBalanceSummaryProjection` |
| `hr` | `PayrollDisbursedEvent` | Update `PayrollCostProjection`; insert `analytics_events` |
| `hr` | `EmployeeOnboardedEvent` | Update `HeadcountProjection` |
| `hr` | `EmployeeTerminatedEvent` | Update `HeadcountProjection` |
| `hr` | `EmployeeSuspendedEvent` | Update `HeadcountProjection` |
| `logistics` | `ShipmentDeliveredEvent` | Update `DeliveryPerformanceProjection`; insert `analytics_events` |
| `logistics` | `ShipmentFailedEvent` | Update `DeliveryPerformanceProjection` |

---

## 11. Distributed Architecture

### Write Path (Event → Projection)

```
Domain Event → Kafka → Analytics Kafka Listener → @Transactional {
    timeseries:  INSERT INTO analytics_events (...)          [TimescaleDB datasource]
    projection:  INSERT INTO daily_sales_projections (...)   [Primary datasource]
                 ON CONFLICT (org_id, outlet_id, date)
                 DO UPDATE SET total_net = total_net + EXCLUDED.total_net, ...
}
```

The TimescaleDB insert and the projection upsert run in **separate transactions** (different datasources). Both are idempotent — a Kafka retry will rewrite with the same values, producing no accumulation error.

### Inbox Pattern (Idempotency)
All analytics listeners use `EventDeliveryTracker` to record processed event IDs. Before processing, the listener checks whether the event ID has already been handled. If so, it skips processing. This prevents double-counting on Kafka retries.

### No Distributed Transactions
Because the TimescaleDB and OLTP writes are in separate transactions, a failure between them could leave one written and the other not. This is acceptable: the Kafka listener will retry, and both writes are idempotent, so the eventual state is correct.

### Read Path
Dashboard queries hit the pre-computed projection tables directly — no joins, no aggregation at query time. For TimescaleDB continuous aggregates, TimescaleDB refreshes them automatically based on the configured refresh policy.

---

## 12. Complete File List

```
atlashub-analytics/
└── src/main/java/com/atlashub/analytics/
    ├── projections/
    │   ├── application/
    │   │   └── commands/
    │   │       ├── UpdateDailySalesProjection/
    │   │       │   ├── UpdateDailySalesProjectionCommand.java
    │   │       │   └── UpdateDailySalesProjectionHandler.java
    │   │       ├── UpdateProductSalesProjection/
    │   │       │   ├── UpdateProductSalesProjectionCommand.java
    │   │       │   └── UpdateProductSalesProjectionHandler.java
    │   │       ├── UpdateCashierPerformanceProjection/
    │   │       │   ├── UpdateCashierPerformanceProjectionCommand.java
    │   │       │   └── UpdateCashierPerformanceProjectionHandler.java
    │   │       ├── UpdateInventoryValueProjection/
    │   │       │   ├── UpdateInventoryValueProjectionCommand.java
    │   │       │   └── UpdateInventoryValueProjectionHandler.java
    │   │       ├── UpdateTransactionVolumeProjection/
    │   │       │   ├── UpdateTransactionVolumeProjectionCommand.java
    │   │       │   └── UpdateTransactionVolumeProjectionHandler.java
    │   │       ├── UpdateWalletBalanceSummaryProjection/
    │   │       │   ├── UpdateWalletBalanceSummaryProjectionCommand.java
    │   │       │   └── UpdateWalletBalanceSummaryProjectionHandler.java
    │   │       ├── UpdatePayrollCostProjection/
    │   │       │   ├── UpdatePayrollCostProjectionCommand.java
    │   │       │   └── UpdatePayrollCostProjectionHandler.java
    │   │       ├── UpdateHeadcountProjection/
    │   │       │   ├── UpdateHeadcountProjectionCommand.java
    │   │       │   └── UpdateHeadcountProjectionHandler.java
    │   │       └── UpdateDeliveryPerformanceProjection/
    │   │           ├── UpdateDeliveryPerformanceProjectionCommand.java
    │   │           └── UpdateDeliveryPerformanceProjectionHandler.java
    │   ├── domain/
    │   │   └── entities/
    │   │       ├── DailySalesProjection.java
    │   │       ├── ProductSalesProjection.java
    │   │       ├── CashierPerformanceProjection.java
    │   │       ├── InventoryValueProjection.java
    │   │       ├── TransactionVolumeProjection.java
    │   │       ├── WalletBalanceSummaryProjection.java
    │   │       ├── PayrollCostProjection.java
    │   │       ├── HeadcountProjection.java
    │   │       └── DeliveryPerformanceProjection.java
    │   └── infrastructure/
    │       ├── messaging/
    │       │   └── listeners/
    │       │       ├── CommerceAnalyticsListener.java
    │       │       ├── PayAnalyticsListener.java
    │       │       ├── HrAnalyticsListener.java
    │       │       └── LogisticsAnalyticsListener.java
    │       └── persistence/
    │           ├── adapters/
    │           │   ├── DailySalesProjectionAdapter.java
    │           │   ├── ProductSalesProjectionAdapter.java
    │           │   ├── CashierPerformanceProjectionAdapter.java
    │           │   ├── InventoryValueProjectionAdapter.java
    │           │   ├── TransactionVolumeProjectionAdapter.java
    │           │   ├── WalletBalanceSummaryProjectionAdapter.java
    │           │   ├── PayrollCostProjectionAdapter.java
    │           │   ├── HeadcountProjectionAdapter.java
    │           │   └── DeliveryPerformanceProjectionAdapter.java
    │           ├── entities/
    │           │   ├── DailySalesProjectionJpa.java
    │           │   ├── ProductSalesProjectionJpa.java
    │           │   ├── CashierPerformanceProjectionJpa.java
    │           │   ├── InventoryValueProjectionJpa.java
    │           │   ├── TransactionVolumeProjectionJpa.java
    │           │   ├── WalletBalanceSummaryProjectionJpa.java
    │           │   ├── PayrollCostProjectionJpa.java
    │           │   ├── HeadcountProjectionJpa.java
    │           │   └── DeliveryPerformanceProjectionJpa.java
    │           └── repositories/
    │               ├── DailySalesProjectionRepository.java
    │               ├── ProductSalesProjectionRepository.java
    │               ├── CashierPerformanceProjectionRepository.java
    │               ├── InventoryValueProjectionRepository.java
    │               ├── TransactionVolumeProjectionRepository.java
    │               ├── WalletBalanceSummaryProjectionRepository.java
    │               ├── PayrollCostProjectionRepository.java
    │               ├── HeadcountProjectionRepository.java
    │               └── DeliveryPerformanceProjectionRepository.java
    ├── timeseries/
    │   ├── application/
    │   │   └── commands/
    │   │       └── RecordAnalyticsEvent/
    │   │           ├── RecordAnalyticsEventCommand.java
    │   │           └── RecordAnalyticsEventHandler.java
    │   ├── domain/
    │   │   └── entities/
    │   │       ├── AnalyticsEvent.java
    │   │       └── BalanceSnapshot.java
    │   └── infrastructure/
    │       ├── config/
    │       │   └── TimescaleDbConfig.java
    │       ├── persistence/
    │       │   ├── entities/
    │       │   │   ├── AnalyticsEventJpa.java
    │       │   │   └── BalanceSnapshotJpa.java
    │       │   └── repositories/
    │       │       ├── AnalyticsEventRepository.java
    │       │       └── BalanceSnapshotRepository.java
    │       └── scheduling/
    │           └── BalanceSnapshotScheduler.java
    └── dashboards/
        ├── application/
        │   └── queries/
        │       ├── GetSalesSummary/
        │       │   ├── GetSalesSummaryQuery.java
        │       │   └── GetSalesSummaryHandler.java
        │       ├── GetTopSellingProducts/
        │       │   ├── GetTopSellingProductsQuery.java
        │       │   └── GetTopSellingProductsHandler.java
        │       ├── GetCashierLeaderboard/
        │       │   ├── GetCashierLeaderboardQuery.java
        │       │   └── GetCashierLeaderboardHandler.java
        │       ├── GetInventoryValue/
        │       │   ├── GetInventoryValueQuery.java
        │       │   └── GetInventoryValueHandler.java
        │       ├── GetHourlyRevenue/
        │       │   ├── GetHourlyRevenueQuery.java
        │       │   └── GetHourlyRevenueHandler.java
        │       ├── GetTransactionVolume/
        │       │   ├── GetTransactionVolumeQuery.java
        │       │   └── GetTransactionVolumeHandler.java
        │       ├── GetWalletOverview/
        │       │   ├── GetWalletOverviewQuery.java
        │       │   └── GetWalletOverviewHandler.java
        │       ├── GetRevenueTimeSeries/
        │       │   ├── GetRevenueTimeSeriesQuery.java
        │       │   └── GetRevenueTimeSeriesHandler.java
        │       ├── GetPayrollCostTrend/
        │       │   ├── GetPayrollCostTrendQuery.java
        │       │   └── GetPayrollCostTrendHandler.java
        │       ├── GetHeadcountSummary/
        │       │   ├── GetHeadcountSummaryQuery.java
        │       │   └── GetHeadcountSummaryHandler.java
        │       ├── GetDeliveryPerformance/
        │       │   ├── GetDeliveryPerformanceQuery.java
        │       │   └── GetDeliveryPerformanceHandler.java
        │       ├── GetRiderLeaderboard/
        │       │   ├── GetRiderLeaderboardQuery.java
        │       │   └── GetRiderLeaderboardHandler.java
        │       ├── GetPlatformTransactionVolume/
        │       │   ├── GetPlatformTransactionVolumeQuery.java
        │       │   └── GetPlatformTransactionVolumeHandler.java
        │       └── GetPlatformFeeRevenue/
        │           ├── GetPlatformFeeRevenueQuery.java
        │           └── GetPlatformFeeRevenueHandler.java
        └── presentation/
            ├── dto/
            │   ├── DailySalesSummaryDto.java
            │   ├── TopProductDto.java
            │   ├── CashierPerformanceDto.java
            │   ├── InventoryValueDto.java
            │   ├── HourlyRevenueDto.java
            │   ├── TransactionVolumeDto.java
            │   ├── WalletBalanceSummaryDto.java
            │   ├── RevenueDataPointDto.java
            │   ├── MonthlyPayrollCostDto.java
            │   ├── HeadcountDto.java
            │   ├── DeliveryPerformanceDto.java
            │   ├── RiderPerformanceDto.java
            │   ├── PlatformVolumeDto.java
            │   └── PlatformFeeDto.java
            └── rest/
                └── AnalyticsDashboardController.java
```
