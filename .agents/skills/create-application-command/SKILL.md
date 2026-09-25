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

## Rule 1: Scaffold Skeleton or Update Existing
Check if the command package (`application/commands/<CommandName>`) already exists. 
- **If it exists:** Do NOT run the scaffold script. Do NOT recreate the package. Proceed directly to updating the existing files.
- **If it does not exist:** Use the provided PowerShell script to safely generate the package and files.
Pass `Void` for the ResponseType if the command does not return data. The skill should intelligently decide whether it needs to return a custom Response record (e.g., for API endpoints) or Void (e.g., for async listeners).
```powershell
.agents\skills\create-application-command\scripts\scaffold-command.ps1 -Module "<module_name>" -CommandName "<CommandName>" -ResponseType "<Void | CommandNameResponse>"
```
*Example: `-Module "iam" -CommandName "AuthAccount" -ResponseType "Void"`*

## Rule 2: Inject Handler Logic
Use `replace_file_content` to inject the dependencies and orchestration logic into the generated `<CommandName>Handler.java` file.

**Strict Architecture Rules:**
- **No Inline Imports:** You MUST NOT use wildcard imports (`import java.util.*`). You MUST NOT use inline fully qualified class names inside the code (e.g., `java.util.List<String>`). Always import explicitly at the top of the file.
- **No Business Logic:** The handler MUST NOT contain business logic. Business logic belongs in Domain Entities or Domain Services/Ports. The handler should only load entities, call their business methods, and save them.
- **Logging:** Ensure manual logging using `org.slf4j.LoggerFactory` is retained (do not use `@Slf4j`).
- **Invariants:** Call entity methods that throw domain errors if invariants fail.
- **Component:** The handler must be annotated with Spring's `@Component`.

## Batch Processing (Multiple Commands)
This skill supports processing a list of multiple commands simultaneously.
1. You MUST process every command iteratively. Do not skip any.
2. **Execution Strategy:** Because handlers contain complex orchestration, you are strongly encouraged to use `invoke_subagent` to spawn a concurrent team of subagents to process them simultaneously to avoid context limits.

## Step 3: Gradle Compilation Check
You MUST run the Gradle compiler to prove to the user that your generated command compiles properly.
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally. You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-platform:iam:compileJava`
