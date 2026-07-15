# cloud-itonami-isic-0116

Open Occupation Blueprint for **ISIC Rev. 4 0116**: Growing of fibre crops.

This repository implements a forkable OSS **fibre-crop-growing operations
coordinator**: a field-management and record-keeping robot manages planting/
fibre-yield/quality-grade logging, field-operation (planting/defoliation/
retting/harvest) scheduling, and supply procurement under a governor-gated
actor, so a fibre-crop farm (cotton, jute, flax, hemp, sisal) keeps its own
operational records and maintains full transparency over decisions.

**Maturity: `:implemented`.** `src/fibreops/` implements the
`FibreOpsAdvisor` (`fibreops.advisor`) and the independent
`FieldOperationsGovernor` (`fibreops.governor`), composed by
`fibreops.operation` following the itonami actor pattern (ADR-2607011000):
`advise -> govern -> phase-gate -> commit | escalate | hold`. 35 tests /
120 assertions green (`clojure -M:test`).

`fibreops.operation` is a synchronous stub of this flow (see its
docstring) — production wiring into a `langgraph-clj` StateGraph with
`interrupt-before`/checkpoint-based human-in-the-loop resume for escalated
operations is deferred, mirroring `cloud-itonami-isic-0111`'s own
`cerealops.operation`.

## What this does NOT do

This actor coordinates **back-office logistics only**. It explicitly does **NOT**:

- **Direct field-equipment operation** — remains the farmer's exclusive authority
- **Pesticide-application decisions** — remains the agronomist/farmer authority
- **Agronomic decision authority** (what/when/how much to plant, defoliate,
  ret, or harvest) — remains human authority; this actor only coordinates the
  logistics around those decisions
- **Direct execution of any kind** — any proposal for direct field-equipment
  control or finalizing a pesticide-application decision is a hard block

## HARD invariants (always hold, never overridable)

1. **field-not-registered** — the request's `field-id` must resolve to a
   registered field in the Store before any proposal can proceed
2. **no-execution** — every proposal's `:effect` must be `:propose` (the governor
   never directly operates field equipment, never finalizes a
   pesticide-application decision)
3. **equipment-or-pesticide-decision-blocked** — `:operate-field-equipment` and
   `:finalize-pesticide-application` proposals are unconditionally, permanently
   blocked
4. **op-not-allowed** — any op outside the closed allowlist below is rejected
5. **field-record-invalid** — `:log-field-record` with a non-positive acreage is
   rejected
6. **quality-grade-invalid** — `:log-field-record` with a fibre-quality grade
   code outside the actor's recognized closed vocabulary
   (`fibreops.facts/fibre-quality-grades`) is rejected

## Always-escalate operations (human sign-off, regardless of confidence)

- `:flag-crop-health-concern` — any pest (e.g. boll weevil)/disease/
  drought-stress concern → automatic escalation
- `:order-supplies` over its category cost threshold (default 500 currency
  units; see `fibreops.facts/supply-categories`)
- Any proposal with confidence below the Governor's floor (0.7)

## Operational requests (closed allowlist, all `:effect :propose`)

```text
:log-field-record
  — record planting/fibre-yield/quality-grade data
  — requires a registered field; non-positive acreage or an unrecognized
    quality-grade code is rejected

:schedule-field-operation
  — propose a planting/defoliation/retting/harvest scheduling operation
  — does NOT make agronomic decisions

:flag-crop-health-concern
  — surface a pest (e.g. boll weevil), disease, or drought-stress concern
  — ALWAYS escalates for human review

:order-supplies
  — procurement for seed, fertilizer, equipment
  — escalates if cost exceeds its category threshold
```

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs the
physical domain work**. Here a field-management robot handles:

- Field record logging and entry
- Field-operation scheduling and reminders
- Supply inventory and ordering
- Audit ledger maintenance

The **FieldOperationsGovernor** is the independent safety layer that gates all
proposals before a robot action is executed. The governor never dispatches
hardware directly; `:high`/`:safety-critical` actions (such as escalated
crop-health concerns or high-cost supply orders) require human sign-off.

## Core Contract

```text
operational request (log, schedule, concern, order)
        |
        v
FibreOpsAdvisor -> FieldOperationsGovernor -> phase gate -> commit, or escalate for human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated operation can dispatch a robot action the governor refuses, suppress an
operating record, or hide a crop-health concern without governor approval and audit
evidence.

## Module structure

Mirrors `cloud-itonami-isic-0111` (`cerealops.*`) module-for-module:

- `fibreops.facts` — reference data: supply-category cost thresholds, fibre
  crops, field-operation vocabulary, fibre-quality grade vocabulary
- `fibreops.registry` — pure independent verification functions
  (cost/acreage/quality-grade/confidence)
- `fibreops.store` — `Store` protocol + in-memory `MemStore` (field registration lookup)
- `fibreops.advisor` — `Advisor` protocol + `MockAdvisor` (the sealed LLM/decision node)
- `fibreops.governor` — `FieldOperationsGovernor`: hard invariants + escalation gates
- `fibreops.phase` — 0→3 rollout phase gate
- `fibreops.operation` — composes advisor → governor → phase into one operation run
- `fibreops.sim` — demo runner (`clojure -M:run`)

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISIC Rev. 4 `0116`). Required capabilities:

- :robotics
- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger
- :telemetry

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Testing

```bash
clojure -M:test   # run the test suite
clojure -M:lint   # clj-kondo, 0 errors / 0 warnings
clojure -M:run    # demo runner
```

## License

AGPL-3.0-or-later.
