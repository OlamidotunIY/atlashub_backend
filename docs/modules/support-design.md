# Support Module Design (`atlashub-platform:support`)

## Role & Purpose

The `support` module manages the **customer support relationship between AtlasHub and its business customers**. Organizations raise support tickets when they encounter issues with payments, deliveries, account access, or any other platform concern. AtlasHub support agents manage and resolve these tickets.

This is a distinct bounded context from `admin` — `admin` is for internal platform management by AtlasHub staff, while `support` is the customer-facing ticketing channel accessible to organization users.

---

## 1. Features

### Ticket Lifecycle
A `SupportTicket` moves through a clear state machine:

```
OPEN → ASSIGNED → IN_PROGRESS → PENDING_CUSTOMER_RESPONSE → RESOLVED → CLOSED
                                         ↑____resume()_______↓
CLOSED → OPEN  (via reopen())
```

### Ticket Categories
Each ticket is categorized for routing and SLA tracking:

| Category | Description |
|---|---|
| `PAYMENT_DISPUTE` | Transaction dispute, incorrect charge, failed payout |
| `ACCOUNT_ACCESS` | Login issues, member access problems |
| `KYC_COMPLIANCE` | Questions about KYC status or rejection |
| `DELIVERY_ISSUE` | Shipment problem, wrong delivery, POD disputes |
| `TECHNICAL` | API integration issues, webhook failures |
| `BILLING` | Invoice questions, subscription issues |
| `GENERAL` | Anything else |

### SLA Tracking
Each category–priority combination has a configured SLA (Service Level Agreement) for first response and resolution. The system tracks whether tickets are within SLA, flags breached tickets, and auto-closes stale resolved tickets.

### Priority Levels
`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`. HIGH and CRITICAL tickets trigger immediate alerts to the support team via WebSocket and email.

### Thread Messaging
Tickets have a threaded conversation (`TicketMessage`). Both organization users and AtlasHub support agents can add messages. Agents can add internal notes (visible only to agents).

### Escalation
Support agents can escalate a ticket to a senior agent or different team (Compliance, Finance, Engineering). Escalation changes the assigned agent and re-notifies.

### Automated Scheduler Jobs
- **SLA Monitor** (every 5 min): Checks open/in-progress tickets for deadline breaches.
- **Auto-Close** (daily 8 AM): Closes RESOLVED tickets idle for 7 days.

---

## 2. Domain Layer

### `SupportTicket` (Aggregate Root)

```
SupportTicket
├── id: Long
├── ticketNumber: String              ← unique, e.g., "TKT-2026-00123"
├── organizationId: Long
├── raisedBy: Long                    ← userId of the person who created the ticket
├── category: TicketCategory          ← PAYMENT_DISPUTE, ACCOUNT_ACCESS, KYC_COMPLIANCE, DELIVERY_ISSUE, TECHNICAL, BILLING, GENERAL
├── priority: TicketPriority          ← LOW, MEDIUM, HIGH, CRITICAL
├── subject: String
├── status: TicketStatus             ← OPEN, ASSIGNED, IN_PROGRESS, PENDING_CUSTOMER_RESPONSE, RESOLVED, CLOSED
├── assignedTo: Long                  ← adminStaffId, nullable
├── slaBreached: Boolean              ← default false; set true by scheduler
├── relatedEntityType: String         ← nullable — e.g., "Charge", "Shipment", "PayrollRun"
├── relatedEntityId: String           ← nullable
├── firstResponseDeadline: ZonedDateTime
├── resolutionDeadline: ZonedDateTime
├── firstRespondedAt: ZonedDateTime   ← nullable
├── resolvedAt: ZonedDateTime         ← nullable
├── resolutionNote: String            ← nullable; set on resolve()
├── closedAt: ZonedDateTime           ← nullable
├── closedBy: Long                    ← nullable (userId or adminStaffId)
└── createdAt: ZonedDateTime
```

**Business Methods:**

| Method | Transition | Effect |
|---|---|---|
| `assign(Long agentId)` | OPEN → ASSIGNED | Sets `assignedTo`; calls `recordFirstResponse()`; raises `TicketAssignedEvent` |
| `startProgress()` | ASSIGNED → IN_PROGRESS | State guard only |
| `awaitCustomerResponse()` | IN_PROGRESS → PENDING_CUSTOMER_RESPONSE | State guard only |
| `resume()` | PENDING_CUSTOMER_RESPONSE → IN_PROGRESS | State guard only |
| `resolve(Long agentId, String note)` | IN_PROGRESS → RESOLVED | Sets `resolvedAt`, `resolutionNote`; raises `TicketResolvedEvent` |
| `close(Long closedBy)` | RESOLVED → CLOSED | Sets `closedAt`, `closedBy`; raises `TicketClosedEvent` |
| `reopen(Long userId, String reason)` | CLOSED → OPEN | Clears `resolvedAt`, `closedAt`; raises `TicketReopenedEvent` |
| `escalate(Long newAgentId, String reason)` | any → same status | Updates `assignedTo`; raises `TicketEscalatedEvent` |
| `recordFirstResponse()` | — | Sets `firstRespondedAt` if currently null |
| `markSlaBreached()` | — | Sets `slaBreached = true`; raises `TicketSlaBreachedEvent` |

**Domain Rules:**
- A CRITICAL ticket must be assigned within 1 hour.
- `resolve()` enforces that the ticket is IN_PROGRESS before transitioning.
- `escalate()` can be called from any non-CLOSED status.
- An invalid state transition throws `InvalidTicketStateException`.

---

### `TicketMessage` (Entity)

```
TicketMessage
├── id: Long
├── ticketId: Long
├── authorId: Long                    ← userId or adminStaffId
├── authorType: AuthorType            ← CUSTOMER | AGENT
├── message: String
├── isInternalNote: Boolean           ← true = visible only to support agents
├── attachmentUrls: List<String>      ← uploaded files, if any
└── createdAt: ZonedDateTime
```

When a customer adds a message to a ticket in PENDING_CUSTOMER_RESPONSE, the handler also calls `ticket.resume()` to transition it back to IN_PROGRESS.

---

### `TicketSlaConfig` (Entity — Platform-Configured)

```
TicketSlaConfig
├── id: Long
├── category: TicketCategory
├── priority: TicketPriority
├── firstResponseMinutes: Integer     ← SLA for first agent response
└── resolutionMinutes: Integer        ← SLA for full resolution
```

**Example SLAs:**

| Category | Priority | First Response | Resolution |
|---|---|---|---|
| `PAYMENT_DISPUTE` | CRITICAL | 30 min | 4 hours |
| `PAYMENT_DISPUTE` | HIGH | 2 hours | 24 hours |
| `TECHNICAL` | HIGH | 2 hours | 48 hours |
| `GENERAL` | LOW | 24 hours | 7 days |

---

### Value Objects

| Value Object | Fields | Used By |
|---|---|---|
| `TicketCategory` | Enum: PAYMENT_DISPUTE, ACCOUNT_ACCESS, KYC_COMPLIANCE, DELIVERY_ISSUE, TECHNICAL, BILLING, GENERAL | `SupportTicket` |
| `TicketPriority` | Enum: LOW, MEDIUM, HIGH, CRITICAL | `SupportTicket`, `TicketSlaConfig` |
| `TicketStatus` | Enum: OPEN, ASSIGNED, IN_PROGRESS, PENDING_CUSTOMER_RESPONSE, RESOLVED, CLOSED | `SupportTicket` |
| `AuthorType` | Enum: CUSTOMER, AGENT | `TicketMessage` |

---

### Domain Events

| Event | Payload Fields | Published When |
|---|---|---|
| `TicketCreatedEvent` | `ticketId`, `ticketNumber`, `organizationId`, `raisedBy`, `category`, `priority`, `subject` | Ticket opened by an org user |
| `TicketAssignedEvent` | `ticketId`, `agentId`, `organizationId` | Ticket assigned to an agent |
| `TicketMessageAddedEvent` | `ticketId`, `authorId`, `authorType`, `isInternalNote` | New message added to a ticket |
| `TicketEscalatedEvent` | `ticketId`, `newAgentId`, `escalationReason`, `organizationId` | Ticket escalated by an agent |
| `TicketResolvedEvent` | `ticketId`, `agentId`, `resolutionNote`, `organizationId` | Ticket resolved |
| `TicketClosedEvent` | `ticketId`, `closedBy`, `organizationId` | Ticket closed |
| `TicketReopenedEvent` | `ticketId`, `userId`, `reason`, `organizationId` | Closed ticket reopened |
| `TicketSlaBreachedEvent` | `ticketId`, `ticketNumber`, `organizationId`, `priority`, `breachType` | SLA deadline passed |

All events are published on Kafka topic: **`support-events`**.

---

### Domain Exceptions

```java
public class TicketNotFoundException extends NotFoundException {
    public TicketNotFoundException(Long ticketId) {
        super("Support ticket not found: " + ticketId);
    }
}

public class InvalidTicketStateException extends BusinessRuleException {
    public InvalidTicketStateException(String message) { super(message); }
}

public class UnauthorizedTicketActionException extends AuthorizationException {
    public UnauthorizedTicketActionException(String message) { super(message); }
}

public class TicketAlreadyClosedException extends BusinessRuleException {
    public TicketAlreadyClosedException(Long ticketId) {
        super("Ticket " + ticketId + " is already closed");
    }
}
```

---

## 3. Application Layer

### Commands

#### `OpenTicket`
```
package com.atlashub.support.application.commands.OpenTicket

OpenTicketCommand(
    Long orgId,
    Long raisedBy,
    TicketCategory category,
    TicketPriority priority,
    String subject,
    String description,
    String relatedEntityType,   // nullable
    String relatedEntityId      // nullable
)
```
**Handler**: `OpenTicketHandler extends Command<OpenTicketCommand, OpenTicketResponse>`
- Loads `TicketSlaConfig` for the given `category + priority`.
- Generates `ticketNumber` (format: `TKT-{YEAR}-{SEQUENCE}`).
- Creates `SupportTicket` and first `TicketMessage` (the description).
- Publishes `TicketCreatedEvent`.
- Returns `OpenTicketResponse(Long ticketId, String ticketNumber)`.

---

#### `AddTicketMessage`
```
package com.atlashub.support.application.commands.AddTicketMessage

AddTicketMessageCommand(
    Long ticketId,
    Long authorId,
    AuthorType authorType,
    String message,
    boolean isInternalNote,
    List<String> attachmentUrls
)
```
**Handler**: `AddTicketMessageHandler extends Command<AddTicketMessageCommand, Void>`
- Loads ticket; throws `TicketNotFoundException` if absent.
- Throws `UnauthorizedTicketActionException` if `isInternalNote=true` and `authorType=CUSTOMER`.
- If `authorType=CUSTOMER` and ticket status is `PENDING_CUSTOMER_RESPONSE`, calls `ticket.resume()`.
- Saves `TicketMessage`; publishes `TicketMessageAddedEvent`.
- Returns `void`.

---

#### `AssignTicket`
```
package com.atlashub.support.application.commands.AssignTicket

AssignTicketCommand(Long ticketId, Long agentId)
```
**Handler**: `AssignTicketHandler extends Command<AssignTicketCommand, Void>`
- Requires `support:tickets:assign` authority.
- Calls `ticket.assign(agentId)`.
- Saves; publishes `TicketAssignedEvent`.

---

#### `StartTicketProgress`
```
package com.atlashub.support.application.commands.StartTicketProgress

StartTicketProgressCommand(Long ticketId, Long agentId)
```
**Handler**: `StartTicketProgressHandler extends Command<StartTicketProgressCommand, Void>`
- Calls `ticket.startProgress()`.
- Validates agent is the assigned agent; throws `UnauthorizedTicketActionException` otherwise.

---

#### `ResolveTicket`
```
package com.atlashub.support.application.commands.ResolveTicket

ResolveTicketCommand(Long ticketId, Long agentId, String resolutionNote)
```
**Handler**: `ResolveTicketHandler extends Command<ResolveTicketCommand, Void>`
- Requires `support:tickets:resolve` authority.
- Calls `ticket.resolve(agentId, resolutionNote)`.
- Publishes `TicketResolvedEvent`.

---

#### `CloseTicket`
```
package com.atlashub.support.application.commands.CloseTicket

CloseTicketCommand(Long ticketId, Long closedBy)
```
**Handler**: `CloseTicketHandler extends Command<CloseTicketCommand, Void>`
- Accepts calls from either a user (`closedBy = userId`) or the scheduler (internal system call).
- Calls `ticket.close(closedBy)`.
- Publishes `TicketClosedEvent`.

---

#### `ReopenTicket`
```
package com.atlashub.support.application.commands.ReopenTicket

ReopenTicketCommand(Long ticketId, Long userId, String reason)
```
**Handler**: `ReopenTicketHandler extends Command<ReopenTicketCommand, Void>`
- Only the original `raisedBy` user (or org admin) may reopen. Throws `UnauthorizedTicketActionException` otherwise.
- Calls `ticket.reopen(userId, reason)`.
- Publishes `TicketReopenedEvent`.

---

#### `EscalateTicket`
```
package com.atlashub.support.application.commands.EscalateTicket

EscalateTicketCommand(Long ticketId, Long newAgentId, String escalationReason)
```
**Handler**: `EscalateTicketHandler extends Command<EscalateTicketCommand, Void>`
- Requires `support:tickets:escalate` authority.
- Calls `ticket.escalate(newAgentId, escalationReason)`.
- Publishes `TicketEscalatedEvent`.

---

#### `MarkSlaBreached`
```
package com.atlashub.support.application.commands.MarkSlaBreached

MarkSlaBreachedCommand(Long ticketId)
```
**Handler**: `MarkSlaBreachedHandler extends Command<MarkSlaBreachedCommand, Void>`
- **Scheduler-triggered only** — not exposed via REST.
- Calls `ticket.markSlaBreached()`.
- Publishes `TicketSlaBreachedEvent`.
- Returns `void`.

---

### Queries

#### `ListMyTickets`
```
package com.atlashub.support.application.queries.ListMyTickets

ListMyTicketsQuery(Long orgId, TicketStatus status, int page, int size)
```
**Handler**: `ListMyTicketsHandler extends Query<ListMyTicketsQuery, PageResult<TicketSummaryResult>>`
- Requires `support:tickets:create` authority (any org user).
- Returns `PageResult<TicketSummaryResult>` — only tickets where `organizationId = orgId`.
- Applies RLS implicitly at DB level.

`TicketSummaryResult`: `ticketId`, `ticketNumber`, `subject`, `category`, `priority`, `status`, `slaBreached`, `assignedTo`, `createdAt`, `updatedAt`.

---

#### `GetTicketDetails`
```
package com.atlashub.support.application.queries.GetTicketDetails

GetTicketDetailsQuery(Long ticketId, Long requesterId, boolean isAgent)
```
**Handler**: `GetTicketDetailsHandler extends Query<GetTicketDetailsQuery, TicketDetailsResult>`
- Returns the full ticket plus all non-internal `TicketMessage` list.
- If `isAgent = true`, also includes internal notes.
- Throws `TicketNotFoundException` or `UnauthorizedTicketActionException` as appropriate.

`TicketDetailsResult`: all `SupportTicket` fields + `List<TicketMessageResult>` (messageId, authorId, authorType, message, isInternalNote, attachmentUrls, createdAt).

---

#### `ListAgentTickets`
```
package com.atlashub.support.application.queries.ListAgentTickets

ListAgentTicketsQuery(Long agentId, TicketStatus status, int page, int size)
```
**Handler**: `ListAgentTicketsHandler extends Query<ListAgentTicketsQuery, PageResult<TicketSummaryResult>>`
- Requires `support:tickets:assign` authority.
- Returns tickets assigned to the requesting agent, filtered by status.

---

#### `ListAllTickets`
```
package com.atlashub.support.application.queries.ListAllTickets

ListAllTicketsQuery(TicketStatus status, TicketCategory category, TicketPriority priority, Boolean slaBreached, int page, int size)
```
**Handler**: `ListAllTicketsHandler extends Query<ListAllTicketsQuery, PageResult<TicketSummaryResult>>`
- Requires admin-level authority (support team lead / admin staff).
- All filters are nullable — null = no filter applied.

---

#### `GetSlaMetrics`
```
package com.atlashub.support.application.queries.GetSlaMetrics

GetSlaMetricsQuery(ZonedDateTime dateFrom, ZonedDateTime dateTo)
```
**Handler**: `GetSlaMetricsHandler extends Query<GetSlaMetricsQuery, SlaMetricsResult>`
- Computes SLA compliance metrics across the given date range.

`SlaMetricsResult`: `totalTickets`, `firstResponseSlaRate` (%), `resolutionSlaRate` (%), `avgFirstResponseMinutes`, `avgResolutionMinutes`, `breachedCount`, broken down by `category` and `priority`.

---

> [!NOTE]
> `PageResult<T>` is used for all ticket list queries because the ticket volume is unbounded and user-facing. `SlaMetricsResult` and `TicketDetailsResult` return single aggregated results — no pagination needed.

---

## 4. Infrastructure Layer

### Persistence

#### JPA Entities

**`SupportTicketJpa`** (`support_tickets`)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `ticket_number` | VARCHAR(30) UNIQUE NOT NULL | |
| `organization_id` | BIGINT NOT NULL | FK-by-value to accounts |
| `raised_by` | BIGINT NOT NULL | |
| `category` | VARCHAR(30) NOT NULL | |
| `priority` | VARCHAR(20) NOT NULL | |
| `subject` | VARCHAR(255) NOT NULL | |
| `status` | VARCHAR(40) NOT NULL | |
| `assigned_to` | BIGINT | nullable |
| `sla_breached` | BOOLEAN NOT NULL DEFAULT FALSE | |
| `related_entity_type` | VARCHAR(50) | nullable |
| `related_entity_id` | VARCHAR(100) | nullable |
| `first_response_deadline` | TIMESTAMPTZ NOT NULL | |
| `resolution_deadline` | TIMESTAMPTZ NOT NULL | |
| `first_responded_at` | TIMESTAMPTZ | nullable |
| `resolved_at` | TIMESTAMPTZ | nullable |
| `resolution_note` | TEXT | nullable |
| `closed_at` | TIMESTAMPTZ | nullable |
| `closed_by` | BIGINT | nullable |
| `created_at` | TIMESTAMPTZ NOT NULL | |

Indexes: `(organization_id, status)`, `(assigned_to, status)`, `(status, sla_breached)`, `(resolution_deadline)` (used by scheduler).

---

**`TicketMessageJpa`** (`ticket_messages`)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `ticket_id` | BIGINT NOT NULL FK → support_tickets | |
| `author_id` | BIGINT NOT NULL | |
| `author_type` | VARCHAR(20) NOT NULL | CUSTOMER / AGENT |
| `message` | TEXT NOT NULL | |
| `is_internal_note` | BOOLEAN NOT NULL DEFAULT FALSE | |
| `attachment_urls` | TEXT[] | PostgreSQL array |
| `created_at` | TIMESTAMPTZ NOT NULL | |

---

**`TicketSlaConfigJpa`** (`ticket_sla_configs`)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `category` | VARCHAR(30) NOT NULL | |
| `priority` | VARCHAR(20) NOT NULL | |
| `first_response_minutes` | INTEGER NOT NULL | |
| `resolution_minutes` | INTEGER NOT NULL | |

Unique constraint: `(category, priority)`.

---

#### Mappers

- `SupportTicketMapper` — maps `SupportTicketJpa` ↔ `SupportTicket` domain entity.
- `TicketMessageMapper` — maps `TicketMessageJpa` ↔ `TicketMessage` domain entity.

---

#### Spring Data Repositories

```java
// com.atlashub.support.infrastructure.persistence.repositories

public interface SupportTicketRepository extends JpaRepository<SupportTicketJpa, Long> {
    Page<SupportTicketJpa> findByOrganizationIdAndStatus(Long orgId, String status, Pageable pageable);
    Page<SupportTicketJpa> findByAssignedToAndStatus(Long agentId, String status, Pageable pageable);
    Page<SupportTicketJpa> findByStatusAndCategoryAndPriorityAndSlaBreached(
        String status, String category, String priority, Boolean slaBreached, Pageable pageable);

    // Used by SLA monitor scheduler
    @Query("SELECT t FROM SupportTicketJpa t WHERE t.status IN ('OPEN','ASSIGNED','IN_PROGRESS') " +
           "AND t.slaBreached = false " +
           "AND (t.firstRespondedAt IS NULL AND t.firstResponseDeadline < :now " +
           "     OR t.resolutionDeadline < :now)")
    List<SupportTicketJpa> findSlaOverdueTickets(ZonedDateTime now);

    // Used by auto-close scheduler
    @Query("SELECT t FROM SupportTicketJpa t WHERE t.status = 'RESOLVED' AND t.resolvedAt < :cutoff")
    List<SupportTicketJpa> findIdleResolvedTickets(ZonedDateTime cutoff);
}

public interface TicketMessageRepository extends JpaRepository<TicketMessageJpa, Long> {
    List<TicketMessageJpa> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}

public interface TicketSlaConfigRepository extends JpaRepository<TicketSlaConfigJpa, Long> {
    Optional<TicketSlaConfigJpa> findByCategoryAndPriority(String category, String priority);
}
```

---

#### Persistence Adapters

- **`SupportTicketPersistenceAdapter`** — implements `SupportTicketRepositoryPort` (load by ID, save, query).
- **`TicketMessagePersistenceAdapter`** — implements `TicketMessageRepositoryPort` (save, findByTicketId).
- **`TicketSlaConfigPersistenceAdapter`** — implements `TicketSlaConfigRepositoryPort` (findByCategoryAndPriority).

---

### Schedulers

#### `SlaMonitorScheduler`

```java
@Component
public class SlaMonitorScheduler {
    private final MarkSlaBreachedHandler markSlaBreachedHandler;
    private final SupportTicketRepository ticketRepository;

    @Scheduled(cron = "0 */5 * * * *")   // every 5 minutes
    public void checkSlaBreaches() {
        List<SupportTicketJpa> overdueTickets =
            ticketRepository.findSlaOverdueTickets(ZonedDateTime.now());

        overdueTickets.forEach(t ->
            markSlaBreachedHandler.execute(new MarkSlaBreachedCommand(t.getId()))
        );
    }
}
```

Invokes: `MarkSlaBreachedCommand` → `MarkSlaBreachedHandler` → `ticket.markSlaBreached()` → `TicketSlaBreachedEvent`.

---

#### `AutoCloseScheduler`

```java
@Component
public class AutoCloseScheduler {
    private final CloseTicketHandler closeTicketHandler;
    private final SupportTicketRepository ticketRepository;

    private static final Long SYSTEM_ACTOR_ID = -1L;  // sentinel for scheduler-initiated close

    @Scheduled(cron = "0 0 8 * * *")    // daily at 08:00
    public void autoCloseIdleResolvedTickets() {
        ZonedDateTime cutoff = ZonedDateTime.now().minusDays(7);
        List<SupportTicketJpa> idleTickets =
            ticketRepository.findIdleResolvedTickets(cutoff);

        idleTickets.forEach(t ->
            closeTicketHandler.execute(new CloseTicketCommand(t.getId(), SYSTEM_ACTOR_ID))
        );
    }
}
```

Invokes: `CloseTicketCommand` → `CloseTicketHandler` → `ticket.close(SYSTEM_ACTOR_ID)` → `TicketClosedEvent`.

---

### Messaging

No inbound Kafka listeners in the MVP — the support module only **produces** events. If future integration requires listening (e.g., `ChargeDisputedEvent` → auto-open a PAYMENT_DISPUTE ticket), a listener would be added here.

---

## 5. Presentation Layer

### Controller

**`SupportTicketController`** — `@RequestMapping("/api/v1/support")`

---

#### `POST /api/v1/support/tickets` — Open Ticket

| | |
|---|---|
| **Method** | POST |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:create` |
| **Handler** | `OpenTicketHandler` |

**Request DTO** (`OpenTicketRequest`):
```
subject: String           (required, max 255)
description: String       (required)
category: TicketCategory  (required)
priority: TicketPriority  (default LOW)
relatedEntityType: String (nullable)
relatedEntityId: String   (nullable)
```

**Response** `ApiResponse<OpenTicketResponse>`:
```
ticketId: Long
ticketNumber: String
```

---

#### `POST /api/v1/support/tickets/{ticketId}/messages` — Add Message

| | |
|---|---|
| **Method** | POST |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:create` |
| **Handler** | `AddTicketMessageHandler` |

**Request DTO** (`AddTicketMessageRequest`):
```
message: String              (required)
isInternalNote: boolean      (default false; only agents can set true)
attachmentUrls: List<String> (nullable)
```
`@AuthenticationPrincipal Long userId` is used to derive `authorId` and `authorType`.

---

#### `POST /api/v1/support/tickets/{ticketId}/assign` — Assign Ticket

| | |
|---|---|
| **Method** | POST |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:assign` |
| **Handler** | `AssignTicketHandler` |

**Request DTO** (`AssignTicketRequest`): `agentId: Long`.

---

#### `POST /api/v1/support/tickets/{ticketId}/start` — Start Progress

| | |
|---|---|
| **Method** | POST |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:assign` |
| **Handler** | `StartTicketProgressHandler` |

No request body.

---

#### `POST /api/v1/support/tickets/{ticketId}/resolve` — Resolve Ticket

| | |
|---|---|
| **Method** | POST |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:resolve` |
| **Handler** | `ResolveTicketHandler` |

**Request DTO** (`ResolveTicketRequest`): `resolutionNote: String` (required).

---

#### `POST /api/v1/support/tickets/{ticketId}/close` — Close Ticket

| | |
|---|---|
| **Method** | POST |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:create` (org user self-close) |
| **Handler** | `CloseTicketHandler` |

No request body. `closedBy` derived from `@AuthenticationPrincipal`.

---

#### `POST /api/v1/support/tickets/{ticketId}/reopen` — Reopen Ticket

| | |
|---|---|
| **Method** | POST |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:create` |
| **Handler** | `ReopenTicketHandler` |

**Request DTO** (`ReopenTicketRequest`): `reason: String` (required).

---

#### `POST /api/v1/support/tickets/{ticketId}/escalate` — Escalate Ticket

| | |
|---|---|
| **Method** | POST |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:escalate` |
| **Handler** | `EscalateTicketHandler` |

**Request DTO** (`EscalateTicketRequest`): `newAgentId: Long`, `escalationReason: String`.

---

#### `GET /api/v1/support/tickets` — List My Tickets (Org User)

| | |
|---|---|
| **Method** | GET |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:create` |
| **Handler** | `ListMyTicketsHandler` |

**Query params**: `status` (optional), `page` (default 0), `size` (default 20).

**Response**: `ApiResponse<PageResult<TicketSummaryDto>>`.

---

#### `GET /api/v1/support/tickets/{ticketId}` — Get Ticket Details

| | |
|---|---|
| **Method** | GET |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:create` (own org) or `support:tickets:assign` (agents) |
| **Handler** | `GetTicketDetailsHandler` |

**Response**: `ApiResponse<TicketDetailsDto>`.

---

#### `GET /api/v1/support/agent/tickets` — List Agent's Tickets

| | |
|---|---|
| **Method** | GET |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:assign` |
| **Handler** | `ListAgentTicketsHandler` |

**Query params**: `status` (optional), `page`, `size`.

---

#### `GET /api/v1/support/admin/tickets` — List All Tickets

| | |
|---|---|
| **Method** | GET |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:escalate` (team leads / admin) |
| **Handler** | `ListAllTicketsHandler` |

**Query params**: `status`, `category`, `priority`, `slaBreached` (all optional), `page`, `size`.

---

#### `GET /api/v1/support/admin/sla-metrics` — SLA Metrics

| | |
|---|---|
| **Method** | GET |
| **Auth** | Bearer JWT |
| **RBAC** | `support:tickets:escalate` |
| **Handler** | `GetSlaMetricsHandler` |

**Query params**: `dateFrom: ZonedDateTime`, `dateTo: ZonedDateTime`.

**Response DTO** (`SlaMetricsDto`): `totalTickets`, `firstResponseSlaRate`, `resolutionSlaRate`, `avgFirstResponseMinutes`, `avgResolutionMinutes`, `breachedCount`, `byCategory: List<SlaCategoryBreakdown>`.

---

### Response DTOs

**`TicketSummaryDto`**: `ticketId`, `ticketNumber`, `subject`, `category`, `priority`, `status`, `slaBreached`, `assignedTo`, `createdAt`, `updatedAt`.

**`TicketDetailsDto`**: All `TicketSummaryDto` fields + `organizationId`, `raisedBy`, `resolutionNote`, `firstResponseDeadline`, `resolutionDeadline`, `firstRespondedAt`, `resolvedAt`, `closedAt`, `closedBy` + `messages: List<TicketMessageDto>`.

**`TicketMessageDto`**: `messageId`, `authorId`, `authorType`, `message`, `isInternalNote`, `attachmentUrls`, `createdAt`.

**`OpenTicketResponse`**: `ticketId`, `ticketNumber`.

---

## 6. RBAC Table

| Permission | Who Holds It | Commands / Endpoints |
|---|---|---|
| `support:tickets:create` | All active org users | `OpenTicket`, `AddTicketMessage`, `CloseTicket`, `ReopenTicket`, `ListMyTickets`, `GetTicketDetails` |
| `support:tickets:assign` | Support agents (AtlasHub staff) | `AssignTicket`, `StartTicketProgress`, `ListAgentTickets`, `ListAllTickets` (read-only) |
| `support:tickets:resolve` | Support agents | `ResolveTicket` |
| `support:tickets:escalate` | Senior support agents / team leads | `EscalateTicket`, `ListAllTickets`, `GetSlaMetrics` |

RBAC is enforced with `@PreAuthorize("hasAuthority('support:tickets:resolve')")` on handler `execute()` methods (or controller methods for query handlers).

---

## 7. Maker-Checker

The support module does **not** require maker-checker approval for any operation. Ticket lifecycle actions are real-time operational decisions, not financial transactions. The SLA system and escalation path provide oversight without dual-approval overhead.

---

## 8. WebSocket Events

Modules publish domain events to Kafka. The `SelectiveWebSocketBroadcaster` in `atlashub-infrastructure` decides which events to forward to WebSocket clients.

| Domain Event | WS Channel | Push Notification Type | Payload |
|---|---|---|---|
| `TicketCreatedEvent` | `/topic/org/{orgId}/support` | `TICKET_CREATED` | `{ type, message: "New {priority} ticket: {subject}" }` |
| `TicketAssignedEvent` | `/user/{agentId}/queue/notifications` | `TICKET_ASSIGNED` | `{ type, message: "Ticket {ticketNumber} assigned to you" }` |

> [!NOTE]
> The broadcaster listens to `support-events` Kafka topic. It does **not** push `TicketMessageAddedEvent`, `TicketResolvedEvent`, or `TicketSlaBreachedEvent` via WebSocket — these are handled by the `notifications` module via email/SMS.

---

## 9. Domain Events Table

| Event | Published When | Consumed By |
|---|---|---|
| `TicketCreatedEvent` | Ticket opened by org user | `notifications` (confirm to org, alert support team queue via WS) |
| `TicketAssignedEvent` | Ticket assigned to agent | `notifications` (notify agent via WS + email) |
| `TicketMessageAddedEvent` | New message added | `notifications` (notify the other party) |
| `TicketEscalatedEvent` | Ticket escalated | `notifications` (notify new agent + org) |
| `TicketResolvedEvent` | Ticket resolved | `notifications` (email org with resolution + CSAT survey link) |
| `TicketClosedEvent` | Ticket closed or auto-closed | `audit` |
| `TicketReopenedEvent` | Closed ticket reopened | `notifications` (notify agent) |
| `TicketSlaBreachedEvent` | SLA deadline passed | `notifications` (alert team lead), `audit` |

All events published to Kafka topic: **`support-events`**.

---

## 10. Distributed Architecture

### Locking
`SupportTicketRepository` uses a `@Lock(LockModeType.PESSIMISTIC_WRITE)` query when loading a ticket for state-changing commands (`assign`, `resolve`, `close`, `escalate`) to prevent concurrent state transitions on the same ticket.

### Outbox Pattern
All domain events are written to the **outbox table** within the same transaction as the aggregate save. A background `OutboxPoller` publishes them to Kafka, guaranteeing at-least-once delivery.

### Idempotency
Scheduler-triggered commands (`MarkSlaBreached`, auto-close `CloseTicket`) check the ticket's current state before applying changes. If the ticket is already in the target state (e.g., already `slaBreached=true`), the command is a no-op.

---

## 11. Complete File List

```
atlashub-platform/support/
└── src/main/java/com/atlashub/support/
    ├── application/
    │   ├── commands/
    │   │   ├── OpenTicket/
    │   │   │   ├── OpenTicketCommand.java
    │   │   │   ├── OpenTicketHandler.java
    │   │   │   └── OpenTicketResponse.java
    │   │   ├── AddTicketMessage/
    │   │   │   ├── AddTicketMessageCommand.java
    │   │   │   └── AddTicketMessageHandler.java
    │   │   ├── AssignTicket/
    │   │   │   ├── AssignTicketCommand.java
    │   │   │   └── AssignTicketHandler.java
    │   │   ├── StartTicketProgress/
    │   │   │   ├── StartTicketProgressCommand.java
    │   │   │   └── StartTicketProgressHandler.java
    │   │   ├── ResolveTicket/
    │   │   │   ├── ResolveTicketCommand.java
    │   │   │   └── ResolveTicketHandler.java
    │   │   ├── CloseTicket/
    │   │   │   ├── CloseTicketCommand.java
    │   │   │   └── CloseTicketHandler.java
    │   │   ├── ReopenTicket/
    │   │   │   ├── ReopenTicketCommand.java
    │   │   │   └── ReopenTicketHandler.java
    │   │   ├── EscalateTicket/
    │   │   │   ├── EscalateTicketCommand.java
    │   │   │   └── EscalateTicketHandler.java
    │   │   └── MarkSlaBreached/
    │   │       ├── MarkSlaBreachedCommand.java
    │   │       └── MarkSlaBreachedHandler.java
    │   ├── queries/
    │   │   ├── ListMyTickets/
    │   │   │   ├── ListMyTicketsQuery.java
    │   │   │   └── ListMyTicketsHandler.java
    │   │   ├── GetTicketDetails/
    │   │   │   ├── GetTicketDetailsQuery.java
    │   │   │   ├── GetTicketDetailsHandler.java
    │   │   │   └── TicketDetailsResult.java
    │   │   ├── ListAgentTickets/
    │   │   │   ├── ListAgentTicketsQuery.java
    │   │   │   └── ListAgentTicketsHandler.java
    │   │   ├── ListAllTickets/
    │   │   │   ├── ListAllTicketsQuery.java
    │   │   │   └── ListAllTicketsHandler.java
    │   │   └── GetSlaMetrics/
    │   │       ├── GetSlaMetricsQuery.java
    │   │       ├── GetSlaMetricsHandler.java
    │   │       └── SlaMetricsResult.java
    │   └── port/
    │       ├── SupportTicketRepositoryPort.java
    │       ├── TicketMessageRepositoryPort.java
    │       └── TicketSlaConfigRepositoryPort.java
    ├── domain/
    │   ├── entities/
    │   │   ├── SupportTicket.java
    │   │   ├── TicketMessage.java
    │   │   └── TicketSlaConfig.java
    │   ├── events/
    │   │   ├── TicketCreatedEvent.java
    │   │   ├── TicketAssignedEvent.java
    │   │   ├── TicketMessageAddedEvent.java
    │   │   ├── TicketEscalatedEvent.java
    │   │   ├── TicketResolvedEvent.java
    │   │   ├── TicketClosedEvent.java
    │   │   ├── TicketReopenedEvent.java
    │   │   └── TicketSlaBreachedEvent.java
    │   ├── exceptions/
    │   │   ├── TicketNotFoundException.java
    │   │   ├── InvalidTicketStateException.java
    │   │   ├── UnauthorizedTicketActionException.java
    │   │   └── TicketAlreadyClosedException.java
    │   └── valueobject/
    │       ├── TicketCategory.java
    │       ├── TicketPriority.java
    │       ├── TicketStatus.java
    │       └── AuthorType.java
    ├── infrastructure/
    │   ├── persistence/
    │   │   ├── adapters/
    │   │   │   ├── SupportTicketPersistenceAdapter.java
    │   │   │   ├── TicketMessagePersistenceAdapter.java
    │   │   │   └── TicketSlaConfigPersistenceAdapter.java
    │   │   ├── entities/
    │   │   │   ├── SupportTicketJpa.java
    │   │   │   ├── TicketMessageJpa.java
    │   │   │   └── TicketSlaConfigJpa.java
    │   │   ├── mappers/
    │   │   │   ├── SupportTicketMapper.java
    │   │   │   └── TicketMessageMapper.java
    │   │   └── repositories/
    │   │       ├── SupportTicketRepository.java
    │   │       ├── TicketMessageRepository.java
    │   │       └── TicketSlaConfigRepository.java
    │   └── scheduling/
    │       ├── SlaMonitorScheduler.java
    │       └── AutoCloseScheduler.java
    └── presentation/
        ├── dto/
        │   ├── OpenTicketRequest.java
        │   ├── AddTicketMessageRequest.java
        │   ├── AssignTicketRequest.java
        │   ├── ResolveTicketRequest.java
        │   ├── ReopenTicketRequest.java
        │   ├── EscalateTicketRequest.java
        │   ├── TicketSummaryDto.java
        │   ├── TicketDetailsDto.java
        │   ├── TicketMessageDto.java
        │   └── SlaMetricsDto.java
        └── rest/
            └── SupportTicketController.java
```
