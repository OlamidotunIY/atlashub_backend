# Multi-Tenancy Architecture

## The Model: Row-Level Tenancy + PostgreSQL RLS

AtlasHub is a **multi-tenant SaaS platform**. Every organization is a tenant. All tenants share the same database schema (no schema-per-tenant, no database-per-tenant).

### Why Not Schema-Per-Tenant?
Schema-per-tenant is operationally complex at scale: running a database migration requires looping over every tenant's schema, you can't easily run cross-tenant queries for platform analytics, and connection pool sizing becomes tricky with thousands of tenants. It is the right choice for regulated industries with strict data isolation requirements (healthcare, government), but not for a multi-product business platform like AtlasHub.

### Why Not Database-Per-Tenant?
Even more operationally expensive, and the isolation benefit is not needed here.

### The Correct Model for AtlasHub
Row-level tenancy: every table has an `organization_id` column. All queries are scoped by this column. Defense-in-depth is provided by PostgreSQL Row-Level Security (RLS) as a second enforcement layer.

---

## Layer 1: Application-Level Tenant Scoping

Every query in every module always includes `WHERE organization_id = :orgId`. This is enforced by the repository adapter implementations.

```java
// In SpringDataChargeRepository
List<ChargeJpaEntity> findByOrganizationIdAndStatus(Long organizationId, ChargeStatus status);

// In ChargeRepositoryAdapter — NEVER called without orgId
public List<Charge> findByStatus(Long orgId, ChargeStatus status) {
    return jpa.findByOrganizationIdAndStatus(orgId, status)
        .stream().map(mapper::toDomain).toList();
}
```

The `organizationId` is extracted from the **JWT access token** in every authenticated request:

```java
@Component
public class TenantContextHolder {
    private static final ThreadLocal<Long> CURRENT_ORG_ID = new ThreadLocal<>();

    public static void set(Long orgId) { CURRENT_ORG_ID.set(orgId); }
    public static Long get() { return CURRENT_ORG_ID.get(); }
    public static void clear() { CURRENT_ORG_ID.remove(); }
}

// In the security filter — runs on every request
@Component
public class TenantFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        JwtAuthPrincipal principal = (JwtAuthPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        TenantContextHolder.set(principal.activeOrganizationId());
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();  // always clean up ThreadLocal
        }
    }
}
```

---

## Layer 2: PostgreSQL Row-Level Security (Defense-in-Depth)

RLS is a PostgreSQL feature that enforces access policies at the database level — even if application code has a bug and forgets to include `WHERE organization_id = ?`, the database will silently filter out rows that don't belong to the current tenant context.

### Setup

```sql
-- Set the current org ID on the database session (done in a Spring Hibernate interceptor)
SET LOCAL app.current_org_id = '12345';

-- RLS policy on every table
ALTER TABLE charges ENABLE ROW LEVEL SECURITY;

CREATE POLICY org_isolation ON charges
    USING (organization_id = NULLIF(current_setting('app.current_org_id', TRUE), '')::BIGINT);
```

### Spring Integration (Hibernate Session Customizer)

```java
@Component
public class TenantRlsInterceptor implements Interceptor {

    @Override
    public void beforeTransactionCompletion(Transaction tx) {
        Long orgId = TenantContextHolder.get();
        if (orgId != null) {
            Session session = // ... get current Hibernate session
            session.createNativeMutationQuery("SET LOCAL app.current_org_id = :orgId")
                .setParameter("orgId", orgId.toString())
                .executeUpdate();
        }
    }
}
```

### Tables That Have RLS Applied
All tables with an `organization_id` column. Tables without it:
- `admin_staff` — no org context, AtlasHub internal
- `notification_templates` — platform-wide
- `permissions` — platform-wide
- `atlashub_products` (catalog) — platform-wide

### Superuser / Admin Bypass
AtlasHub's admin operations run under a separate DB role with `BYPASSRLS` or `SET app.current_org_id = ''` (empty string → RLS policy evaluates to false for all rows, bypassed). Admin queries explicitly include `organization_id` themselves.

---

## Layer 3: `organization_id` Index Strategy

Because every query filters by `organization_id`, every table must have an index that makes this efficient. For most tables, a composite index `(organization_id, <secondary_filter>)` is ideal:

```sql
-- For charges: most queries filter by org + status or org + date
CREATE INDEX idx_charges_org_status ON charges (organization_id, status);
CREATE INDEX idx_charges_org_created ON charges (organization_id, created_at DESC);

-- For sales_orders: filter by org + outlet + date
CREATE INDEX idx_sales_orders_org_outlet ON sales_orders (organization_id, outlet_id, sale_date DESC);

-- For employees: filter by org + status
CREATE INDEX idx_employees_org_status ON employees (organization_id, status);
```

---

## Cross-Tenant Access (Platform Analytics)

AtlasHub's own analytics (total platform volume, fee revenue) require cross-tenant queries. These run under the admin DB role with RLS bypassed. They are never exposed to org users — only to AtlasHub internal dashboards.

```java
// In atlashub-analytics:admin
@Repository
public class PlatformAnalyticsRepository {
    @PersistenceContext
    private EntityManager em;

    // This method runs with bypass — admin context only, never called from org-scoped endpoints
    public BigDecimal getTotalPlatformVolumeForMonth(YearMonth month) {
        // application-level: TenantContextHolder.get() returns null here (admin context)
        // RLS: no org_id SET, so RLS policy is satisfied by the bypass role
        return em.createQuery("SELECT SUM(a.totalVolume.amount) FROM TransactionVolumeProjection a " +
                              "WHERE a.month = :month", BigDecimal.class)
            .setParameter("month", month)
            .getSingleResult();
    }
}
```

---

## Tenant Isolation Risk Checklist

| Risk | Mitigation |
|---|---|
| Forgot `WHERE organization_id` in a query | RLS catches it at DB level |
| JWT contains wrong `activeOrganizationId` | `MembershipQueryPort.isMemberOf()` re-validates on sensitive operations |
| Admin bypassing org scope by accident | Admin endpoints use separate service layer that explicitly requires null `TenantContextHolder` |
| Thread pool reuse leaking org ID | `TenantContextHolder.clear()` called in `finally` block of filter |
| Async tasks (Kafka listeners) carry no user context | Kafka listeners set `TenantContextHolder` from event payload's `organizationId` before calling use cases |

---

## New Tenant Onboarding

When a new organization is created:
1. `RegisterOrganizationUseCase` creates the `Organization` row with a new `id`
2. All downstream modules bootstrap their org-scoped data by reacting to `OrganizationCreatedEvent`:
   - `compliance` creates `ComplianceRecord`
   - `iam` creates the OWNER role and founder membership
   - `billing` initializes subscription state
3. No DDL is run — no new schema, no new tables. It's just a row.

This is why row-level tenancy scales: onboarding the 10,000th tenant is identical to onboarding the first.
