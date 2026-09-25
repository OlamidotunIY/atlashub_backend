---
name: create-application-command
description: >-
  Use this skill to create Application Layer Commands and Handlers (CQRS).
---

# Create Application Command Workflow

Application Commands live in `application/commands/<CommandName>/`. Each package contains a Handler, a Command request object, and optionally a Response object.

## Pre-Requisites (Dependencies)
Before writing the handler, analyze the required dependencies:
1. **Repositories:** If the command needs a domain repository (e.g., `UserRepository`), and it doesn't exist, trigger the `create-domain-repository` skill first.
2. **Cross-Module Queries:** If the command needs data from an entity belonging to a *different* module, it MUST NOT use that module's repository. It must use a Query Port from the `shared` module. Trigger the `create-shared-query-port` skill if it doesn't exist.

## Rule 1: Scaffold Base Structure
You MUST use the provided PowerShell script to safely generate the baseline package structure and boilerplate files.
```powershell
.agents\skills\create-application-command\scripts\scaffold-command.ps1 -Module "<module_name>" -CommandName "<CommandName>" -ResponseType "<Void | EntityName>"
```
*Example: `-Module "iam" -CommandName "CreateCustomRole" -ResponseType "CustomRole"`*

**CRITICAL RESPONSE TYPE RULE:** You must carefully determine if the command needs to return a Response. 
- If the command is triggered by a REST API endpoint (especially `Create`, `Issue`, `Update` operations), it MUST return the Domain Entity itself (e.g., `CustomRole`, `ApiKey`). 
- DO NOT blindly return `Void` unless it is explicitly an async listener or background job that requires no response.

## Rule 2: Full Logic Implementation Requirement
After the scaffold script creates the baseline files, you MUST use `replace_file_content` to replace the `// TODO` comments and `return null;` placeholders in `<CommandName>Handler.java` with the **FULL, COMPLETE orchestration logic**. 
- You MUST inject the actual Repositories or Ports.
- You MUST instantiate/load entities, call their methods, and save them.
- **NEVER** leave the generated `// TODO` comments in your final code.
- **NEVER** leave `return null;` as a placeholder.

**Strict Architecture Rules:**
- **No Inline Imports:** You MUST NOT use wildcard imports (`import java.util.*`). You MUST NOT use inline fully qualified class names. Always import explicitly at the top of the file.
- **No Business Logic:** The handler MUST NOT contain business logic. Business logic belongs in Domain Entities or Domain Services/Ports. The handler should only load entities, call their business methods, and save them.
- **Entity Instantiation:** NEVER use the `new` keyword to create an entity inside a command handler. You MUST use the entity's static factory method (e.g., `CustomRole.create(...)`).
- **ID Generation:** When creating a new entity, obtain its ID by calling `nextIdentity()` on its corresponding base repository (e.g., `Long id = roleRepository.nextIdentity();`).
- **Logging:** Ensure manual logging using `org.slf4j.LoggerFactory` is retained.
- **Invariants:** Call entity methods that throw domain errors if invariants fail.
- **Component:** The handler must be annotated with Spring's `@Component`.

## Batch Processing (Multiple Commands)
This skill supports processing a list of multiple commands simultaneously.
1. You MUST process every command iteratively. Do not skip any.
2. **Execution Strategy:** Because handlers contain complex orchestration, You MUST ALWAYS use `invoke_subagent` to spawn a concurrent team of subagents when processing multiple items.


## CRITICAL: Self-Correction & Verification Before Gradle
Before you (or your dedicated subagents) run the Gradle compiler check, you MUST ALWAYS perform a strict self-review of all created and modified files. 
- Read back the files you just wrote using "cat" or "view_file".
- Check against ALL rules (e.g., absolutely NO inline imports, NO wildcard imports, NO leftover "// TODO"s, NO "return null;" placeholders).
- If ANY rule is violated, you MUST fix it immediately using "replace_file_content".
- Only after this explicit re-confirmation are you allowed to run ".\gradlew compileJava". Dedicated subagents MUST also follow this rule.

## Step 3: Gradle Compilation Check
You MUST run the Gradle compiler to prove to the user that your generated command compiles properly.
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally. You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-platform:iam:compileJava`
