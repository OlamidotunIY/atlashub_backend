---
name: create-domain-event
description: >-
  Use this skill whenever the user asks to "create domain events" or "create a domain event".
  The user will provide the event name, its trigger/purpose, and the target module name.
---

# Create Domain Event Workflow

You are responsible for generating Domain Event records in the Atlashub backend codebase according to strict Domain-Driven Design (DDD) guidelines.
To eliminate human/AI error and ensure perfectly structured, compliant Java files, you MUST use the provided PowerShell helper script instead of writing the code manually.

## Inputs Required from User
If the user does not provide these, ask for the missing information:
1. **Event Name(s)** (e.g., `MemberJoinedEvent`)
2. **Trigger / Consumers** (e.g., "Invitation accepted, used by notifications and hr")
3. **Module Name** (e.g., `iam`, `accounts`)

## Batch Processing (Multiple Events)
If the user pastes a list or table of multiple events at once:
1. Deduce the target module from context, or ask the user if it's missing.
2. You MUST process every event iteratively. Do not skip any.
3. For **every single event** in the list, independently perform **Step 1 (Deep Domain Analysis)** to deduce its unique payload.
4. You can execute the PowerShell script multiple times in a single turn to generate all the files at once.

## Step 1: Deep Domain Analysis (Payload Discovery)
Before writing any code, you MUST figure out the optimal payload by analyzing the domain:
1. **Read the Sender Entity:** Use your search tools to find and read the sender's Domain Entity in the codebase (e.g., `Invitation.java` or `OrganizationMember.java`).
2. **Read the Receiver Entity/Docs:** Identify the receiver modules (e.g., `notifications`, `hr`). Use your search tools to find and read the receiver Domain Entities (e.g., `Employee.java`). If the receiver module is not yet implemented in code, search the project's markdown documentation to find the planned entity fields.
3. **Determine the Payload:** Based on what the sender has and what the receiver needs, deduce the exact fields required for the `Payload` record. Format them as a comma-separated string of Java declarations (e.g., `"Long organizationId, Long userId, String email, Long customRoleId"`).
4. **CRITICAL PAYLOAD RULE:** Do **NOT** include the ID of the sender/aggregate (e.g., `invitationId`, `memberId`) inside the `Payload`. The standard `aggregateId` field on the event already holds this value.

## Step 2: Code Generation (via Script)
Do NOT use `write_to_file` to write the Java code manually. 
Instead, execute the provided helper script. The script automatically searches for the module path, prevents overwrites, bans wildcard imports, and formats the Java record perfectly.

**Command:**
```powershell
.agents\skills\create-domain-event\scripts\generate-event.ps1 -Module "<module_name>" -EventName "<event_name>" -PayloadFields "<deduced_payload_fields>"
```

*Example Execution:*
```powershell
.agents\skills\create-domain-event\scripts\generate-event.ps1 -Module "iam" -EventName "MemberJoinedEvent" -PayloadFields "Long organizationId, Long userId, String email, Long customRoleId"
```

## Step 3: Gradle Compilation Check
After the script finishes generating the files, you MUST run the Gradle compiler to prove to the user that your generated events have zero syntax or import errors.
1. Determine the Gradle module path (e.g., if the module is in `atlashub-platform/iam`, the Gradle path is `:atlashub-platform:iam`).
2. Run the compilation command: `.\gradlew :<gradle_path>:compileJava`
3. If the build fails due to the files you just generated, you must fix the errors immediately.

## Step 4: Final Verification
Confirm to the user that the file was created. Show them the successful output of the Gradle build to prove it compiled flawlessly.
