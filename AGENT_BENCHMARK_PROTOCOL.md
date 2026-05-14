# Agent Benchmark Protocol

**Read this if you are an AI agent.** This file tells you exactly how to run the benchmark on yourself, validate your work, and report results.

---

## Your Mission

You will perform the **same coding tasks twice** — once on the monolith codebase, once on the microservices codebase — then validate both solutions and honestly score which was harder and why.

The goal is to generate data about whether monolithic or microservices architecture makes your job easier.

---

## Before You Start

### Step 1 — Orient yourself

Read these files in order:
1. `CLAUDE.md` — architecture overview and build commands
2. `library/monolith/src/main/java/com/benchmark/library/loan/LoanService.java` — the key cross-module pattern
3. `library/microservices/loan-service/src/main/java/com/benchmark/library/loan/LoanService.java` — the microservices equivalent (note it has no cross-module logic yet)

After reading, you should be able to answer:
- How does `LoanService` in the monolith access `BookService`?
- How would `LoanService` in the microservices need to access `BookService`?

### Step 2 — Understand the module dependency graph

```
Library Monolith:
  LoanService → injects BookService, MemberService (Java method calls)
  FineService → injects LoanService (Java method calls)
  ReservationService → injects BookService, MemberService (Java method calls)

Library Microservices:
  loan-service → would need HTTP call to book-service, member-service
  fine-service → would need HTTP call to loan-service
  reservation-service → would need HTTP call to book-service, member-service
```

---

## Task Set A — Library Domain

Run each task on **both** the monolith and the microservices. After each task, fill in the self-report template at the bottom.

---

### Task A1: Code Generation — Add Loan History with Book Details

**Monolith task**: In `library/monolith/`, add a method `getLoanHistoryWithDetails(Long memberId)` to `LoanService` that returns a list of loans for the member, with the book title and author enriched on each loan. Return a new record/class `LoanDetail` that contains the `Loan` plus `bookTitle` and `bookAuthor` fields. Add a corresponding endpoint `GET /api/v1/loans/member/{memberId}/history` to `LoanController`.

**Microservices task**: Do the same thing in `library/microservices/loan-service/`. The loan-service cannot directly call BookService — it must use `RestTemplate` or `WebClient` to call book-service at `http://book-service:8081/api/v1/books/{bookId}`.

**Validation** (run after each implementation):
```bash
# Monolith: build must succeed
cd library/monolith && mvn compile -q
# Expected: BUILD SUCCESS

# Microservices: build must succeed
cd library/microservices/loan-service && mvn compile -q
# Expected: BUILD SUCCESS
```

**Self-check questions** (answer honestly):
- How many files did you need to read to understand the monolith context?
- How many files did you need to read to understand the microservices context?
- Did you need to handle partial failure (what if book-service is down) in the microservices version?
- Which implementation took more decision-making?

---

### Task A2: Bug Fix — Transaction Boundary Bug

**Context**: There is a latent bug in `LoanService.createLoan()`. If `memberService.validateActiveMember()` is called AFTER `bookService.decrementAvailableCopies()`, and the member check fails, the book copy count gets decremented but no loan is created — inventory is permanently off by one.

**Monolith task**: Look at `library/monolith/src/main/java/com/benchmark/library/loan/LoanService.java`. Check the order of operations in `createLoan()`. If the bug is present, fix it so all validations happen before any state changes.

**Microservices task**: In `library/microservices/loan-service/`, the equivalent bug is that an HTTP call to book-service to decrement copies might succeed before the HTTP call to member-service to validate the member. Design and implement a fix. Consider: can you roll back the book decrement if the member check fails? What approach will you use?

**Validation**:
```bash
# Monolith: verify createLoan validates member BEFORE decrementing book copies
grep -n "validateActiveMember\|decrementAvailableCopies" \
  library/monolith/src/main/java/com/benchmark/library/loan/LoanService.java
# validateActiveMember line number must be LESS THAN decrementAvailableCopies line number

# Microservices: just verify it compiles
cd library/microservices/loan-service && mvn compile -q
```

**Self-check questions**:
- In the monolith, how many lines of code did the fix require?
- In the microservices version, how many lines did the fix require?
- Did the microservices fix involve any compensating logic or retry handling?
- Could you guarantee atomicity in the microservices version? Why or why not?

---

### Task A3: Code Comprehension — Trace and Explain

**Monolith task**: Trace the exact execution path when `POST /api/v1/fines/issue?loanId=1` is called. Name every method called in order, which class it's in, and whether it reads from or writes to the database.

**Microservices task**: Describe how you would trace the equivalent operation in the microservices architecture. Which services would be involved? How would you follow the call chain across service boundaries?

**Self-check questions**:
- How many files did you open to complete the monolith trace?
- Could you answer the microservices trace with the same confidence? Why or why not?
- What information is missing in the microservices that would be needed for a complete trace?

---

## Task Set B — Healthcare Domain

---

### Task B1: Code Generation — Doctor Workload Report

**Monolith task**: In `healthcare/monolith/`, add a method `getDoctorWorkloadSummary(Long doctorId)` to `AppointmentService` that returns a summary containing: doctor name (from DoctorService), total appointments, appointments by status (SCHEDULED/COMPLETED/CANCELLED/NO_SHOW), and pending billings count (from BillingService). Return a new record `DoctorWorkloadSummary`. Add endpoint `GET /api/v1/appointments/doctor/{doctorId}/workload`.

**Microservices task**: Implement the same feature in `healthcare/microservices/appointment-service/`. This requires HTTP calls to doctor-service and billing-service. Handle the case where either service is unavailable.

**Validation**:
```bash
cd healthcare/monolith && mvn compile -q
cd healthcare/microservices/appointment-service && mvn compile -q
```

**Self-check questions**:
- In the monolith, how many `@Autowired`/`@RequiredArgsConstructor` injections did you add?
- In the microservices, how many HTTP client configurations did you add?
- Which version required more boilerplate code unrelated to the business logic?

---

### Task B2: Bug Fix — Missing Cascade on Appointment Cancellation

**Task**: When an appointment is cancelled (`PATCH /api/v1/appointments/{id}/cancel`), any associated medical records and billings should also be cancelled/marked invalid. Currently, `AppointmentService.cancelAppointment()` only updates the appointment status and does nothing about medical records or billing.

**Monolith task**: Fix `AppointmentService.cancelAppointment()` in `healthcare/monolith/` to also cancel associated billings (via `BillingService`) and optionally flag medical records (via `MedicalRecordService`). All changes should happen in one `@Transactional` call.

**Microservices task**: Fix `AppointmentService.cancelAppointment()` in `healthcare/microservices/appointment-service/`. You must now HTTP-call billing-service and medicalrecord-service. Consider: what happens if the billing-service call succeeds but the medicalrecord-service call fails?

**Validation**:
```bash
cd healthcare/monolith && mvn compile -q
cd healthcare/microservices/appointment-service && mvn compile -q
```

**Self-check questions**:
- In the monolith, is your fix guaranteed to be all-or-nothing (atomic)?
- In the microservices, can you make the same atomicity guarantee? If not, what did you do instead?
- How many lines of exception handling did each version require?

---

## Task Set C — Insurance Domain

---

### Task C1: End-to-End Feature — Auto-Settlement on Claim Approval

**Monolith task**: Modify `ClaimService.approveClaim()` in `insurance/monolith/` so that when a claim is approved, it automatically creates a `Settlement` record (via `SettlementService`) with the approved amount and status PROCESSING. The claim approval and settlement creation must be atomic — if settlement creation fails, the approval rolls back.

**Microservices task**: Implement the same feature in `insurance/microservices/claim-service/`. After marking the claim as approved, you must call settlement-service via HTTP. If the settlement call fails, decide: do you roll back the approval or leave it approved without a settlement?

**Validation**:
```bash
cd insurance/monolith && mvn compile -q
cd insurance/microservices/claim-service && mvn compile -q
```

**Self-check questions**:
- How did you enforce atomicity in the monolith version?
- What consistency guarantee does your microservices version provide?
- Which version would you trust more in production? Why?

---

### Task C2: Comprehension — Policy Ownership Verification

**Task**: Explain precisely why the check `policy.getCustomerId().equals(customerId)` in `ClaimService.fileClaim()` works in the monolith but would require an extra HTTP call in microservices.

**Expected answer for monolith**: `ClaimService` injects `PolicyService` directly, calls `policyService.getPolicyById(policyId)` which returns the full `Policy` object in the same JVM. Accessing `policy.getCustomerId()` is a field read — zero network calls.

**Expected answer for microservices**: `claim-service` doesn't have the `Policy` class. It would need to HTTP GET `policy-service/api/v1/policies/{policyId}`, deserialize the JSON response into a DTO, then extract `customerId`. That's 1 additional HTTP roundtrip just for validation.

**Self-check**: Did you identify both the monolith path and the microservices path correctly?

---

## How to Score Your Own Performance

After completing all tasks, fill out this self-assessment:

```
=== AGENT SELF-ASSESSMENT REPORT ===

Agent ID / Run ID: _______________
Date: _______________

--- TASK COMPLETION ---

Task A1 (Loan History):
  Monolith completed:       YES / NO / PARTIAL
  Microservices completed:  YES / NO / PARTIAL
  Monolith files read:      ___ files
  Microservices files read: ___ files
  Monolith extra boilerplate lines (HTTP client, error handling): 0
  Microservices extra boilerplate lines:  ___

Task A2 (Transaction Bug Fix):
  Monolith fix correct:     YES / NO
  Microservices fix atomic: YES / NO / BEST_EFFORT
  Monolith fix lines:       ___
  Microservices fix lines:  ___

Task A3 (Trace):
  Monolith trace complete:      YES / NO / PARTIAL
  Microservices trace complete: YES / NO / PARTIAL

Task B1 (Doctor Workload):
  Monolith completed:       YES / NO / PARTIAL
  Microservices completed:  YES / NO / PARTIAL

Task B2 (Cascade Cancel):
  Monolith atomic:          YES / NO
  Microservices atomic:     YES / NO / BEST_EFFORT

Task C1 (Auto-Settlement):
  Monolith atomic:          YES / NO
  Microservices consistent: YES / NO / BEST_EFFORT

Task C2 (Policy Ownership):
  Both paths explained correctly: YES / NO / PARTIAL

--- DIFFICULTY RATINGS (1=easy, 5=very hard) ---

Task A1:  Monolith: _/5   Microservices: _/5
Task A2:  Monolith: _/5   Microservices: _/5
Task A3:  Monolith: _/5   Microservices: _/5
Task B1:  Monolith: _/5   Microservices: _/5
Task B2:  Monolith: _/5   Microservices: _/5
Task C1:  Monolith: _/5   Microservices: _/5

Average difficulty — Monolith:      _/5
Average difficulty — Microservices: _/5

--- CONTEXT USAGE ---

Total files read for all monolith tasks:      ___
Total files read for all microservices tasks: ___

--- ATOMICITY SCORE ---
(Tasks where you could guarantee all-or-nothing behavior)

Monolith atomic tasks:      _/4
Microservices atomic tasks: _/4

--- FREE TEXT ---

Which architecture let you reason about cross-module behavior more easily?
_______________________________________________

Which task showed the biggest difference between architectures?
_______________________________________________

What was hardest about the microservices tasks that wasn't hard in the monolith?
_______________________________________________

=== END REPORT ===
```

---

## Validation Commands Reference

Run these after each implementation to verify your code compiles:

```bash
# Library Monolith
cd C:\work\modulithAIAgent\ModulithBenchMark\library\monolith
mvn compile -q && echo "PASS" || echo "FAIL"

# Healthcare Monolith
cd C:\work\modulithAIAgent\ModulithBenchMark\healthcare\monolith
mvn compile -q && echo "PASS" || echo "FAIL"

# Insurance Monolith
cd C:\work\modulithAIAgent\ModulithBenchMark\insurance\monolith
mvn compile -q && echo "PASS" || echo "FAIL"

# Library Microservices - loan-service
cd C:\work\modulithAIAgent\ModulithBenchMark\library\microservices\loan-service
mvn compile -q && echo "PASS" || echo "FAIL"

# Healthcare Microservices - appointment-service
cd C:\work\modulithAIAgent\ModulithBenchMark\healthcare\microservices\appointment-service
mvn compile -q && echo "PASS" || echo "FAIL"

# Insurance Microservices - claim-service
cd C:\work\modulithAIAgent\ModulithBenchMark\insurance\microservices\claim-service
mvn compile -q && echo "PASS" || echo "FAIL"
```

## Runtime Validation (if services are running)

```bash
BASE_LIB=http://localhost:8080
BASE_HC=http://localhost:8081
BASE_INS=http://localhost:8082

# Verify monoliths are healthy
curl -s $BASE_LIB/actuator/health | python -c "import sys,json; d=json.load(sys.stdin); print('Library: ' + d['status'])"
curl -s $BASE_HC/actuator/health  | python -c "import sys,json; d=json.load(sys.stdin); print('Healthcare: ' + d['status'])"
curl -s $BASE_INS/actuator/health | python -c "import sys,json; d=json.load(sys.stdin); print('Insurance: ' + d['status'])"

# Test a cross-module operation — create loan (validates member + book atomically)
# First create book and member, then loan
curl -s -X POST $BASE_LIB/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Book","isbn":"TEST-001","author":"Test Author","totalCopies":2}' > /tmp/book.json

curl -s -X POST $BASE_LIB/api/v1/members \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@benchmark.com"}' > /tmp/member.json

BOOK_ID=$(python -c "import json; print(json.load(open('/tmp/book.json'))['id'])")
MEMBER_ID=$(python -c "import json; print(json.load(open('/tmp/member.json'))['id'])")

curl -s -X POST "$BASE_LIB/api/v1/loans?bookId=$BOOK_ID&memberId=$MEMBER_ID" | python -c "import sys,json; d=json.load(sys.stdin); print('Loan created, status:', d.get('status','ERROR'), 'bookId:', d.get('bookId'))"

# Verify available copies decreased (cross-module atomicity check)
curl -s $BASE_LIB/api/v1/books/$BOOK_ID | python -c "import sys,json; d=json.load(sys.stdin); print('Available copies:', d['availableCopies'], '(expected 1 if was 2)')"
```

---

## What Good Results Look Like

If the hypothesis is correct, you should observe:

| Metric | Monolith | Microservices | Expected gap |
|--------|----------|---------------|-------------|
| Files read per cross-module task | 2–4 files | 6–12 files | Monolith needs fewer |
| Extra boilerplate lines | 0–5 | 20–60 | Monolith has less |
| Atomic guarantee achievable | Always | Rarely | Monolith always wins |
| Confidence in trace | High | Medium | Monolith clearer |
| Difficulty rating | 1–2/5 | 3–4/5 | Monolith easier |

If you observe the opposite — microservices tasks were easier — that is equally valid data. Report it honestly.
