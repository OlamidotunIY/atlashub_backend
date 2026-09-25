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
