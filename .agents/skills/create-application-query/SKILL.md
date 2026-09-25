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

## Rule 1: Scaffold Skeleton
Use the provided PowerShell script to safely generate the package and files.
```powershell
.agents\skills\create-application-query\scripts\scaffold-query.ps1 -Module "<module_name>" -QueryName "<QueryName>" -ResponseType "<QueryNameResponse | List<UserDto>>"
```

## Rule 2: Inject Handler Logic
Use `replace_file_content` to inject the dependencies and read logic into the generated `<QueryName>Handler.java` file.

**Strict Architecture Rules:**
- **No Inline Imports:** You MUST NOT use wildcard imports (`import java.util.*`). You MUST NOT use inline fully qualified class names inside the code (e.g., `java.util.List<String>`). Always import explicitly at the top of the file.
- **No Business Logic:** The handler MUST NOT contain business logic. 
- **Logging:** Ensure manual logging using `org.slf4j.LoggerFactory` is retained (do not use `@Slf4j`).
- **Component:** The handler must be annotated with Spring's `@Component`.

## Batch Processing (Multiple Queries)
This skill supports processing a list of multiple queries simultaneously.
1. You MUST process every query iteratively. Do not skip any.
2. **Execution Strategy:** Because handlers contain complex orchestration, processing many sequentially in one turn can overwhelm context limits. You are strongly encouraged to use `invoke_subagent` to spawn a concurrent team of subagents to process them simultaneously.

## Step 3: Gradle Compilation Check
You MUST run the Gradle compiler to prove to the user that your generated query compiles properly.
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally for speed. You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-platform:iam:compileJava`
