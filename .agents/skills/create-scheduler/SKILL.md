---
name: create-scheduler
description: >-
  Use this skill to create a Spring @Scheduled infrastructure scheduler that delegates execution to a void Command Handler. Never put business logic inside the scheduler itself.
---

# Create Scheduler

## Overview
Schedulers live in `infrastructure/schedulers/` and are thin `@Component` classes with a `@Scheduled(cron = "...")` method. Their sole responsibility is to trigger a Command Handler at the configured interval. All business logic lives in the handler.

## Rules
- **No business logic** in the scheduler class itself.
- The scheduler MUST inject and call a Command Handler via constructor injection.
- The handler's command record MUST already exist (or be created via `create-application-command` first).
- The scheduler MUST log at `log.info` level when it fires.
- The `@Scheduled` annotation MUST use `cron` expression (not `fixedRate` or `fixedDelay`) unless explicitly instructed otherwise.

## Pre-Requisites
1. The Command Handler being called must already exist. If not, trigger `create-application-command` first.
2. Confirm the cron expression (e.g., `0 0 * * * *` for hourly).

## Generation Mode

**Step 1: Scaffold skeleton**
```powershell
.\.agents\skills\create-scheduler\scripts\scaffold-scheduler.ps1 `
    -Module "<module>" `
    -SchedulerName "<SchedulerName>" `
    -CronExpression "<cron>" `
    -HandlerName "<HandlerName>"
```

Example:
```powershell
.\.agents\skills\create-scheduler\scripts\scaffold-scheduler.ps1 `
    -Module "billing" `
    -SchedulerName "InvoiceGeneration" `
    -CronExpression "0 0 1 * * *" `
    -HandlerName "GenerateInvoiceHandler"
```

Creates: `infrastructure/schedulers/InvoiceGenerationScheduler.java`

**Step 2: Verify & adjust**
Use `replace_file_content` to:
1. Add the correct import for the Handler class.
2. Pass the correct `new <CommandName>Command(...)` arguments if the command record takes parameters.

## CRITICAL: Self-Correction & Verification Before Gradle
- No wildcard imports
- `@Scheduled` cron value is correct
- Handler is constructor-injected (not field-injected)
- No business logic in scheduler

## Step 3: Gradle Verification
```
.\gradlew :atlashub-platform:<module>:compileJava
```

## Final Step: Git Commit & Push
```
git add <paths>
git commit -m "feat(<module>): add <SchedulerName> scheduled job"
git push origin HEAD
```
