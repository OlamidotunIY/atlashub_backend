---
name: create-scheduler
description: Create or audit an AtlasHub scheduled infrastructure trigger that invokes a command handler and contains no business or persistence logic.
---

# Create scheduler

Load `atlashub-module-workflow`, resolve artifact `scheduler`, and read scheduling docs and the target command. Invoke `create-application-command` first if absent.

- Schedulers always live in `infrastructure/messaging/schedulers`. Never create a top-level `infrastructure/schedulers` package.
- Scheduler is a thin Spring component: log safe run metadata, construct the documented command, call the handler.
- It never injects repositories, domain services, query ports, caches, or external clients.
- Cron/time zone/property configuration comes from configuration, not unexplained literals, unless docs explicitly fix it.
- Define concurrency/idempotency behavior for multi-instance deployment; the command remains safe on retries.
- Do not add security annotations requiring a user session.

Test command invocation and run validator/module tests. Application startup must confirm scheduling and bean wiring.
