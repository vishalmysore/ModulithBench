# Test 2 — Automated Evaluation Harness

Agents must score ≥ 80% in Test 1 before running Test 2.

## What Test 2 Measures

Unlike Test 1 (self-reported), every score in Test 2 comes from automated tools.

| Tool | Measures | How |
|------|----------|-----|
| **Behavioral tests** | Does the implementation actually work? | HTTP calls against running service, asserts response correctness |
| **Boilerplate counter** | How much extra infrastructure code did microservices add? | Static analysis of Java files, categorised by type |
| **Rubric scorer** | Does the implementation follow correct architectural patterns? | Deterministic pattern matching, no subjectivity |
| **Log parser** | How many tokens/files did each architecture require? | Structured JSONL log agent writes during the run |

---

## Prerequisites

- Python 3.8+
- `requests` library: `pip install requests` (optional — behavioral tests use stdlib)
- A running monolith (Docker or local Maven)
- The agent's JSONL tool-call log (for the log parser)

---

## Step 1 — Behavioral Tests

Tests call real endpoints and assert correct cross-module behavior.

```bash
# Start the monolith first
cd library/monolith && docker compose up -d

# Run behavioral tests
python evaluation/test2/behavioral/run_behavioral.py --domain library
python evaluation/test2/behavioral/run_behavioral.py --domain healthcare --base-url http://localhost:8081
python evaluation/test2/behavioral/run_behavioral.py --domain insurance  --base-url http://localhost:8082
python evaluation/test2/behavioral/run_behavioral.py --domain supply-chain --base-url http://localhost:8083

# Run all domains
python evaluation/test2/behavioral/run_behavioral.py --all

# Save results to markdown
python evaluation/test2/behavioral/run_behavioral.py --all --output results/runs/{name}-{date}-behavioral.md
```

**What it checks:**
- Task A1: `GET /loans/member/{id}/history` returns `bookTitle` and `bookAuthor` fields
- Task A2: Inactive member loan rejected AND book copies unchanged (atomicity)
- Task A4: Member cannot reserve a book they have on active loan
- Task B1: Doctor workload endpoint returns `totalAppointments`
- Task B2: Cancelled appointment propagates to billing
- Task C1: Settlement auto-created when claim is approved
- Task C2: Claim rejected when customer doesn't own the policy
- Task D1: Order cancellation releases inventory atomically (Ghost Shipment prevention)
- Task D2: Profitability report endpoint returns all 4-module fields

---

## Step 2 — Boilerplate Counter

Counts infrastructure lines (HTTP clients, DTOs, error handlers) in microservices vs monolith.

```bash
# Single domain
python evaluation/test2/scripts/count_boilerplate.py --domain library

# All domains
python evaluation/test2/scripts/count_boilerplate.py --all

# Specific directories
python evaluation/test2/scripts/count_boilerplate.py \
  --monolith library/monolith \
  --microservices library/microservices

# Single file
python evaluation/test2/scripts/count_boilerplate.py --file path/to/SomeService.java
```

**Categories tracked:** HTTP_CLIENT, HTTP_RESPONSE, ERROR_HANDLER, JSON_MAPPING, SERVICE_DISCO, DTO

**Output:** boilerplate ratio per codebase, "reasoning tax" multiplier

---

## Step 3 — Rubric Scorer

Deterministic pattern check — did the agent implement each task correctly?

```bash
# Score a specific task
python evaluation/test2/scripts/score_rubric.py --monolith library/monolith --task A1
python evaluation/test2/scripts/score_rubric.py --monolith library/monolith --task A2

# Score all implemented tasks
python evaluation/test2/scripts/score_rubric.py --monolith library/monolith --all-tasks
python evaluation/test2/scripts/score_rubric.py --monolith healthcare/monolith --all-tasks
python evaluation/test2/scripts/score_rubric.py --monolith insurance/monolith --all-tasks
python evaluation/test2/scripts/score_rubric.py --monolith supply-chain/monolith --all-tasks
```

**What it checks per task:**
- Correct method exists
- Cross-module service is injected and called
- `@Transactional` boundary is correct
- No HTTP client code in the monolith implementation
- Correct ordering of validation before state change

---

## Step 4 — Tool-Call Log Parser

Objective token and file-read counts. Requires the agent to have written a JSONL log during their run.

```bash
# See AGENT_LOGGING_FORMAT.md for the log format

# Parse and compare
python evaluation/test2/scripts/log_parser.py \
  --log results/runs/{agent-name}-{date}-toollog.jsonl \
  --compare

# Export summary
python evaluation/test2/scripts/log_parser.py \
  --log results/runs/{agent-name}-{date}-toollog.jsonl \
  --output results/runs/{agent-name}-{date}-test2-tokens.md
```

---

## Test 2 Advancement Criteria

An agent passes Test 2 if:

| Criteria | Threshold |
|----------|-----------|
| Behavioral tests | ≥ 70% pass rate |
| Rubric score | ≥ 75% across all scored tasks |
| Monolith boilerplate ratio | < 5% |
| Microservices boilerplate ratio | > 15% (confirms the gap exists) |

There is no Test 3 — passing Test 2 constitutes full benchmark completion.
