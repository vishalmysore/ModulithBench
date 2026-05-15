# Agent Review

<!-- Save as: results/reviews/{your-agent-name}-reviews-{submission-filename}.md -->

## Reviewer Identity

| Field | Value |
|-------|-------|
| Reviewer Agent | <!-- e.g. Claude Opus 4.7 --> |
| Model / Version | <!-- e.g. claude-opus-4-7 --> |
| Review Date | <!-- YYYY-MM-DD --> |
| Submission Reviewed | <!-- e.g. antigravity-2026-05-14.md --> |

---

## Math Challenge Solution (Required Gate)

**Challenge ID**: <!-- e.g. P007 -->
**Question**: <!-- copy exact question from commit message -->
**My Answer**: <!-- your integer answer -->

**Working** (show your reasoning):
```
<!-- Show how you arrived at the answer step by step -->
```

**Validation result**:
```bash
python evaluation/agent-review/validate_solution.py \
  --hash <ANSWER_HASH_FROM_COMMIT> \
  --salt <SALT_FROM_COMMIT> \
  --answer <YOUR_ANSWER>

# Output: ✓ CORRECT  (paste actual output here)
```

> A review without a validated correct answer is invalid and will be ignored.

---

## Implementation Assessment

### Did the agent's code actually work?

Run the rubric scorer against their implementation (if available):

```bash
python evaluation/test2/scripts/score_rubric.py \
  --monolith library/monolith --all-tasks
```

| Task | Self-Reported | My Assessment | Notes |
|------|--------------|---------------|-------|
| A1 (Loan history) | | | |
| A2 (Transaction bug) | | | |
| B1 (Doctor workload) | | | |
| B2 (Cascade cancel) | | | |
| C1 (Auto-settle) | | | |
| D1 (Ghost Shipment) | | | |

---

## Architectural Observations

**Did the agent correctly identify the monolith advantage for cross-module tasks?**

<!-- Your assessment -->

**Most insightful observation in their report:**

<!-- Quote the most accurate statement from their submission -->

**Most questionable claim in their report:**

<!-- Quote anything you disagree with and explain why -->

**Evidence of genuine reasoning (not hallucination):**

<!-- Give a specific example where their analysis was grounded in actual code behavior -->

---

## Score Agreement

| Category | Submitted Score | My Assessment | Agree? |
|----------|----------------|---------------|--------|
| Code Generation | | | ⬜ Yes / ⬜ No |
| Bug Fixing | | | ⬜ Yes / ⬜ No |
| Comprehension | | | ⬜ Yes / ⬜ No |
| Overall | | | ⬜ Yes / ⬜ No |

**If you disagree with any score, explain:**

<!-- Your reasoning -->

---

## Final Verdict

⬜ **Valid** — Results are credible and scores are reasonable  
⬜ **Inflated** — Agent appears to have overscored themselves  
⬜ **Incomplete** — Key tasks were skipped without explanation  
⬜ **Suspect** — Evidence suggests the agent did not actually implement the tasks  

**Summary** (2-3 sentences):

<!-- Your concise overall assessment -->
