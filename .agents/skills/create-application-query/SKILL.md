---
name: create-application-query
description: >-
  Use this skill to create Application Layer Queries and Handlers (CQRS read operations).
---

# Create Application Query Workflow

Application Queries live in `application/queries/<QueryName>/` (or `application/query/<QueryName>/` depending on the module's existing folder). Each package contains a Handler, a Query request object, and typically a Response object.

## Pre-Requisites (Dependencies)
Before writing the handler, analyze the required dependencies:
1. **Cross-Module Queries:** If the query needs data from an entity belonging to a *different* module, it MUST NOT use that module's repository. It must use a Query Port from the `shared` module. Trigger the `create-shared-query-port` skill if it doesn't exist.

## Rule 1: Scaffold Base Structure
You MUST use the provided PowerShell script to safely generate the baseline package structure and boilerplate files.
```powershell
.agents\skills\create-application-query\scripts\scaffold-query.ps1 -Module "<module_name>" -QueryName "<QueryName>" -ResponseType "<QueryNameResponse | PageResult<UserDto>>"
```

**CRITICAL RETURN TYPE RULE (PAGINATION):**
- You must dynamically determine if a list query should return a paginated list or a raw list based on its parameters.
- **Paginated:** If the query request explicitly includes `page` and `size` parameters, you MUST return `PageResult<T>` from `com.atlashub.shared.domain.valueobject.PageResult`.
- **Raw List:** If the query request does NOT include `page` and `size` parameters, you MUST return a standard `List<T>`. Do not return `PageResult` and do not hardcode fake pagination parameters.

## Rule 2: Full Logic Implementation Requirement
After the scaffold script creates the baseline files, you MUST use `replace_file_content` to replace the `// TODO` comments and `return null;` placeholders in `<QueryName>Handler.java` with the **FULL, COMPLETE orchestration logic**. 
- You MUST inject the actual Repositories or Ports.
- You MUST fetch the necessary data.
- You MUST manually map the Entities to the Result records and return them.
- **NEVER** leave the generated `// TODO` comments in your final code.
- **NEVER** leave `return null;` as a placeholder.

**Strict Architecture Rules:**
- **No Inline Imports:** You MUST NOT use wildcard imports (`import java.util.*`). You MUST NOT use inline fully qualified class names. Always import explicitly at the top of the file.
- **No Business Logic:** The handler MUST NOT contain domain business logic. It should only fetch data and map it to DTOs/Results.
- **Logging:** Ensure manual logging using `org.slf4j.LoggerFactory` is retained.
- **Component:** The handler must be annotated with Spring's `@Component`.

## Batch Processing (Multiple Queries)
This skill supports processing a list of multiple queries simultaneously.
1. You MUST process every query iteratively. Do not skip any.
2. **Execution Strategy:** Because handlers contain complex orchestration, processing many sequentially in one turn can overwhelm context limits. You MUST ALWAYS use `invoke_subagent` to spawn a concurrent team of subagents when processing multiple items.


## CRITICAL: Self-Correction & Verification Before Gradle
Before you (or your dedicated subagents) run the Gradle compiler check, you MUST ALWAYS perform a strict self-review of all created and modified files. 
- Read back the files you just wrote using "cat" or "view_file".
- Check against ALL rules (e.g., absolutely NO inline imports, NO wildcard imports, NO leftover "// TODO"s, NO "return null;" placeholders).
- If ANY rule is violated, you MUST fix it immediately using "replace_file_content".
- Only after this explicit re-confirmation are you allowed to run ".\gradlew compileJava". Dedicated subagents MUST also follow this rule.

## Step 3: Gradle Compilation Check
You MUST run the Gradle compiler to prove to the user that your generated query compiles properly.
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally for speed. You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-platform:iam:compileJava`
