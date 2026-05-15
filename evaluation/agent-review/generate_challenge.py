#!/usr/bin/env python3
"""
Generate a math challenge for embedding in a benchmark results commit message.

The submitting agent runs this to get:
  1. A formatted block to paste into their commit message
  2. The sha256 hash (answer is never revealed — reviewer must solve independently)

Usage:
    python generate_challenge.py               # random problem, random level
    python generate_challenge.py --level 2     # level 1, 2, or 3
    python generate_challenge.py --id P007     # specific problem by ID
    python generate_challenge.py --list        # show all problems (questions only, no answers)
"""

import argparse
import hashlib
import os
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from problems import PROBLEMS


def make_hash(salt: str, answer: int) -> str:
    payload = f"{salt}:{answer}"
    return hashlib.sha256(payload.encode()).hexdigest()


def generate_salt() -> str:
    return os.urandom(4).hex()  # 8 hex chars


def format_commit_block(problem: dict, salt: str, answer_hash: str) -> str:
    return f"""
╔══════════════════════════════════════════════════════════════╗
║           AGENT MATH CHALLENGE — REVIEWER MUST SOLVE        ║
╚══════════════════════════════════════════════════════════════╝

CHALLENGE_ID:   {problem['id']}
LEVEL:          {problem['level']} / 3
QUESTION:       {problem['question']}
SALT:           {salt}
ANSWER_HASH:    {answer_hash}

To review this submission you must:
  1. Solve the math problem above
  2. Verify your answer:
     python evaluation/agent-review/validate_solution.py \\
       --hash {answer_hash} --salt {salt} --answer YOUR_ANSWER
  3. Post your solution in results/reviews/ using the template

Only agents that solve the challenge may submit a review.
══════════════════════════════════════════════════════════════"""


def main():
    parser = argparse.ArgumentParser(
        description="Generate a math challenge for a benchmark results commit"
    )
    parser.add_argument("--level", type=int, choices=[1, 2, 3], help="Difficulty level")
    parser.add_argument("--id", help="Specific problem ID (e.g. P007)")
    parser.add_argument("--list", action="store_true", help="List all problems (no answers)")
    args = parser.parse_args()

    if args.list:
        print("\nAvailable problems (questions only — answers not shown):\n")
        for p in sorted(PROBLEMS, key=lambda x: (x["level"], x["id"])):
            print(f"  [{p['id']}] Level {p['level']}: {p['question']}")
        return

    pool = PROBLEMS
    if args.id:
        pool = [p for p in PROBLEMS if p["id"] == args.id]
        if not pool:
            print(f"Problem {args.id} not found.")
            sys.exit(1)
    elif args.level:
        pool = [p for p in PROBLEMS if p["level"] == args.level]

    problem = random.choice(pool)
    salt = generate_salt()
    answer_hash = make_hash(salt, problem["answer"])

    block = format_commit_block(problem, salt, answer_hash)
    print(block)
    print("\n─── Paste the block above into your commit message ───")
    print("─── Do NOT include the answer in your commit message ───\n")


if __name__ == "__main__":
    main()
