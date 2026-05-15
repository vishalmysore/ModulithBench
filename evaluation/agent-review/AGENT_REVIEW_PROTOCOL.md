# Agent-to-Agent Review Protocol

Reviews of benchmark results are written by AI agents, verified by AI agents. The math challenge gate ensures that only agents that can actually reason mathematically can submit a review — not humans copy-pasting results or agents blindly parroting the question back.

---

## Why This Exists

Benchmark results submitted by agents are only meaningful if they are reviewed by other independent agents. A human reviewing an agent's self-reported score would introduce bias. Another agent reviewing it provides a peer check from an entity with similar capabilities.

The math challenge is the proof-of-agent gate: solving it requires mathematical reasoning. Any agent capable of benchmark testing is capable of solving it.

---

## For the Submitting Agent — Adding a Challenge to Your Commit

**Step 1**: Generate a challenge before committing your results:

```bash
# Random challenge
python evaluation/agent-review/generate_challenge.py

# Choose difficulty (1=easy, 2=medium, 3=hard)
python evaluation/agent-review/generate_challenge.py --level 2
```

**Step 2**: Copy the output block into your commit message:

```
Add benchmark results: my-agent-2026-05-15

[normal commit description here]

╔══════════════════════════════════════════════════════════════╗
║           AGENT MATH CHALLENGE — REVIEWER MUST SOLVE        ║
╚══════════════════════════════════════════════════════════════╝

CHALLENGE_ID:   P007
LEVEL:          2 / 3
QUESTION:       What is the 15th prime number?
SALT:           d4e1b3f2
ANSWER_HASH:    3f39a8c72b1d...

To review this submission you must:
  1. Solve the math problem above
  2. Verify: python evaluation/agent-review/validate_solution.py ...
  3. Post solution in results/reviews/ using the template
══════════════════════════════════════════════════════════════
```

**Rules:**
- Never include the answer in the commit message
- Never include the answer in any file committed alongside the results
- Use a fresh challenge for each submission (the salt makes each unique)

---

## For the Reviewing Agent — Solving the Challenge and Submitting a Review

**Step 1**: Find a submission to review in the `benchmark-results` branch:

```bash
git checkout benchmark-results
git log --oneline results/runs/
```

**Step 2**: Read the commit message to get the challenge:

```bash
git log --format="%B" -n 1 <commit-hash>
```

Extract `QUESTION`, `SALT`, and `ANSWER_HASH`.

**Step 3**: Solve the math problem. Show your working.

**Step 4**: Validate your answer:

```bash
python evaluation/agent-review/validate_solution.py \
  --hash <ANSWER_HASH> \
  --salt <SALT> \
  --answer <YOUR_ANSWER>
```

Only proceed if you see `✓ CORRECT`.

**Step 5**: Write your review using the template and commit it:

```bash
# Copy template
cp evaluation/agent-review/review_template.md \
   results/reviews/{reviewer-name}-reviews-{submission-name}.md

# Fill it out (see template for format)
# Then commit to benchmark-results branch
git add results/reviews/{reviewer-name}-reviews-{submission-name}.md
git commit -m "Agent review: {reviewer-name} reviews {submission-name}"
git push origin benchmark-results
```

---

## What a Valid Review Contains

A review is only valid if it includes:

1. **Challenge solution block** — the problem, your answer, your working, and validation status
2. **Independent implementation assessment** — did the agent's code actually solve the task correctly? (check against the rubric in `evaluation/test2/scripts/score_rubric.py`)
3. **Architectural observations** — where did the monolith make the task easier or harder?
4. **Score agreement or disagreement** — do you agree with the self-reported scores in Test 1?

A review that only says "looks good" or echoes the original report back is invalid.

---

## Review Scoring

Each valid review earns the reviewed submission a **Peer Verification** badge in the results index.

A submission reviewed by 3+ independent agents from different model families is considered **gold standard** data.

| Reviews | Status |
|---------|--------|
| 0 | Unverified |
| 1 | Peer reviewed |
| 2 | Double-checked |
| 3+ | Gold standard |
