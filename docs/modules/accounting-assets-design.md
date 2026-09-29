# Accounting Assets Design (`atlashub-accounting` / `com.atlashub.accounting.assets`)

## Role & Purpose

The `assets` subpackage manages fixed asset registration and monthly depreciation. Organizations register physical assets (computers, vehicles, machinery), and a monthly scheduler automatically posts depreciation journal entries to the GL.

Depreciation is calculated using the **straight-line method** (MVP). Reducing-balance is a future enhancement.

Gradle module: `atlashub-accounting`  
Package: `com.atlashub.accounting.assets`

---

## Domain Layer

### `Asset` (Aggregate Root)

**Package:** `com.atlashub.accounting.assets.domain.entities`

```
Asset
├── id                      : Long
├── organizationId          : Long
├── name                    : String
├── tagNumber               : String        ← unique asset tag
├── category                : String        ← e.g., "Computer Equipment", "Motor Vehicle"
├── purchaseDate            : LocalDate
├── purchaseValue           : Money
├── salvageValue            : Money         ← residual value at end of useful life
├── usefulLifeMonths        : Integer
├── depreciationMethod      : DepreciationMethod ← STRAIGHT_LINE (MVP)
├── accumulatedDepreciation : Money
├── currentBookValue        : Money
└── status                  : AssetStatus   ← ACTIVE | FULLY_DEPRECIATED | DISPOSED
```

**Business methods (on entity):**

| Method | Guard | Events | Exceptions |
|---|---|---|---|
| `depreciate(amount)` | status == ACTIVE | `AssetDepreciatedEvent` (if monthly run completed) | — |
| `dispose(saleValue, disposalDate)` | status == ACTIVE or FULLY_DEPRECIATED | `AssetDisposedEvent` | `AssetAlreadyDisposedException` |

**Domain Rule:** Monthly depreciation amount = `(purchaseValue - salvageValue) / usefulLifeMonths`. When `accumulatedDepreciation >= (purchaseValue - salvageValue)`, status transitions to `FULLY_DEPRECIATED`.

**Why depreciation on entity:** The calculation is purely based on the asset's own fields. No external repository access is required.

**Why `DepreciationService` is a domain service:** The monthly depreciation run must iterate over ALL active assets for an org and post ONE journal entry per org (or one per asset, depending on policy). This coordination across multiple aggregates cannot live on a single entity.

---

### Domain Service: `DepreciationService`

**Package:** `com.atlashub.accounting.assets.domain.services`  
**Declared as:** `@Bean` in `ApplicationConfig`

**Responsibility:** Calculates the monthly depreciation amount for a single `Asset` and returns it. The `RunDepreciationHandler` uses this service per asset, then calls `RecordJournalEntryHandler` (in `accounting.gl`) to post the resulting journal entry.

```java
public class DepreciationService {
    public Money calculateMonthlyDepreciation(Asset asset);  // (purchaseValue - salvageValue) / usefulLifeMonths
    public boolean isFullyDepreciated(Asset asset);
}
```

---

### Domain Events — `com.atlashub.accounting.assets.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `AssetDepreciatedEvent` | Monthly depreciation posted | `notifications` (monthly depreciation report summary) |
| `AssetDisposedEvent` | Asset disposed/sold | `accounting:gl` (post disposal gain/loss journal entry) |

---

### Domain Exceptions — `com.atlashub.accounting.assets.domain.exceptions`

```java
public class AssetNotFoundException extends NotFoundException {
    public AssetNotFoundException(Long id) { super("Asset not found: " + id); }
    public AssetNotFoundException(String tagNumber) { super("Asset not found with tag: " + tagNumber); }
}
public class AssetAlreadyDisposedException extends ConflictException {
    public AssetAlreadyDisposedException(Long id) { super("Asset " + id + " has already been disposed"); }
}
public class AssetAlreadyFullyDepreciatedException extends BusinessRuleException {
    public AssetAlreadyFullyDepreciatedException() {
        super("Asset is fully depreciated — no further depreciation can be posted");
    }
}
```

---

## Application Layer

### Commands — `com.atlashub.accounting.assets.application.commands`

#### `RegisterAssetCommand`
```java
record RegisterAssetCommand(
    Long organizationId, String name, String tagNumber, String category,
    LocalDate purchaseDate, Money purchaseValue, Money salvageValue,
    int usefulLifeMonths, DepreciationMethod depreciationMethod
)
```
**Handler:** `RegisterAssetHandler` | **Response:** `RegisterAssetResponse(Long assetId)`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:assets:manage')")`  
**Flow:** Create `Asset` (status = ACTIVE, `currentBookValue = purchaseValue`, `accumulatedDepreciation = Money.ZERO`) → `repository.save()`

---

#### `RunDepreciationCommand`
```java
record RunDepreciationCommand(Long organizationId, LocalDate asOfDate)
```
**Handler:** `RunDepreciationHandler` | **Response:** `void`  
**Invocation source:** `DepreciationScheduler` (monthly, cron `0 0 1 * *`)  
**Flow:**
1. Load all `ACTIVE` assets for org
2. For each: `DepreciationService.calculateMonthlyDepreciation(asset)`
3. `asset.depreciate(amount)` — updates `accumulatedDepreciation`, `currentBookValue`; if fully depreciated → `FULLY_DEPRECIATED`
4. Call `RecordJournalEntryHandler` with: Dr Depreciation Expense, Cr Accumulated Depreciation
5. `repository.save(asset)`
6. After all assets processed: publish `AssetDepreciatedEvent` (batch summary)

---

#### `DisposeAssetCommand`
```java
record DisposeAssetCommand(Long assetId, Money saleValue, LocalDate disposalDate, Long disposedBy)
```
**Handler:** `DisposeAssetHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:assets:manage')")`  
**Flow:** Load `Asset` → `asset.dispose(saleValue, disposalDate)` → calculate gain/loss (`saleValue - currentBookValue`) → call `RecordJournalEntryHandler` for disposal entry → save

---

### Queries — `com.atlashub.accounting.assets.application.queries`

#### `ListAssetsQuery`
```java
record ListAssetsQuery(Long organizationId, AssetStatus status)
```
**Handler:** `ListAssetsHandler` | **Result:** `List<AssetResult>` (bounded — org has finite number of fixed assets)

---

#### `GetAssetDetailsQuery`
```java
record GetAssetDetailsQuery(Long assetId)
```
**Handler:** `GetAssetDetailsHandler` | **Result:** `AssetResult`

`AssetResult`: `id`, `name`, `tagNumber`, `category`, `purchaseValue`, `currentBookValue`, `accumulatedDepreciation`, `usefulLifeMonths`, `status`, `purchaseDate`

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `AssetJpaEntity` | `accounting_assets` | `@Version` optimistic |

**Spring Data:**
```
AssetJpaRepository
  + findByOrganizationIdAndStatus(Long orgId, AssetStatus status): List<AssetJpaEntity>
  + findByOrganizationIdAndTagNumber(Long orgId, String tagNumber): Optional<AssetJpaEntity>
```

**Repository Adapter:** `AssetRepositoryAdapter` → `accounting_asset_seq`

### Scheduler — `infrastructure/schedulers/`

#### `DepreciationScheduler`
| Attribute | Value |
|---|---|
| **Cron** | `0 0 1 * *` (1st of each month at midnight) |
| **Action** | Loads all orgs with active assets, calls `RunDepreciationHandler` for each |
| **Response** | `void` — scheduler-triggered, no return value |

---

## Presentation Layer

### Controller: `AccountingAssetsController` — `/api/v1/accounting/assets`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/assets` | `accounting:assets:manage` | `RegisterAssetRequest` | `RegisterAssetResponse` |
| `GET` | `/assets` | — | `?orgId&status` | `List<AssetResult>` |
| `GET` | `/assets/{id}` | — | — | `AssetResult` |
| `POST` | `/assets/{id}/dispose` | `accounting:assets:manage` | `DisposeAssetRequest` | `void` |

---

## Complete File List

```
atlashub-accounting/src/main/java/com/atlashub/accounting/assets/
├── domain/
│   ├── entities/
│   │   └── Asset.java
│   ├── events/
│   │   ├── AssetDepreciatedEvent.java
│   │   └── AssetDisposedEvent.java
│   ├── exceptions/
│   │   ├── AssetNotFoundException.java
│   │   ├── AssetAlreadyDisposedException.java
│   │   └── AssetAlreadyFullyDepreciatedException.java
│   ├── repositories/
│   │   └── AssetRepository.java
│   ├── services/
│   │   └── DepreciationService.java
│   └── valueobject/
│       ├── AssetStatus.java
│       └── DepreciationMethod.java
├── application/
│   ├── commands/
│   │   ├── RegisterAsset/ [RegisterAssetCommand, RegisterAssetHandler, RegisterAssetResponse]
│   │   ├── RunDepreciation/ [RunDepreciationCommand, RunDepreciationHandler]
│   │   └── DisposeAsset/ [DisposeAssetCommand, DisposeAssetHandler]
│   └── queries/
│       ├── ListAssets/ [ListAssetsQuery, ListAssetsHandler]
│       └── GetAssetDetails/ [GetAssetDetailsQuery, GetAssetDetailsHandler, AssetResult]
├── infrastructure/
│   ├── persistence/ [adapters, entities, mappers, repositories]
│   └── schedulers/
│       └── DepreciationScheduler.java
└── presentation/
    ├── dto/ [RegisterAssetRequest, RegisterAssetResponse, DisposeAssetRequest, AssetResult]
    └── rest/
        └── AccountingAssetsController.java
```
