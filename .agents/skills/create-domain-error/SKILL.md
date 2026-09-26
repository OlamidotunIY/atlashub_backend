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


## Subagent Separation of Concerns (Vertical Slicing)
When using invoke_subagent to process multiple items, you MUST adhere to strict Separation of Concerns (SoC) via **Vertical Slicing**:
1. **One Subagent per Item**: Assign each subagent exactly ONE item (e.g., one entity, one command, one mapper).
2. **End-to-End Flow**: The subagent is responsible for checking its own pre-requisites. If any dependencies (e.g., Value Objects, Events, Entities, Mappers) are missing, the subagent MUST execute the instructions of those respective skills to generate them before proceeding.
3. **Independent Verification**: The subagent MUST run its own verification (e.g., .\gradlew compileJava for the module) to ensure its specific slice is perfect.
4. **Independent Commit**: Once verified, the subagent MUST commit its own changes to Git and end its turn. Do not wait for a parent agent to commit.

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



## CRITICAL: Self-Correction & Verification Before Gradle
Before you (or your dedicated subagents) run the Gradle compiler check, you MUST ALWAYS perform a strict self-review of all created and modified files. 
- Read back the files you just wrote using `cat` or \iew_file\.
- Check against ALL rules (e.g., absolutely NO inline imports, NO wildcard imports, NO leftover `// TODO`s, NO \eturn null;\ placeholders).
- If ANY rule is violated, you MUST fix it immediately using \eplace_file_content\.
- Only after this explicit re-confirmation are you allowed to run \.\gradlew compileJava\. Dedicated subagents MUST also follow this rule.

## Final Step: Git Commit & Push
Verification is NOT the final step; committing your work is.
After your code successfully compiles and passes all verification rules, you (and every individual subagent) MUST commit and push your changes to GitHub.
1. Stage your specific files: "git add <paths_to_your_files>"
2. Commit your changes using standard Conventional Commits formatting (e.g., "feat(<module>): add <feature>", "refactor(<module>): ...").
3. Push to the remote repository: "git push origin HEAD"
**CRITICAL:** If you are a subagent, you MUST commit and push your own specific work independently as soon as it passes compilation. Do not wait for the parent agent.
