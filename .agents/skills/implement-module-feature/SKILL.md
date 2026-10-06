---
name: implement-module-feature
description: Implement or complete an AtlasHub backend feature end-to-end across domain, application, infrastructure, messaging, presentation, and tests without leaving missing dependencies.
---

# Implement an AtlasHub module feature

First load and execute `atlashub-module-workflow`. Run its context resolver and read every required document completely. Then inspect the current code for the target module and list the requested slice's existing and missing artifacts.

Use the dependency graph in `../atlashub-module-workflow/references/dependency-routing.md`. Invoke each required artifact skill in dependency order. A feature is incomplete if any documented command, query, event, port implementation, persistence adapter, listener, endpoint, or meaningful test is missing.

## Required sequence

1. Translate the docs and explicit user decisions into acceptance criteria. Never invent provider-facing public contracts.
2. Model or update value objects, semantic domain exceptions, events, entities, and domain services.
3. Add same-module repository contracts and cross-boundary ports. Reads across modules use shared query ports; writes use events.
4. Implement commands and queries. Commands orchestrate writes; queries are read-only.
5. Complete persistence and external adapters. Do not leave an interface without its owning implementation and Spring bean registration when runtime injection requires it.
6. Add command-only listeners/schedulers and per-entity REST presentation with DTOs.
7. Delete artifacts made obsolete by the new design and prove they have no callers.
8. Add tests, run `Test-AtlashubArchitecture.ps1`, compile/test the exact Gradle module, and perform context/startup verification when Spring wiring changed.

Stop and report a docs/code conflict that would change business behavior. Do not silently choose old docs over corrected code or vice versa.
