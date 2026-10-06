---
name: audit-module
description: Audit an AtlasHub backend module end-to-end against its current docs, dependency boundaries, runtime wiring, incomplete artifacts, tests, and obsolete files.
---

# Audit an AtlasHub module

Load `atlashub-module-workflow`, resolve artifact `audit`, and read every returned doc. Also read any provider setup docs referenced by the module design.

Audit in this order:

1. Build a docs-to-code matrix for entities, value objects, exceptions, events, commands, queries, ports, adapters, listeners, controllers, and tests.
2. Separate `documented but missing`, `implemented but undocumented`, `contract mismatch`, `broken`, and `obsolete/unreferenced`. Do not label intentional corrected code as wrong solely because older docs differ.
3. Verify cross-module reads use shared application ports and writes use events.
4. Verify every port has one intended runtime implementation and that the executable module depends on its owning Gradle module.
5. Verify listener/scheduler command-only dependencies, per-resource controllers, DTO placement, principal-derived context, and `ApiResponse<T>` responses.
6. Search for unused replacement artifacts and stale methods after model changes.
7. Run the architecture validator with `-AllFiles`, targeted compile/tests, and an application-context/startup check when possible.

An audit request alone is read-only. Report findings with file evidence and severity; change code only when the user also asks for fixes.
