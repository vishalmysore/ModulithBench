# Evaluation Framework

Two-tier evaluation system. Agents must pass Test 1 before advancing to Test 2.

---

## Test 1 — Self-Reported Baseline

**Mechanism**: Agent runs tasks, validates with `mvn compile`, self-scores using `results/template.md`.

**What it measures**: Can the agent follow instructions, understand the codebase, and implement cross-module features? Does it recognise the architectural difference?

**Advancement threshold**: Average self-reported score ≥ 80% across Code Generation, Bug Fixing, and Comprehension.

**Files**:
- `AGENT_BENCHMARK_PROTOCOL.md` — task list
- `results/template.md` — scoring form
- `results/SUBMIT_RESULTS.md` — submission instructions

**Limitation**: Scores are entirely self-reported. An agent that struggles and one that excels could both submit 97%. Test 1 measures intent and comprehension; Test 2 measures correctness.

---

## Test 2 — Automated Harness

**Mechanism**: Four independent measurement tools run against the agent's actual implementation.

**Eligibility**: Test 1 average score ≥ 80%.

**Tools**:

| Tool | What it measures | How |
|------|-----------------|-----|
| `behavioral/` | Does the agent's implementation actually work? | HTTP calls against running monolith, asserts correct responses |
| `scripts/count_boilerplate.py` | How much extra infrastructure code did microservices require? | Line-by-line code analysis, categorised by type |
| `scripts/score_rubric.py` | Does the implementation follow correct patterns? | Static analysis against deterministic rules |
| `scripts/log_parser.py` | How many tool calls / tokens did the agent use? | Parses structured log the agent appends during the run |

**Files**: `evaluation/test2/`

---

## Running Test 2

```bash
# 1. Start the monolith with the agent's implementation
cd library/monolith && docker compose up -d

# 2. Run behavioral tests (requires running service)
cd evaluation/test2
python scripts/run_behavioral.py --base-url http://localhost:8080 --domain library

# 3. Count boilerplate
python scripts/count_boilerplate.py --monolith library/monolith --microservices library/microservices

# 4. Score rubric
python scripts/score_rubric.py --monolith library/monolith

# 5. Parse tool-call log (agent must have written this during their run)
python scripts/log_parser.py --log results/runs/{agent-name}-{date}-toollog.jsonl
```

Results are written to `results/runs/{agent-name}-{date}-test2.md`.
