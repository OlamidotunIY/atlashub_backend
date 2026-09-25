---
name: create-domain-entity
description: >-
  Use this skill to create, audit, check, or update Domain Entities / Aggregate Roots.
---

# Create Domain Entity

## Audit / Update Mode
If the user asks you to "check", "verify", or "update" an existing Entity:
1. Read the existing entity file using your tools.
2. Verify it meets ALL rules below (e.g., ID fields are final, `touch()` exists and is called, invariants are checked, events are registered).
3. If it perfectly matches, tell the user "Everything is structurally perfect" and do nothing.
4. If it violates ANY rules, do not recreate it. Use the `replace_file_content` tool to safely inject the missing pieces. Then compile it via Gradle.

## Batch Processing (Multiple Entities)
This skill supports processing a list or table of multiple entities simultaneously for BOTH Generation Mode and Audit/Update Mode.
1. You MUST process every entity iteratively. Do not skip any.
2. **Execution Strategy:** Because Domain Entities contain complex business logic, processing many sequentially in one turn can overwhelm context limits. You are strongly encouraged to use `invoke_subagent` to spawn a concurrent team of subagents to process or audit them simultaneously, isolating the context for each entity.

## Pre-Requisites (Events Discovery & Errors)
Before generating or updating an entity, analyze its Domain Rules and Business Methods.

**Handling Domain Events:**
1. **Check Existing:** Check the module's `domain/events` folder. If events already exist that perfectly fit the entity and its business methods, you will simply register them later.
2. **Check Docs:** If events are missing, do not match the entity, or do not cover all business methods, read the module's markdown documentation (specifically the "4. Domain Events" section/table). Use the "Published When" and "Consumed By" details from that table to trigger the `create-domain-event` skill to generate the missing events.
3. **Proactive Creation:** If you personally identify a valid business use case that *should* publish an event, but it is missing from the docs, you are empowered to proactively create that event using the `create-domain-event` skill.

**Handling Domain Errors:**
- If a method implies throwing a custom domain error (e.g., "deactivating the last owner throws LastOwnerDeactivationException"), you MUST first use the **`create-domain-error`** skill to generate it.

**Handling Value Objects:**
- If a field is a custom complex type or enum (e.g., `MemberStatus`), first check if it exists in the `shared` module.
- If it does not exist globally or locally, you MUST use the **`create-domain-valueobject`** skill to generate it before writing the entity.

## Generation Mode (Creating New)
**Step 1: Scaffold Skeleton**
Run the PowerShell script to safely generate the baseline file structure and prevent accidental overwrites:
```powershell
.agents\skills\create-domain-entity\scripts\scaffold-entity.ps1 -Module "<module>" -EntityName "<EntityName>" -IsAggregateRoot $<true/false>
```

**Step 2: Inject Business Logic**
Once the skeleton is scaffolded, use `replace_file_content` to inject the fields, constructor, `create` method, and business mutators into the file, adhering to these rules:

1. **Final Fields:** ID fields (`id`, `organizationId`) MUST be `final Long`.
2. **Static Factory Method (`create`):**
   - Accept ONLY fields the system cannot deduce.
   - Do NOT accept `createdAt`, `updatedAt`, or default statuses. Set them internally.
3. **Mutator Methods & `touch()`:**
   - Any method that updates state MUST call `this.touch();`.
   - Implement `private void touch() { this.updatedAt = ZonedDateTime.now(); }`.
4. **Events & Errors:**
   - Enforce all requested rules inside the mutator methods. Throw specific Domain Errors if violated (create them with `create-domain-error` if missing).
   - Use `this.registerEvent(...)` with `CorrelationId.getOrCreate()` for domain events. (Create events with `create-domain-event` first if missing).

## Step 3: Gradle Verification
Run `.\gradlew :<module_gradle_path>:compileJava` to verify it compiles perfectly without missing imports.
If errors occur, fix them immediately before answering the user.
