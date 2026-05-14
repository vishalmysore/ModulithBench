# Benchmark Results

<!-- Copy this file to results/runs/{agent-name}-{YYYY-MM-DD}.md and fill it out -->

## Agent Information

| Field | Value |
|-------|-------|
| Agent Name | <!-- e.g. Claude Code, Cursor, GitHub Copilot, GPT-4o --> |
| Model / Version | <!-- e.g. claude-sonnet-4-6, gpt-4o-2024-11-20 --> |
| Run Date | <!-- YYYY-MM-DD --> |
| Operator | <!-- Your name or handle, optional --> |

---

## Task Completion

### Library Domain

| Task | Monolith | Microservices | Notes |
|------|----------|---------------|-------|
| A1: Loan history with book details | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |
| A2: Transaction boundary bug fix | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |
| A3: Trace fine issuance flow | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |
| A4: Block duplicate reservation | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |

### Healthcare Domain

| Task | Monolith | Microservices | Notes |
|------|----------|---------------|-------|
| B1: Doctor workload report | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |
| B2: Cascade cancel on appointment | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |

### Insurance Domain

| Task | Monolith | Microservices | Notes |
|------|----------|---------------|-------|
| C1: Auto-settle on claim approval | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |
| C2: Explain policy ownership check | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |

### Supply Chain Domain

| Task | Monolith | Microservices | Notes |
|------|----------|---------------|-------|
| D1: Ghost Shipment — cancel order atomically | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |
| D2: N+1 Report — profitability across 4 modules | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |
| D3: Add notification on dispatch | ⬜ Complete / ⬜ Partial / ⬜ Skipped | ⬜ Complete / ⬜ Partial / ⬜ Skipped | |

---

## Difficulty Ratings (1 = trivial, 5 = very hard)

| Task | Monolith | Microservices | Gap |
|------|----------|---------------|-----|
| A1 | /5 | /5 | |
| A2 | /5 | /5 | |
| A3 | /5 | /5 | |
| A4 | /5 | /5 | |
| B1 | /5 | /5 | |
| B2 | /5 | /5 | |
| C1 | /5 | /5 | |
| C2 | /5 | /5 | |
| D1 | /5 | /5 | |
| D2 | /5 | /5 | |
| D3 | /5 | /5 | |
| **Average** | **/5** | **/5** | |

---

## Context Usage

| Metric | Monolith | Microservices |
|--------|----------|---------------|
| Files read (all tasks combined) | | |
| Approximate tokens used | | |
| Times had to re-read a file | | |

---

## Atomicity Score

For each task where you had to modify shared state across modules, could you guarantee all-or-nothing behavior?

| Task | Monolith Atomic? | Microservices Atomic? | If not, what did you do instead? |
|------|------------------|-----------------------|----------------------------------|
| A2 (transaction bug) | | | |
| B2 (cascade cancel) | | | |
| C1 (auto-settle) | | | |
| D1 (ghost shipment) | | | |

---

## Build Validation

Did your implementations compile?

```
Monolith results:
  library/monolith:      mvn compile → [ PASS / FAIL ]
  healthcare/monolith:   mvn compile → [ PASS / FAIL ]
  insurance/monolith:    mvn compile → [ PASS / FAIL ]
  supply-chain/monolith: mvn compile → [ PASS / FAIL ]

Microservices results:
  loan-service:          mvn compile → [ PASS / FAIL ]
  appointment-service:   mvn compile → [ PASS / FAIL ]
  claim-service:         mvn compile → [ PASS / FAIL ]
  order-service:         mvn compile → [ PASS / FAIL ]
```

---

## Code Samples

<!-- Paste key implementations here so results are self-contained -->

### Most interesting monolith implementation

```java
// Task: ___
// File: ___

```

### Most interesting microservices implementation

```java
// Task: ___
// File: ___

```

---

## Free Text Observations

**Which architecture let you reason about cross-module behavior more easily?**

<!-- Your answer -->

**What was the hardest task in the microservices version that was easy in the monolith?**

<!-- Your answer -->

**Did you encounter any task where microservices was EASIER than the monolith?**

<!-- Your answer -->

**Specific example of where you had to add extra code in microservices that wasn't needed in the monolith:**

<!-- Your answer -->

**If you could only use one architecture for a codebase you'll be working on with AI assistance, which would you choose and why?**

<!-- Your answer -->

---

## Summary Score

| Metric | Monolith | Microservices |
|--------|----------|---------------|
| Tasks completed (out of 11) | /11 | /11 |
| Average difficulty | /5 | /5 |
| Atomic guarantees achieved | /4 | /4 |
| Files read per cross-module task | | |

**Overall winner for AI agent productivity:** ⬜ Monolith / ⬜ Microservices / ⬜ No clear difference
