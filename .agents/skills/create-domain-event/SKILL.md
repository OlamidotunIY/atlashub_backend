---
name: create-domain-event
description: >-
  Use this skill whenever the user asks to "create domain events" or "create a domain event".
  The user will provide the event name, its trigger/purpose, and the target module name.
---

# Create Domain Event

You are responsible for generating Domain Event records in the Atlashub backend codebase according to strict Domain-Driven Design (DDD) guidelines.

## Inputs Required from User
If the user does not provide all of these, ask for the missing ones:
1. **Event Name** (e.g., `MemberJoinedEvent`)
2. **Trigger / Consumers** (e.g., "Invitation accepted, used by notifications and hr")
3. **Module Name** (e.g., `iam`, `accounts`)

## Step 1: Deep Domain Analysis (Payload Discovery)
Before writing any code, you MUST figure out the optimal payload by analyzing the domain:
1. **Read the Sender Entity:** Use your search tools to find and read the sender's Domain Entity in the codebase (e.g., `Invitation.java` or `OrganizationMember.java`).
2. **Read the Receiver Entity/Docs:** Identify the receiver modules (e.g., `notifications`, `hr`). Use your search tools to find and read the receiver Domain Entities (e.g., `Employee.java`). If the receiver module is not yet implemented in code, you MUST search the project's markdown documentation to find the planned entity fields.
3. **Determine the Payload:** Based on what the sender has and what the receiver needs, deduce the exact fields required for the `Payload` record.
4. **CRITICAL PAYLOAD RULE:** Do **NOT** include the ID of the sender/aggregate (e.g., `invitationId`, `memberId`) inside the `Payload`. The standard `aggregateId` field on the event already holds this value.

## Step 2: File Location Resolution
Do not assume the root folder. Use your search tools (like `find_by_name`) to locate the root directory matching the `<module>` name provided by the user.
Once you find the module's root directory, the file path must strictly be:
`<FoundModulePath>/src/main/java/com/atlashub/<module>/domain/events/<EventName>.java`

## Step 3: Code Generation
You must strictly follow this exact boilerplate structure. **Do not use wildcard imports.**

```java
package com.atlashub.<module>.domain.events;

import com.atlashub.shared.domain.events.DomainEvent;
import java.time.ZonedDateTime;

public record <EventName>(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<<EventName>.Payload> {

    public record Payload(
        // Inject the deeply analyzed fields here (EXCLUDING the aggregateId)
    ) {
    }
}
```

## Step 4: Verification
Confirm to the user that the file was created, provide the clickable path, and briefly explain how you deduced the payload fields based on your domain analysis.
