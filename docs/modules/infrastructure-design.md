# Infrastructure Module Design (`atlashub-infrastructure`)

This module houses the cross-cutting technical concerns required by the platform and products.

## 1. `eventbus` Submodule
Handles all asynchronous messaging (Outbox & Inbox patterns).

**Entities**:
- `OutboxMessage`: `id`, `topic`, `payload`, `status` (PENDING, PROCESSED), `createdAt`, `processedAt`.
- `EventDeliveryTracker` (Inbox): `eventId`, `consumerId`, `status` (PENDING, SUCCESS, DLQ).

**Processes**:
- `OutboxPollingScheduler`: Periodically polls `OutboxMessage` where status is `PENDING`, pushes to Kafka/RabbitMQ, and updates to `PROCESSED`.
- `EventTrackerApi`: API used by all consumers to guarantee idempotency via the Inbox pattern.

## 2. `notifications` Submodule
Handles dispatching Email, SMS, and In-App Push notifications based on Domain Events.

**Listeners (Examples)**:
- `AuthEventListener`: Listens to `AuthNewDeviceLoginEvent`, `AuthVerificationCreatedEvent` to send security and OTP emails.
- `IdentityEventListener`: Listens to `InvitationCreated` to send "You've been invited" emails.
- `CommerceEventListener`: Listens to `SalesOrderCompleted` to send receipts.

**Adapters**:
- `SmtpEmailSenderAdapter`: Integrates with SMTP providers (SendGrid/SES).
- HTML Templates (Thymeleaf/Freemarker) for dynamic email generation.

## 3. `audit` Submodule
Maintains immutable records of user actions and critical system state changes.

**Entities**:
- `AuditLog`: `id`, `userId`, `organizationId`, `action`, `resourceId`, `resourceType`, `previousState`, `newState`, `ipAddress`, `timestamp`.

**Listeners**:
- Subscribes to critical domain events (e.g., `EmployeeTerminatedEvent`, `StockAdjustedEvent`) across the entire monolith and logs them for compliance.

## 4. `rate-limiter` Submodule
Prevents abuse of public APIs (Authentication, Webhooks).
- Backed by Redis.
- Configurable limits per `OrganizationId` or `IP Address`.
