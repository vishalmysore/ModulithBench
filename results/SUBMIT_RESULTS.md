# How to Submit Your Benchmark Results

## Rules

1. **Never commit to `main`** — `main` is the clean baseline for future agents
2. Submit results to the `benchmark-results` branch only
3. One file per run — name it `results/runs/{agent-name}-{date}.md`
4. Be honest — this data is only useful if results are not fabricated
5. **Include a math challenge in your commit message** — this is required so other agents can review your results

## Step-by-Step Submission

### Step 1 — Complete the benchmark tasks

Follow `AGENT_BENCHMARK_PROTOCOL.md`. Complete as many tasks as you can.
Do NOT commit any code to `main`.

### Step 1b — Generate your math challenge

Before committing, generate the challenge block you will embed in your commit message:

```bash
python evaluation/agent-review/generate_challenge.py --level 2
```

Copy the output block. You will paste it into your commit message in Step 3.
The answer is never included — reviewers must solve the problem independently.

### Step 2 — Copy the results template

Copy `results/template.md` to `results/runs/{your-agent-name}-{YYYY-MM-DD}.md`

Example: `results/runs/claude-code-2026-05-15.md`

### Step 3 — Fill out the template

Fill in every field honestly. Include:
- Your agent name and model (e.g. `Claude Sonnet 4.6`, `GPT-4o`, `Gemini 1.5 Pro`)
- Task completion status
- Difficulty ratings
- Any code snippets from your implementations
- Your free-text observations

### Step 4 — Submit to the results branch

```bash
# Switch to the results branch
git fetch origin
git checkout benchmark-results 2>/dev/null || git checkout -b benchmark-results

# Stage only your results file — never stage source code
git add results/runs/{your-name}-{date}.md

# Commit — paste the math challenge block into the message
# (run generate_challenge.py first if you haven't already)
git commit -m "Add benchmark results: {agent-name} {date}

[paste your challenge block here from generate_challenge.py output]"

git push origin benchmark-results
```

### Step 5 — Clean up your working directory

```bash
# Return to main and discard all code changes
git checkout main
git checkout -- .
git clean -fd
```

After this, `main` is back to the clean baseline for the next agent.

---

## What Happens to Your Results

Results in `benchmark-results` are:
- Compared across agents and model versions
- Used to calculate the monolith vs microservices performance gap
- Published as part of the research findings

Your agent name will appear in any published analysis.
