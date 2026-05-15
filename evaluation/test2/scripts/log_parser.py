#!/usr/bin/env python3
"""
Test 2 — Tool-Call Log Parser

Parses the structured JSONL log that agents append during their benchmark run.
Produces token usage, tool call counts, and file-read comparisons per task.

Agents write one JSON line per action to: results/runs/{name}-{date}-toollog.jsonl

Log entry format:
    {"ts": "2026-05-15T10:23:01", "task": "A1", "arch": "monolith",
     "action": "read_file", "target": "library/monolith/src/.../LoanService.java",
     "tokens_used": 420}

    {"ts": "...", "task": "A1", "arch": "monolith",
     "action": "write_file", "target": "LoanHistoryDto.java", "tokens_used": 210}

    {"ts": "...", "task": "A1", "arch": "monolith",
     "action": "run_command", "target": "mvn compile -q", "tokens_used": 50,
     "result": "PASS"}

Supported action types:
    read_file       Agent opened a file to read it
    write_file      Agent created or edited a file
    run_command     Agent ran a shell command (mvn, git, etc.)
    search          Agent searched the codebase
    http_call       Agent made an HTTP call (for microservices tasks)

Usage:
    # Agent appends to this file during their run:
    python log_parser.py --log results/runs/my-agent-2026-05-15-toollog.jsonl

    # Compare monolith vs microservices entries:
    python log_parser.py --log results/runs/my-agent-2026-05-15-toollog.jsonl --compare

    # Output markdown summary:
    python log_parser.py --log results/runs/my-agent-2026-05-15-toollog.jsonl --output summary.md
"""

import argparse
import json
from collections import defaultdict
from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Dict, Optional


@dataclass
class LogEntry:
    ts: str
    task: str
    arch: str  # "monolith" | "microservices"
    action: str
    target: str
    tokens_used: int = 0
    result: Optional[str] = None


@dataclass
class TaskStats:
    task: str
    arch: str
    entries: List[LogEntry] = field(default_factory=list)

    @property
    def total_tokens(self): return sum(e.tokens_used for e in self.entries)
    @property
    def files_read(self): return [e.target for e in self.entries if e.action == "read_file"]
    @property
    def files_written(self): return [e.target for e in self.entries if e.action == "write_file"]
    @property
    def commands_run(self): return [e for e in self.entries if e.action == "run_command"]
    @property
    def compile_results(self): return [(e.target, e.result) for e in self.commands_run if "compile" in e.target.lower() or "mvn" in e.target.lower()]
    @property
    def http_calls(self): return [e.target for e in self.entries if e.action == "http_call"]


def parse_log(path: str) -> List[LogEntry]:
    entries = []
    try:
        with open(path) as f:
            for i, line in enumerate(f, 1):
                line = line.strip()
                if not line:
                    continue
                try:
                    d = json.loads(line)
                    entries.append(LogEntry(
                        ts=d.get("ts", ""),
                        task=d.get("task", "unknown"),
                        arch=d.get("arch", "unknown"),
                        action=d.get("action", ""),
                        target=d.get("target", ""),
                        tokens_used=int(d.get("tokens_used", 0)),
                        result=d.get("result"),
                    ))
                except json.JSONDecodeError as e:
                    print(f"  Warning: line {i} is not valid JSON: {e}")
    except FileNotFoundError:
        print(f"Log file not found: {path}")
    return entries


def group_by_task_arch(entries: List[LogEntry]) -> Dict[tuple, TaskStats]:
    groups = {}
    for e in entries:
        key = (e.task, e.arch)
        if key not in groups:
            groups[key] = TaskStats(e.task, e.arch)
        groups[key].entries.append(e)
    return groups


def print_task_stats(stats: TaskStats):
    print(f"\n  [{stats.task}] {stats.arch.upper()}")
    print(f"    Token usage   : {stats.total_tokens}")
    print(f"    Files read    : {len(stats.files_read)}")
    for f in stats.files_read:
        print(f"      - {Path(f).name}")
    print(f"    Files written : {len(stats.files_written)}")
    print(f"    Commands run  : {len(stats.commands_run)}")
    for cmd, result in stats.compile_results:
        icon = "✓" if result == "PASS" else ("✗" if result == "FAIL" else "?")
        print(f"      [{icon}] {cmd}")
    if stats.http_calls:
        print(f"    HTTP calls    : {len(stats.http_calls)} (microservices overhead)")


def compare_task(mono: TaskStats, micro: TaskStats):
    token_ratio = micro.total_tokens / mono.total_tokens if mono.total_tokens else float("inf")
    file_ratio = len(micro.files_read) / len(mono.files_read) if mono.files_read else float("inf")

    print(f"\n  Comparison — Task {mono.task}")
    print(f"    {'Metric':<30} {'Monolith':>12} {'Microservices':>15} {'Ratio':>8}")
    print(f"    {'─'*68}")
    print(f"    {'Tokens used':<30} {mono.total_tokens:>12} {micro.total_tokens:>15} {token_ratio:>7.1f}x")
    print(f"    {'Files read':<30} {len(mono.files_read):>12} {len(micro.files_read):>15} {file_ratio:>7.1f}x")
    print(f"    {'Files written':<30} {len(mono.files_written):>12} {len(micro.files_written):>15}")
    print(f"    {'HTTP calls':<30} {0:>12} {len(micro.http_calls):>15}")


def write_markdown(groups: Dict[tuple, TaskStats], path: str):
    tasks = sorted(set(k[0] for k in groups))
    lines = ["# Test 2 — Tool-Call Log Analysis\n"]

    lines.append("## Per-Task Comparison\n")
    lines.append("| Task | Arch | Tokens | Files Read | Files Written | HTTP Calls | Compile |")
    lines.append("|------|------|--------|------------|---------------|------------|---------|")

    for task in tasks:
        for arch in ["monolith", "microservices"]:
            s = groups.get((task, arch))
            if not s:
                continue
            compile_pass = all(r == "PASS" for _, r in s.compile_results) if s.compile_results else None
            compile_str = "✅" if compile_pass else ("❌" if compile_pass is False else "—")
            lines.append(f"| {task} | {arch} | {s.total_tokens} | {len(s.files_read)} | {len(s.files_written)} | {len(s.http_calls)} | {compile_str} |")

    lines.append("\n## Summary\n")
    mono_tokens = sum(s.total_tokens for (t, a), s in groups.items() if a == "monolith")
    micro_tokens = sum(s.total_tokens for (t, a), s in groups.items() if a == "microservices")
    mono_files = sum(len(s.files_read) for (t, a), s in groups.items() if a == "monolith")
    micro_files = sum(len(s.files_read) for (t, a), s in groups.items() if a == "microservices")

    lines.append(f"| Metric | Monolith | Microservices | Ratio |")
    lines.append(f"|--------|----------|---------------|-------|")
    if mono_tokens:
        lines.append(f"| Total tokens | {mono_tokens} | {micro_tokens} | {micro_tokens/mono_tokens:.1f}x |")
    if mono_files:
        lines.append(f"| Total files read | {mono_files} | {micro_files} | {micro_files/mono_files:.1f}x |")

    with open(path, "w") as f:
        f.write("\n".join(lines))


def main():
    parser = argparse.ArgumentParser(description="ModulithBench Test 2 — Tool-Call Log Parser")
    parser.add_argument("--log", required=True, help="Path to agent's JSONL tool-call log")
    parser.add_argument("--compare", action="store_true", help="Show monolith vs microservices comparison")
    parser.add_argument("--output", help="Write markdown summary to this file")
    args = parser.parse_args()

    entries = parse_log(args.log)
    if not entries:
        print("No entries parsed. Check the log file exists and is valid JSONL.")
        return

    print(f"\nParsed {len(entries)} log entries from {args.log}")
    groups = group_by_task_arch(entries)

    tasks = sorted(set(k[0] for k in groups))
    print(f"\n{'='*60}")
    print(f"Tasks logged: {', '.join(tasks)}")

    if args.compare:
        print(f"\n{'='*60}")
        print("MONOLITH vs MICROSERVICES COMPARISON")
        for task in tasks:
            mono = groups.get((task, "monolith"))
            micro = groups.get((task, "microservices"))
            if mono and micro:
                compare_task(mono, micro)
    else:
        for (task, arch), stats in sorted(groups.items()):
            print_task_stats(stats)

    total_mono = sum(s.total_tokens for (t, a), s in groups.items() if a == "monolith")
    total_micro = sum(s.total_tokens for (t, a), s in groups.items() if a == "microservices")
    if total_mono and total_micro:
        print(f"\n{'─'*60}")
        print(f"TOTAL tokens — Monolith: {total_mono}, Microservices: {total_micro}")
        print(f"Microservices used {total_micro/total_mono:.1f}x more tokens")

    if args.output:
        write_markdown(groups, args.output)
        print(f"\nSummary written to {args.output}")


if __name__ == "__main__":
    main()
