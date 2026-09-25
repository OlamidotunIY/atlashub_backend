---
name: create-domain-entity
description: >-
  Use this skill to create Domain Entities or Aggregate Roots, complete with invariants, domain events, errors, and strict DDD rules.
---

# Create Domain Entity

You are responsible for generating complete, robust Domain Entities according to strict DDD architecture.

## Inputs Required
1. **Entity Name & Type** (e.g., `OrganizationMember (Aggregate Root)`)
2. **Fields & Business Methods** (User will provide a markdown list of fields and methods)
3. **Domain Rules** (Invariants that must be enforced)
4. **Module Name** (e.g., `iam`)

## Step 1: Pre-Requisites (Events & Errors)
Before generating the entity, analyze the requested Domain Rules and Business Methods.
- If a method implies publishing a domain event (e.g., "accepting an invite throws MemberJoinedEvent"), you MUST first use the **`create-domain-event`** skill to generate it.
- If a method implies throwing a custom domain error (e.g., "deactivating the last owner throws LastOwnerDeactivationException"), you MUST first use the **`create-domain-error`** skill to generate it.

## Step 2: Code Generation Rules
You must build the entity strictly following these rules:

1. **Path:** `<ModuleRoot>/src/main/java/com/atlashub/<module>/domain/entities/<EntityName>.java`
2. **Aggregate Root:** If the user specifies it is an Aggregate Root, it must `extends AggregateRoot<Long>`. You must override `public Long getId()`. (Import `com.atlashub.shared.domain.entities.AggregateRoot`).
3. **Final Fields:**
   - ID fields (e.g., `id`, `organizationId`) MUST be `final Long`.
   - Any field that is strictly set at creation and never updated MUST be `final`.
4. **Constructors:**
   - You must create a full constructor taking ALL fields.
5. **Static Factory Method (`create`):**
   - Must have a `public static <Entity> create(...)` method.
   - It should ONLY accept fields the system cannot deduce itself.
   - Do NOT accept `createdAt` (set it to `ZonedDateTime.now()`), `updatedAt` (set it to `now()`), or default statuses (e.g., `Status.PENDING`). Inject them automatically inside the `create` method.
6. **Mutator Methods & `touch()`:**
   - Any method that updates state MUST call `this.touch();`.
   - You must implement: `private void touch() { this.updatedAt = ZonedDateTime.now(); }` if the entity has an `updatedAt` field.
7. **Business Logic & Invariants:**
   - Enforce all requested rules inside the mutator methods.
   - If a rule fails, throw the specific Domain Error you created in Step 1.
   - If 2 or more methods check the exact same invariant, extract it into a private helper method (e.g., `private boolean isLocked()`).
8. **Event Registration:**
   - When a business action is successful and requires an event, use `this.registerEvent(...)`.
   - Pass `CorrelationId.getOrCreate()` into the event's correlation field. (Import `com.atlashub.shared.domain.valueobject.CorrelationId`).

## Step 3: Gradle Verification
Run `.\gradlew :<module_gradle_path>:compileJava` to verify it compiles perfectly without missing imports.
