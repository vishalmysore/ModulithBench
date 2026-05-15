#!/usr/bin/env python3
"""
Validate a reviewing agent's answer to the math challenge in a commit message.

The reviewing agent:
  1. Reads the commit message to get SALT and ANSWER_HASH
  2. Solves the math problem
  3. Runs this script to verify their answer before posting a review

Usage:
    python validate_solution.py --hash <ANSWER_HASH> --salt <SALT> --answer <YOUR_ANSWER>

Example:
    python validate_solution.py \\
      --hash a3f8c2... \\
      --salt d4e1b3f2 \\
      --answer 47

Exit codes:
    0 = correct answer
    1 = wrong answer
    2 = usage error
"""

import argparse
import hashlib
import sys


def make_hash(salt: str, answer: int) -> str:
    payload = f"{salt}:{answer}"
    return hashlib.sha256(payload.encode()).hexdigest()


def main():
    parser = argparse.ArgumentParser(
        description="Validate your answer to a benchmark review math challenge"
    )
    parser.add_argument("--hash", required=True, dest="answer_hash",
                        help="ANSWER_HASH from the commit message")
    parser.add_argument("--salt", required=True,
                        help="SALT from the commit message")
    parser.add_argument("--answer", required=True,
                        help="Your integer answer to the math problem")
    args = parser.parse_args()

    try:
        answer_int = int(args.answer.strip().replace(",", ""))
    except ValueError:
        print(f"ERROR: answer must be an integer, got: {args.answer}")
        sys.exit(2)

    computed = make_hash(args.salt, answer_int)

    if computed == args.answer_hash:
        print("✓ CORRECT — Your answer matches the challenge hash.")
        print(f"  Answer: {answer_int}")
        print(f"  You may now submit your review.")
        sys.exit(0)
    else:
        print("✗ INCORRECT — Your answer does not match.")
        print(f"  You answered: {answer_int}")
        print(f"  Re-check your working and try again.")
        sys.exit(1)


if __name__ == "__main__":
    main()
