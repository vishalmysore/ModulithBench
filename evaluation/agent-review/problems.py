"""
Math problem bank for agent-to-agent review challenges.

Answers are stored here and NEVER exposed in commit messages.
The commit only contains the question text + sha256(salt + str(answer)).
A reviewing agent must solve independently and validate with validate_solution.py.
"""

PROBLEMS = [
    # ─── Level 1: Arithmetic, basic algebra ──────────────────────────────────
    {
        "id": "P001", "level": 1,
        "question": "What is the sum of all integers from 1 to 100?",
        "answer": 5050,
        "explanation": "Gauss formula: 100*101/2 = 5050"
    },
    {
        "id": "P002", "level": 1,
        "question": "What is the result of: 2^10 - 2^8 + 2^4?",
        "answer": 784,
        "explanation": "1024 - 256 + 16 = 784"
    },
    {
        "id": "P003", "level": 1,
        "question": "If f(x) = 3x² - 2x + 7, what is f(5)?",
        "answer": 72,
        "explanation": "3*25 - 10 + 7 = 72"
    },
    {
        "id": "P004", "level": 1,
        "question": "What is the sum of the first 8 perfect squares: 1² + 2² + ... + 8²?",
        "answer": 204,
        "explanation": "1+4+9+16+25+36+49+64 = 204"
    },
    {
        "id": "P005", "level": 1,
        "question": "What is 17 × 23 + 11 × 7 - 44?",
        "answer": 424,
        "explanation": "391 + 77 - 44 = 424"
    },
    {
        "id": "P006", "level": 1,
        "question": "A sequence starts: 3, 5, 9, 17, 33, ... (each term = 2 × previous - 1). What is the 8th term?",
        "answer": 257,
        "explanation": "a(n)=2*a(n-1)-1: 3,5,9,17,33,65,129,257"
    },
    # ─── Level 2: Number theory, sequences ───────────────────────────────────
    {
        "id": "P007", "level": 2,
        "question": "What is the 15th prime number?",
        "answer": 47,
        "explanation": "2,3,5,7,11,13,17,19,23,29,31,37,41,43,47"
    },
    {
        "id": "P008", "level": 2,
        "question": "What is the sum of the first 10 Fibonacci numbers (starting 1, 1, 2, 3, ...)?",
        "answer": 143,
        "explanation": "1+1+2+3+5+8+13+21+34+55 = 143"
    },
    {
        "id": "P009", "level": 2,
        "question": "What is 123456789 mod 97?",
        "answer": 39,
        "explanation": "97*1272750=123456750; 123456789-123456750=39"
    },
    {
        "id": "P010", "level": 2,
        "question": "How many prime factors does 360 have, counting multiplicity?",
        "answer": 6,
        "explanation": "360 = 2³ × 3² × 5¹ → 3+2+1 = 6"
    },
    {
        "id": "P011", "level": 2,
        "question": "What is the 20th triangular number T(20) = 20×21/2?",
        "answer": 210,
        "explanation": "20*21/2 = 210"
    },
    {
        "id": "P012", "level": 2,
        "question": "What is the GCD of 1848 and 3564?",
        "answer": 132,
        "explanation": "3564=1×1848+1716; 1848=1×1716+132; 1716=13×132+0 → GCD=132"
    },
    {
        "id": "P013", "level": 2,
        "question": "What is the sum of all two-digit prime numbers whose digits sum to 10?",
        "answer": 129,
        "explanation": "19(1+9=10,prime), 37(3+7=10,prime), 73(7+3=10,prime). Sum=19+37+73=129"
    },
    # ─── Level 3: Combinatorics, linear algebra ───────────────────────────────
    {
        "id": "P014", "level": 3,
        "question": "In how many ways can 5 people be seated in a row if 2 specific people must always sit adjacent?",
        "answer": 48,
        "explanation": "Treat pair as one unit: 4! × 2 = 24 × 2 = 48"
    },
    {
        "id": "P015", "level": 3,
        "question": "What is the determinant of the matrix [[3,1,2],[0,4,1],[0,0,5]]?",
        "answer": 60,
        "explanation": "Upper triangular: det = 3×4×5 = 60"
    },
    {
        "id": "P016", "level": 3,
        "question": "How many distinct binary strings of length 8 contain exactly three 1s?",
        "answer": 56,
        "explanation": "C(8,3) = 56"
    },
    {
        "id": "P017", "level": 3,
        "question": "A frog climbs a 20-step staircase taking 1 or 2 steps at a time. How many distinct paths reach the top?",
        "answer": 10946,
        "explanation": "ways(n)=ways(n-1)+ways(n-2), ways(1)=1, ways(2)=2. ways(20)=10946 (21st Fibonacci number)"
    },
    {
        "id": "P018", "level": 3,
        "question": "How many integers from 1 to 500 are coprime to 500?",
        "answer": 200,
        "explanation": "Euler totient: 500=2²×5³. φ(500)=500×(1-½)×(1-⅕)=200"
    },
    {
        "id": "P019", "level": 3,
        "question": "What is the smallest n such that n! ends in exactly 12 trailing zeros?",
        "answer": 50,
        "explanation": "Trailing zeros = Σfloor(50/5^k): floor(50/5)+floor(50/25)=10+2=12. n=49 gives 9+1=10. So n=50."
    },
    {
        "id": "P020", "level": 3,
        "question": "What is the sum of all integers from 1 to 1000 divisible by 3 or 5 but not both?",
        "answer": 201003,
        "explanation": "div3_only=166833-33165=133668. div5_only=100500-33165=67335. Total=201003"
    },
]
