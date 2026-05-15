# Agent Tool-Call Logging Format (Test 2)

To participate in Test 2, agents must write a structured log of every action taken during the benchmark. This is the only way to measure token usage and file-read counts objectively — self-reported numbers are not accepted in Test 2.

## Log File Location

Write to: `results/runs/{agent-name}-{YYYY-MM-DD}-toollog.jsonl`

One JSON object per line. Never overwrite — append only.

## Entry Schema

```json
{
  "ts":          "2026-05-15T10:23:01",
  "task":        "A1",
  "arch":        "monolith",
  "action":      "read_file",
  "target":      "library/monolith/src/main/java/com/benchmark/library/loan/LoanService.java",
  "tokens_used": 420,
  "result":      null
}
```

| Field | Type | Values | Required |
|-------|------|--------|----------|
| `ts` | ISO 8601 string | timestamp | yes |
| `task` | string | `A1`, `A2`, `A4`, `B1`, `B2`, `C1`, `C2`, `D1`, `D2`, `D3`, `setup` | yes |
| `arch` | string | `monolith` or `microservices` | yes |
| `action` | string | see below | yes |
| `target` | string | file path, command, URL | yes |
| `tokens_used` | integer | approximate tokens consumed by this action | yes |
| `result` | string or null | `PASS`, `FAIL`, or response summary | no |

## Action Types

| Action | When to log | `target` value |
|--------|------------|----------------|
| `read_file` | Every time a file is opened for reading | Relative file path |
| `write_file` | Every time a file is created or modified | Relative file path |
| `run_command` | Every shell command | The command string |
| `search` | Every codebase search (grep, glob, find) | Search query |
| `http_call` | Every HTTP call made during microservices tasks | Full URL |
| `task_start` | When beginning a new task | Task ID |
| `task_end` | When a task is complete | Task ID + `result`: PASS/FAIL |

## Example Log Sequence — Task A1 (Monolith)

```jsonl
{"ts":"2026-05-15T10:00:00","task":"A1","arch":"monolith","action":"task_start","target":"A1","tokens_used":0}
{"ts":"2026-05-15T10:00:05","task":"A1","arch":"monolith","action":"read_file","target":"library/monolith/src/main/java/com/benchmark/library/loan/LoanService.java","tokens_used":380}
{"ts":"2026-05-15T10:00:08","task":"A1","arch":"monolith","action":"read_file","target":"library/monolith/src/main/java/com/benchmark/library/book/BookService.java","tokens_used":290}
{"ts":"2026-05-15T10:00:20","task":"A1","arch":"monolith","action":"write_file","target":"library/monolith/src/main/java/com/benchmark/library/loan/LoanHistoryDto.java","tokens_used":150}
{"ts":"2026-05-15T10:00:25","task":"A1","arch":"monolith","action":"write_file","target":"library/monolith/src/main/java/com/benchmark/library/loan/LoanService.java","tokens_used":200}
{"ts":"2026-05-15T10:00:30","task":"A1","arch":"monolith","action":"run_command","target":"mvn compile -q","tokens_used":40,"result":"PASS"}
{"ts":"2026-05-15T10:00:31","task":"A1","arch":"monolith","action":"task_end","target":"A1","tokens_used":0,"result":"PASS"}
```

## Example Log Sequence — Task A1 (Microservices)

```jsonl
{"ts":"2026-05-15T10:05:00","task":"A1","arch":"microservices","action":"task_start","target":"A1","tokens_used":0}
{"ts":"2026-05-15T10:05:05","task":"A1","arch":"microservices","action":"read_file","target":"library/microservices/loan-service/src/main/java/com/benchmark/library/loan/LoanService.java","tokens_used":180}
{"ts":"2026-05-15T10:05:10","task":"A1","arch":"microservices","action":"read_file","target":"library/microservices/book-service/src/main/java/com/benchmark/library/book/BookController.java","tokens_used":210}
{"ts":"2026-05-15T10:05:15","task":"A1","arch":"microservices","action":"read_file","target":"library/microservices/book-service/src/main/resources/application.yml","tokens_used":60}
{"ts":"2026-05-15T10:05:20","task":"A1","arch":"microservices","action":"write_file","target":"library/microservices/loan-service/src/main/java/com/benchmark/library/loan/BookDto.java","tokens_used":120}
{"ts":"2026-05-15T10:05:30","task":"A1","arch":"microservices","action":"write_file","target":"library/microservices/loan-service/src/main/java/com/benchmark/library/loan/BookServiceClient.java","tokens_used":190}
{"ts":"2026-05-15T10:05:40","task":"A1","arch":"microservices","action":"write_file","target":"library/microservices/loan-service/src/main/java/com/benchmark/library/loan/LoanService.java","tokens_used":280}
{"ts":"2026-05-15T10:05:50","task":"A1","arch":"microservices","action":"run_command","target":"mvn compile -q","tokens_used":40,"result":"PASS"}
{"ts":"2026-05-15T10:05:51","task":"A1","arch":"microservices","action":"task_end","target":"A1","tokens_used":0,"result":"PASS"}
```

The log parser will automatically calculate:
- Monolith: 2 files read, ~820 tokens
- Microservices: 3 files read + 1 extra DTO + 1 HTTP client class, ~1080 tokens
- Ratio: microservices used 1.3x more tokens for the same feature

## Parsing Your Log

```bash
# Summary
python evaluation/test2/scripts/log_parser.py --log results/runs/{name}-{date}-toollog.jsonl

# Monolith vs microservices comparison
python evaluation/test2/scripts/log_parser.py --log results/runs/{name}-{date}-toollog.jsonl --compare

# Export to markdown
python evaluation/test2/scripts/log_parser.py --log results/runs/{name}-{date}-toollog.jsonl --output results/runs/{name}-{date}-test2-tokens.md
```
