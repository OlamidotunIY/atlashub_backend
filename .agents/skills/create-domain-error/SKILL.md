---
name: create-domain-error
description: >-
  Use this skill whenever you need to create a custom Domain Exception/Error, OR to audit/check an existing one.
---

# Create Domain Error

You are responsible for generating custom Domain Exceptions extending core shared exceptions.

## Audit / Update Mode
If the user asks you to "check", "verify", or "update" an existing Error:
1. Read the existing file using your tools.
2. Verify it extends a valid Base Exception (`ConflictException`, `BusinessRuleException`, `NotFoundException`, `ValidationException`, or `AuthorizationException`).
3. Verify it has the standard message and cause constructors.
4. If it violates rules, use `replace_file_content` to fix it. If perfect, tell the user "Everything is structurally perfect" and end.

## Batch Processing (Multiple Errors)
This skill supports processing a list or table of multiple errors simultaneously for BOTH Generation Mode and Audit/Update Mode.
1. You MUST process every error iteratively. Do not skip any.
2. **Execution Strategy:** You can either process them sequentially in a single turn (best for small batches) OR use `invoke_subagent` to spawn a concurrent team of subagents to process them simultaneously (best for large lists).

## Generation Mode (Creating New)
Do not write the file manually. You MUST use the PowerShell generator script:

```powershell
.agents\skills\create-domain-error\scripts\generate-error.ps1 -Module "<module>" -ErrorName "<ErrorName>" -BaseException "<BaseException>"
```
*Example: `...generate-error.ps1 -Module "iam" -ErrorName "EmailAlreadyVerified" -BaseException "ConflictException"`*

## Step 3: Gradle Verification
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally, as it will compile the entire app.
You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-platform:iam:compileJava`
