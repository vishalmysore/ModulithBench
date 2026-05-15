# Agent Benchmark Protocol

**Read this if you are an AI agent.** This file tells you exactly how to run the benchmark on yourself, validate your work, and report results.

---

## Two-Tier Evaluation System

This benchmark has two tests. You start with Test 1. If you score ≥ 80%, you proceed to Test 2.

| | Test 1 | Test 2 |
|---|---|---|
| **How scored** | Self-reported by you | Automated tools — no self-reporting |
| **What it measures** | Comprehension, intent, code quality | Behavioral correctness, boilerplate overhead, token efficiency |
| **Validation** | `mvn compile` | HTTP endpoint assertions + static analysis |
| **Result format** | `results/template.md` | `evaluation/test2/` scripts |
| **Advancement** | Score ≥ 80% → proceed to Test 2 | Passing Test 2 = benchmark complete |

**Complete Test 1 first.** Only run Test 2 if your Test 1 average is ≥ 80%.

For Test 2 instructions: `evaluation/test2/README.md`
For the tool-call log format (required for Test 2): `evaluation/test2/AGENT_LOGGING_FORMAT.md`

---

---

## IMPORTANT: Do Not Commit or Push Your Changes

**Your changes must never go to the `main` branch.**

`main` is the clean baseline that every agent starts from. If your solutions are committed to `main`, the next agent to run the benchmark will see completed code instead of blank tasks — invalidating all future benchmark results.

**How to handle your work:**

- Write your code locally and validate it compiles (`mvn compile`)
- Copy your implementations into your self-assessment report as code blocks
- When you are done, **discard all file changes** before finishing:
  ```bash
  # Revert all modified files
  git checkout -- .

  # Delete any new files you created
  git clean -fd
  ```
- Your results live in your report output, not in the repository

**Never run `git add`, `git commit`, or `git push` during this benchmark.**

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

## Task Set D — Supply Chain Domain (Advanced)

The hardest domain. 8 modules, physical-world constraints, and two scenarios that are uniquely powerful for demonstrating the monolith advantage.

---

### Task D1: The Ghost Shipment — Atomic Order Cancellation

**Context**: A customer cancels an order. At the moment of cancellation:
- The warehouse has a pick task in progress (status: PICKING)
- The carrier has been notified and has a booking (status: BOOKED)
- Inventory has 3 units reserved for this order

The cancellation must atomically: release inventory + cancel warehouse task + cancel carrier booking.
If any step fails, nothing should change.

**Read first**: `supply-chain/monolith/src/main/java/com/benchmark/supplychain/order/OrderService.java` — the `cancelOrder()` method shows the monolith implementation. Read the Javadoc explaining the Ghost Shipment.

**Monolith task**: Add a 5th step to `cancelOrder()`: also notify the `TrackingService` to mark any tracking record as CANCELLED. Verify the new step is inside the same `@Transactional`.

**Microservices task**: In `supply-chain/microservices/order-service/`, implement the cancellation saga. The order-service must call inventory-service, warehouse-service, and carrier-service via HTTP. Design your compensation strategy: if carrier-service returns 503, what happens?

**Validation**:
```bash
cd supply-chain/monolith && mvn compile -q
cd supply-chain/microservices/order-service && mvn compile -q
```

**Self-check questions**:
- In the monolith, how many lines did adding the 5th compensation step require?
- In the microservices saga, how many new lines did adding a 4th service require (new event type, consumer, compensating handler)?
- Can your microservices version guarantee that partial cancellation never happens? If not, how do you detect it?

---

### Task D2: The N+1 Report — Shipment Profitability

**Context**: Generate a "Shipment Profitability Report" for a given order. The report needs:
- Revenue (from Order module)
- Shipping cost (from Carrier module)
- Duties and taxes (from Customs module)
- Fuel cost estimate (from Route module)

**Read first**: `supply-chain/monolith/src/main/java/com/benchmark/supplychain/billing/BillingService.java` — see `generateProfitabilityReport()`. Notice it reads 4 modules in one `@Transactional(readOnly = true)`.

**Monolith task**: Extend the report to also include the current tracking status (from TrackingService). Add `trackingStatus` field to `ShipmentProfitabilityReport` and populate it.

**Microservices task**: In `supply-chain/microservices/billing-service/`, implement the same profitability report. You must call: order-service, carrier-service, customs-service, route-service via HTTP. Count how many lines of non-business-logic code you write (HTTP client setup, error handling, JSON mapping).

**Validation**:
```bash
cd supply-chain/monolith && mvn compile -q
cd supply-chain/microservices/billing-service && mvn compile -q
```

**Self-check questions**:
- Monolith: how many lines is `generateProfitabilityReport()`?
- Microservices: how many of those lines are HTTP infrastructure vs business logic?
- What happens in the microservices version if customs-service is down? Does the report fail entirely or return partial data?

---

### Task D3: Add Dispatch Notification

**Task**: When a warehouse task transitions to DISPATCHED, automatically send a notification (create a `Notification` record with `orderId`, `message`, and `timestamp`).

**Monolith task**: Add a `Notification` entity and `NotificationService` to `supply-chain/monolith/`. Modify `WarehouseService.dispatch()` to call `notificationService.createNotification()` in the same transaction.

**Microservices task**: In `supply-chain/microservices/warehouse-service/`, publish an event or make an HTTP call to a hypothetical notification-service after dispatch. Handle the case where the notification service is unavailable.

**Self-check questions**:
- In the monolith, if the notification fails to save, does the dispatch still complete?
- In the microservices version, what is your consistency model between dispatch and notification?

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

Task D1 (Ghost Shipment):
  Monolith atomic:          YES / NO
  Microservices atomic:     YES / NO / BEST_EFFORT
  Compensating logic lines added (microservices): ___

Task D2 (N+1 Profitability Report):
  Monolith completed:       YES / NO / PARTIAL
  Microservices completed:  YES / NO / PARTIAL
  Monolith HTTP calls made: 0
  Microservices HTTP calls: ___
  Microservices error handlers added: ___

Task D3 (Add Notification on Dispatch):
  Monolith completed:       YES / NO / PARTIAL
  Microservices completed:  YES / NO / PARTIAL

--- DIFFICULTY RATINGS (1=easy, 5=very hard) ---

Task A1:  Monolith: _/5   Microservices: _/5
Task A2:  Monolith: _/5   Microservices: _/5
Task A3:  Monolith: _/5   Microservices: _/5
Task B1:  Monolith: _/5   Microservices: _/5
Task B2:  Monolith: _/5   Microservices: _/5
Task C1:  Monolith: _/5   Microservices: _/5
Task D1:  Monolith: _/5   Microservices: _/5
Task D2:  Monolith: _/5   Microservices: _/5
Task D3:  Monolith: _/5   Microservices: _/5

Average difficulty — Monolith:      _/5
Average difficulty — Microservices: _/5

--- CONTEXT USAGE ---

Total files read for all monolith tasks:      ___
Total files read for all microservices tasks: ___

--- ATOMICITY SCORE ---
(Tasks where you could guarantee all-or-nothing behavior)

Monolith atomic tasks:      _/5
Microservices atomic tasks: _/5

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

## How to Submit Your Results

After completing the benchmark, submit results to the `benchmark-results` branch.
**Never commit to `main`.**

### Quick submission

```bash
# 1. Copy the results template
cp results/template.md results/runs/{your-agent-name}-$(date +%Y-%m-%d).md

# 2. Fill out the template (add your scores and observations)

# 3. Switch to results branch and submit
git fetch origin
git checkout benchmark-results 2>/dev/null || git checkout -b benchmark-results
git add results/runs/{your-agent-name}-$(date +%Y-%m-%d).md
git commit -m "Add benchmark results: {your-agent-name} $(date +%Y-%m-%d)"
git push origin benchmark-results

# 4. Return to clean main baseline
git checkout main
git checkout -- .
git clean -fd
```

See `results/SUBMIT_RESULTS.md` for full instructions and naming conventions.

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
