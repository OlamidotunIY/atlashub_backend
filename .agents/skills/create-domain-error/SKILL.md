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

## Generation Mode (Creating New)
Do not write the file manually. You MUST use the PowerShell generator script:

```powershell
.agents\skills\create-domain-error\scripts\generate-error.ps1 -Module "<module>" -ErrorName "<ErrorName>" -BaseException "<BaseException>"
```
*Example: `...generate-error.ps1 -Module "iam" -ErrorName "EmailAlreadyVerified" -BaseException "ConflictException"`*

## Step 3: Gradle Verification
Run `.\gradlew :<module_gradle_path>:compileJava` to verify it compiles.
