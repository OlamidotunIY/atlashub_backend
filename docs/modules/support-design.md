# Support Module Design (`atlashub-platform:support`)

## Role & Purpose

The `support` module manages the **customer support relationship between AtlasHub and its business customers**. Organizations can raise support tickets when they encounter issues with payments, deliveries, account access, or any other aspect of the platform. AtlasHub support agents manage and resolve these tickets.

This is a distinct bounded context from `admin` — `admin` is for internal platform management by AtlasHub staff, while `support` is the customer-facing ticketing channel accessible by organizations.

---

## 1. Features

### Ticket Lifecycle
A `SupportTicket` moves through a clear state machine: OPEN → ASSIGNED → IN_PROGRESS → PENDING_CUSTOMER_RESPONSE → RESOLVED → CLOSED. Organizations can re-open a closed ticket if the issue recurs.

### Ticket Categories
Each ticket is categorized for routing and SLA tracking:
- `PAYMENT_DISPUTE` — transaction dispute, incorrect charge, failed payout
- `ACCOUNT_ACCESS` — login issues, member access problems
- `KYC_COMPLIANCE` — questions about KYC status or rejection
- `DELIVERY_ISSUE` — shipment problem, wrong delivery, POD disputes
- `TECHNICAL` — API integration issues, webhook failures
- `BILLING` — invoice questions, subscription issues
- `GENERAL` — anything else

### SLA Tracking
Each category has a configured SLA (Service Level Agreement) for first response and resolution. The system tracks whether tickets are within SLA, warns before breach, and flags breached tickets.

### Priority Levels
`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`. HIGH and CRITICAL tickets trigger immediate alerts to the support team via WebSocket and email.

### Thread Messaging
Tickets have a threaded conversation (`TicketMessage`). Both the organization's users and AtlasHub support agents can add messages. Support agents can add internal notes (visible only to agents).

### Escalation
Support agents can escalate a ticket to a senior agent or to the appropriate team (Compliance, Finance, Engineering). Escalation changes the assigned agent and re-notifies.

### Automated Suggestions (Future)
When a ticket is created, the system searches the knowledge base for related articles and suggests them to the user before they submit — self-service deflection.

---

## 2. Domain Entities & Aggregates

### `SupportTicket` (Aggregate Root)

```
SupportTicket
├── id: Long
├── ticketNumber: String             ← unique, e.g., "TKT-2026-00123"
├── organizationId: Long
├── raisedBy: Long                   ← userId of the person who created the ticket
├── category: TicketCategory         ← PAYMENT_DISPUTE, ACCOUNT_ACCESS, etc.
├── priority: TicketPriority         ← LOW, MEDIUM, HIGH, CRITICAL
├── subject: String
├── status: TicketStatus             ← OPEN, ASSIGNED, IN_PROGRESS, PENDING_CUSTOMER_RESPONSE, RESOLVED, CLOSED
├── assignedTo: Long                 ← adminStaffId, nullable
├── relatedEntityType: String        ← nullable — e.g., "Charge", "Shipment", "PayrollRun"
├── relatedEntityId: String          ← nullable — the ID of the business entity the ticket is about
├── firstResponseDeadline: ZonedDateTime ← SLA deadline
├── resolutionDeadline: ZonedDateTime    ← SLA deadline
├── firstRespondedAt: ZonedDateTime      ← nullable
├── resolvedAt: ZonedDateTime            ← nullable
├── closedAt: ZonedDateTime              ← nullable
├── closedBy: Long                       ← nullable (userId or adminStaffId)
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `assign(Long agentId)` → sets OPEN → ASSIGNED → registers `TicketAssignedEvent`
- `startProgress()` → ASSIGNED → IN_PROGRESS
- `awaitCustomerResponse()` → IN_PROGRESS → PENDING_CUSTOMER_RESPONSE
- `resume()` → PENDING_CUSTOMER_RESPONSE → IN_PROGRESS (when customer replies)
- `resolve(Long agentId, String resolutionNote)` → IN_PROGRESS → RESOLVED → registers `TicketResolvedEvent`
- `close(Long closedBy)` → RESOLVED → CLOSED → registers `TicketClosedEvent`
- `reopen(Long userId, String reason)` → CLOSED → OPEN → registers `TicketReopenedEvent`
- `escalate(Long newAgentId, String escalationReason)` → changes assigned agent → registers `TicketEscalatedEvent`
- `recordFirstResponse()` → sets `firstRespondedAt` if not already set

**Domain Rules:**
- A CRITICAL ticket must be assigned within 1 hour
- Only the ticket opener (or an org admin) can close or re-open a ticket
- An agent cannot close their own ticket without marking it RESOLVED first

---

### `TicketMessage` (Entity)

```
TicketMessage
├── id: Long
├── ticketId: Long
├── authorId: String                 ← userId or adminStaffId
├── authorType: AuthorType           ← CUSTOMER | AGENT
├── message: String
├── isInternalNote: Boolean          ← true = visible only to support agents
├── attachmentUrls: List<String>     ← uploaded files, if any
└── createdAt: ZonedDateTime
```

---

### `TicketSlaConfig` (Entity — Platform-Configured)

```
TicketSlaConfig
├── id: Long
├── category: TicketCategory
├── priority: TicketPriority
├── firstResponseMinutes: Integer    ← SLA for first agent response
└── resolutionMinutes: Integer       ← SLA for full resolution
```

**Example SLAs:**
| Category | Priority | First Response | Resolution |
|---|---|---|---|
| PAYMENT_DISPUTE | CRITICAL | 30 min | 4 hours |
| PAYMENT_DISPUTE | HIGH | 2 hours | 24 hours |
| TECHNICAL | HIGH | 2 hours | 48 hours |
| GENERAL | LOW | 24 hours | 7 days |

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `TicketCreatedEvent` | Ticket opened by org | `notifications` (confirm to org, alert support team), `admin` (show in queue) |
| `TicketAssignedEvent` | Ticket assigned to agent | `notifications` (notify assigned agent via WebSocket + email) |
| `TicketMessageAddedEvent` | New message on ticket | `notifications` (notify the other party) |
| `TicketEscalatedEvent` | Ticket escalated | `notifications` (notify new agent and org) |
| `TicketResolvedEvent` | Ticket resolved | `notifications` (email org with resolution + CSAT survey link) |
| `TicketSlaBreachedEvent` | SLA deadline missed | `notifications` (alert team lead), `audit` |

---

## 4. Exceptions & Errors

**`SupportErrorCode`**:
- `TICKET_NOT_FOUND`
- `INVALID_TICKET_STATE` — trying to resolve an OPEN ticket, etc.
- `UNAUTHORIZED_TICKET_ACTION` — org user trying to perform agent action
- `TICKET_ALREADY_CLOSED`

---

## 5. Commands & Use Cases

- `OpenTicketCommand(orgId, raisedBy, category, priority, subject, description, relatedEntityType, relatedEntityId)` → `OpenTicketUseCase`
- `AddTicketMessageCommand(ticketId, authorId, authorType, message, isInternalNote, attachmentUrls)` → `AddTicketMessageUseCase`
- `AssignTicketCommand(ticketId, agentId)` → `AssignTicketUseCase`
- `StartTicketProgressCommand(ticketId)` → `StartTicketProgressUseCase`
- `ResolveTicketCommand(ticketId, agentId, resolutionNote)` → `ResolveTicketUseCase`
- `CloseTicketCommand(ticketId, closedBy)` → `CloseTicketUseCase`
- `ReopenTicketCommand(ticketId, userId, reason)` → `ReopenTicketUseCase`
- `EscalateTicketCommand(ticketId, newAgentId, reason)` → `EscalateTicketUseCase`
- `MarkSlaBreachedCommand(ticketId)` → `MarkSlaBreachedUseCase` ← called by scheduled SLA monitor

---

## 6. Queries

- `ListMyTicketsQuery(orgId, status)` → `Page<TicketSummaryResult>` ← organization view
- `GetTicketDetailsQuery(ticketId)` → `TicketDetailsResult` ← includes messages
- `ListAgentTicketsQuery(agentId, status)` → `Page<TicketSummaryResult>` ← agent view
- `ListAllTicketsQuery(status, category, priority, slaBreached)` → `Page<TicketSummaryResult>` ← admin view
- `GetSlaMetricsQuery(dateFrom, dateTo)` → `SlaMetricsResult` ← first response rate, resolution rate

---

## 7. Scheduled Jobs

- **SLA Monitor** (runs every 5 minutes): Checks all OPEN/ASSIGNED/IN_PROGRESS tickets. For each, checks if `firstResponseDeadline` or `resolutionDeadline` has passed. Calls `MarkSlaBreachedUseCase` for violators.
- **Auto-Close** (runs daily): RESOLVED tickets that haven't been re-opened or closed after 7 days are automatically CLOSED.
