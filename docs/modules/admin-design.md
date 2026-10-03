# Admin Module Design (`atlashub-platform:admin`)

## Status and boundary

This module is not active in `settings.gradle`; implementation work is deferred. This document is the current contract.

Admin owns AtlasHub staff identities, operations queues, support notes, organization bans, and platform reporting. It does not own or decide organization compliance.

Anchor performs banking/KYB verification. `platform:compliance` verifies Anchor webhooks, applies the external fact to AtlasHub's own compliance aggregate, and publishes AtlasHub compliance events. No admin endpoint, command, permission, or event can approve or reject compliance.

## Staff roles

| Role | Capabilities |
|---|---|
| `SUPER_ADMIN` | Staff management, organization ban/unban, full operations visibility |
| `COMPLIANCE_OPERATIONS` | Monitor compliance/provider state, inspect missing requirements, create and resolve escalations |
| `SUPPORT_AGENT` | Organization lookup, support notes, escalation |
| `FINANCE_OPERATIONS` | Reconciliation and platform financial reports |

## Aggregates

### `AdminStaff`

Owns staff identity, status, role, MFA requirement, and audit fields. Staff actions always record actor and correlation ID.

### `ComplianceOperationsTask`

An observation/escalation record created from compliance action-required/provider-error events.

```text
id, organizationId, complianceRecordId?
provider?, environment?
type: ACTION_REQUIRED | PROVIDER_ERROR | RECONCILIATION | MANUAL_ESCALATION
status: OPEN | ASSIGNED | WAITING_EXTERNAL | RESOLVED
assignedTo?, notes, resolutionNotes?
createdAt, updatedAt, resolvedAt?
```

Resolving this task never changes `ComplianceRecord`. Additional-information requirements originate from compliance/provider facts; admin may annotate or escalate them only.

### `OrganizationRestriction`

Records an authorized platform ban/unban with reason, actor, timestamps, and current state. This is separate from compliance status.

## Commands

- `CreateComplianceOperationsTaskCommand`
- `AssignComplianceOperationsTaskCommand`
- `EscalateComplianceOperationsTaskCommand`
- `ResolveComplianceOperationsTaskCommand`
- `BanOrganizationCommand`
- `UnbanOrganizationCommand`
- staff lifecycle commands

Listeners invoke commands only and never repositories. Cross-module writes are asynchronous events.

There are deliberately no `ApproveKycCommand`, `RejectKycCommand`, `KycApprovedEvent`, or `KycRejectedEvent` artifacts.

## Events

| Event | Consumers |
|---|---|
| `ComplianceOperationsEscalatedEvent` | Operations, notifications |
| `ComplianceOperationsResolvedEvent` | Operations |
| `OrganizationBannedEvent` | auth, pay accounts, pay ledger, billing, notifications |
| `OrganizationUnbannedEvent` | auth, pay accounts, pay ledger, billing, notifications |

An organization ban adds only the `ORGANIZATION_BAN` restriction in consumers. Unban removes only that restriction; compliance/risk/manual restrictions remain effective.

## Read boundaries

Admin may synchronously read compliance/account summaries through shared query ports implemented by the owning module under `infrastructure.persistence.adapters`. It cannot import another module's repository or JPA entity.

## API

Controllers are per resource and take the staff principal from security context:

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/admin/compliance-operations` | List/filter operations tasks |
| `POST` | `/api/v1/admin/compliance-operations/{id}/assign` | Assign task |
| `POST` | `/api/v1/admin/compliance-operations/{id}/escalate` | Escalate task |
| `POST` | `/api/v1/admin/compliance-operations/{id}/resolve` | Resolve operations task only |
| `POST` | `/api/v1/admin/organizations/{id}/ban` | Apply organization ban |
| `POST` | `/api/v1/admin/organizations/{id}/unban` | Remove organization-ban restriction |

No compliance approval/rejection endpoint exists.

## Persistence and audit

- All mutations use optimistic locking and transactional outbox events.
- Staff actions retain actor, reason, correlation ID, and timestamps.
- Provider payloads and secrets are not copied into admin records.
- Sensitive compliance data is read through redacted views and audited.
- Operations tasks are idempotent by source event ID/type.

## Activation requirements

Before adding the module to `settings.gradle`, implement staff security/MFA, per-resource controllers, shared read-port adapters in owning modules, operations task commands/listeners, ban/unban events, audit tests, and explicit architecture tests proving the admin module cannot transition compliance.
