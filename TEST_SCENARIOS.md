# AI Agent Test Scenarios - Library Management System

These scenarios are designed to be **identical across monolith and microservices** versions. AI agents are tested on the same tasks, and performance is compared.

---

## TEST CATEGORY 1: CODE GENERATION & FEATURE ADDITION (40% weight)

### Scenario 1.1: Reservation Cancellation Feature
**Difficulty**: Medium
**Estimated Complexity**: 2-3 service modifications

#### Task Description
```
Add a reservation cancellation feature to the library system.

REQUIREMENTS:
1. Allow members to cancel their existing reservations
2. When a reservation is cancelled:
   - Change its status to CANCELLED
   - Automatically move the next person in queue to position 1
   - If the book becomes available and someone is next in queue, 
     notify them
3. Add validation:
   - Only PENDING reservations can be cancelled
   - Only the member who made the reservation can cancel it
4. Create a REST endpoint: POST /api/v1/reservations/{id}/cancel
5. Add unit tests for the cancellation logic
6. Ensure the system remains consistent (no race conditions)

ACCEPTANCE CRITERIA:
- Feature compiles and starts without errors
- Tests pass for normal cancellation and edge cases
- Endpoint is callable and returns correct status codes
- Queue positions are correctly updated
- Next person in queue is notified (or prepared to be)
```

#### Scoring Rubric

| Aspect | Perfect (100) | Good (80) | Partial (60) | Poor (40) | Failed (0) |
|--------|---|---|---|---|---|
| **Correctness** | All tests pass, no runtime errors | 1-2 minor bugs fixable in <1 interaction | Works but has edge case bugs | Compiles but doesn't work | Won't compile |
| **Completeness** | Service, Controller, Tests all present | Missing 1 minor component | Missing tests or validation | Missing integration points | Major components missing |
| **Architecture** | Follows project patterns, proper separation | Minor pattern violations | Some architectural issues | Doesn't follow patterns | Completely off-pattern |
| **Transaction Safety (Monolith)** | Atomic, all-or-nothing | @Transactional present but incomplete | No transaction handling | N/A | N/A |
| **Service Communication (Microservices)** | Proper HTTP/event handling | Communication works but inefficient | Partial communication | Network-blind | N/A |

#### Scoring Details
For MONOLITH:
- ✅ Correctness (50%): Logic works atomically - does queue update happen correctly? Do tests pass?
- ✅ Completeness (30%): ReservationService, ReservationController, integration with BookService
- ✅ Pattern Adherence (20%): Uses @Transactional, @Autowired, proper entity relationships

For MICROSERVICES:
- ✅ Correctness (40%): Logic works across services - does queue update trigger book service? Do events publish?
- ✅ Completeness (40%): ReservationService, ReservationController, BookServiceClient, Event publisher
- ✅ Communication (20%): Correct HTTP calls or message publishing

---

### Scenario 1.2: Book Recommendation System
**Difficulty**: Medium-Hard
**Estimated Complexity**: 2-3 service modifications

#### Task Description
```
Add a simple recommendation system for books.

REQUIREMENTS:
1. Create a recommendation algorithm that suggests books based on:
   - Books in the same genre as books the member has previously borrowed
   - Books by the same author as books the member has previously borrowed
2. Create a new Recommendation entity with:
   - recommendedFor (Member)
   - recommendedBook (Book)
   - reason (enum: SAME_GENRE, SAME_AUTHOR)
   - createdAt
3. Add a RecommendationService that:
   - Generates recommendations for a member based on their borrowing history
   - Returns top 5 recommendations
   - Doesn't recommend books already borrowed or reserved by the member
4. Add endpoint: GET /api/v1/recommendations/member/{memberId}
5. Add unit tests

ACCEPTANCE CRITERIA:
- Feature compiles and starts without errors
- Can generate recommendations without errors
- Recommendations are relevant (same genre or author)
- No duplicate or already-borrowed books are recommended
- Tests pass with various member borrowing histories
```

#### Key Challenge
- **Monolith**: Direct access to all historical data via repositories
- **Microservices**: Must call LoanService to get history, call BookService for details

---

## TEST CATEGORY 2: BUG DETECTION & FIXING (35% weight)

### Bug 2.1: Fine Calculation Error (PLANTED)
**Difficulty**: Medium
**Estimated Debug Time**: 3-5 interactions

#### Bug Description
```
SYMPTOM OBSERVED BY MEMBER:
"I returned my book 5 days late with a daily fine rate of $2.00.
The system is charging me $12.00 instead of $10.00"

ACTUAL BUG (not told to agent):
In LoanService.returnBook(), the fine calculation uses:
  long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
  BigDecimal fineAmount = dailyRate.multiply(BigDecimal.valueOf(daysOverdue));

Problem: The calculation includes both endpoints instead of days between.
Example: Due 2024-01-10, Returned 2024-01-15 → 5 days but counts as 6

ROOT CAUSE:
ChronoUnit.DAYS.between() counts from start of first day to start of last day.
Example: Jan 10 8:00 AM to Jan 15 3:00 PM = 5 days (correct)
But when both are at midnight: Jan 10 00:00 to Jan 15 00:00 = 5 days (start to start)
However, the code should count 2024-01-11, 12, 13, 14, 15 = 5 days inclusive

FIX:
long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), loan.getReturnDate());
// For inclusive counting:
if (daysOverdue > 0) {
  daysOverdue++; // OR use proper date arithmetic
}

OR better:
long daysOverdue = LocalDate.now().toEpochDay() - loan.getDueDate().toEpochDay();
```

#### Evaluation

| Aspect | Points |
|--------|--------|
| **Root Cause Found** | Time to identify LoanService.returnBook() fine calculation: 40 pts |
| **Correct Fix** | Proper date arithmetic applied: 40 pts |
| **Fix Quality** | Minimal, elegant fix without side effects: 20 pts |

**Scoring**:
- Found root cause in 1-2 interactions: 100 points
- Found root cause in 3-4 interactions: 80 points
- Found root cause in 5-6 interactions: 60 points
- Found root cause in 7+ interactions: 40 points
- Couldn't identify the bug: 0 points

---

### Bug 2.2: Reservation Queue Corruption (PLANTED)
**Difficulty**: Hard
**Estimated Debug Time**: 5-8 interactions

#### Bug Description
```
SYMPTOM OBSERVED:
"When I cancel my reservation, the queue positions don't update properly.
Member 'Alice' who was #2 should become #1, but she's still showing #2.
Even worse, when she tries to borrow, the book isn't available to her!"

ACTUAL BUG (not told to agent):
In ReservationService.cancelReservation():

  public void cancelReservation(Long reservationId) {
    Reservation res = repo.findById(reservationId);
    res.setStatus(CANCELLED);
    repo.save(res); // ← BUG CHECKPOINT 1: Not deleting, just marking cancelled
    
    // Get remaining reservations and update positions
    List<Reservation> queue = repo.findNextInQueueByBookId(res.getBook().getId());
    for (int i = 0; i < queue.size(); i++) {
      Reservation r = queue.get(i);
      r.setPositionInQueue(i + 1); // ← BUG CHECKPOINT 2: Only updates in-memory
      // MISSING: r.save() ← ACTUAL BUG: Never saves the updated positions!
    }
  }

CONSEQUENCES:
1. Cancelled reservation still marked CANCELLED but not truly removed
2. Other reservations have new position in memory but NEVER saved to database
3. Query findNextInQueueByBookId() still finds old positions
4. Queue is logically broken but silently
```

#### Evaluation

| Aspect | Points |
|--------|--------|
| **Identify Missing repo.save()** | Core issue, hardest part: 50 pts |
| **Identify Collection.save() needed** | Must recognize batch update pattern: 30 pts |
| **Complete Fix** | Add repo.saveAll() and verify: 20 pts |

**Scoring**:
- Identified complete issue (missing save + logic error): 100 points
- Identified missing save but not fully: 70 points
- Found surface-level issue, missed root cause: 40 points
- Could not identify issue: 0 points

---

## TEST CATEGORY 3: CODEBASE COMPREHENSION & NAVIGATION (25% weight)

### Comprehension 3.1: Cross-Module Book Borrowing Flow
**Difficulty**: Medium
**Estimated Time**: 3-5 minutes of reading

#### Task
```
"A member named Alice clicks 'Borrow' on book 'Dune'. The system 
currently has 3 copies, all checked out. Your task:

Explain EXACTLY what happens from her perspective:
1. Where does the request go? (which controller/endpoint)
2. What validation happens? (Show relevant code)
3. What objects are created/modified?
4. What error does she see if she can't borrow?
5. If she CAN'T borrow but the book becomes available later, 
   what's the flow for her to get it?

Show me code snippets, class names, and data flow."
```

#### Perfect Answer Should Include

**MONOLITH**:
```
1. POST /api/v1/loans/borrow (LoanController.borrowBook)
2. LoanController calls LoanService.borrowBook(memberId, bookId)
3. LoanService:
   - memberRepository.findById(memberId) 
   - Checks member.status == ACTIVE
   - countActiveLoansByMemberId() < maxActiveBorrows
   - bookRepository.findById(bookId)
   - Checks book.availableCopies > 0 ← Alice can't borrow (0 copies)
4. Returns error: "Book is not available for borrowing"
5. If Alice wants to be notified:
   - POST /api/v1/reservations (ReservationController)
   - ReservationService.createReservation(memberId, bookId)
   - Gets next queue position: findNextInQueueByBookId()
   - Creates Reservation with status=PENDING
   - When book returned: Loan.returnBook → book.availableCopies++
   - Can trigger ReservationService to notify Alice
```

**MICROSERVICES**:
```
1. POST http://gateway:8000/api/v1/loans/borrow (LoanController in loan-service)
2. LoanController calls LoanService.borrowBook()
3. LoanService:
   - Calls memberServiceClient.validateMember() [HTTP GET to member-service:8080]
   - Calls bookServiceClient.checkAvailability() [HTTP GET to book-service:8081]
   - If book not available: returns error
   - If available: calls bookServiceClient.reserveCopy() [HTTP PUT to book-service:8081]
   - Creates Loan in loan-service database
   - Publishes LoanCreated event to RabbitMQ
4. Error: "Book not available"
5. For reservation:
   - POST http://gateway:8000/api/v1/reservations (ReservationController in reservation-service)
   - ReservationService calls bookServiceClient.getQueuePosition()
   - Creates Reservation in reservation-service database
   - Publishes ReservationCreated event
   - When book returned: loan-service publishes BookReturned event
   - reservation-service listens, notifies Alice
```

#### Scoring

| Aspect | Points |
|--------|--------|
| **End-to-End Flow** | Correctly traces request from UI to response: 40 pts |
| **Service Identification** | Finds LoanService, LoanController, dependencies: 30 pts |
| **Error Path** | Explains validation and error handling: 20 pts |
| **Code Specificity** | Cites actual class/method names: 10 pts |

**Scoring**:
- **Excellent (100)**: Complete accurate flow, all relevant code identified
- **Good (80)**: Mostly correct, minor gaps in detail
- **Adequate (60)**: Basic understanding, some components missing
- **Poor (40)**: Significant gaps, misses key interactions
- **Failed (0)**: Fundamental misunderstanding

---

### Comprehension 3.2: Fine Generation and Payment
**Difficulty**: Medium-Hard
**Estimated Time**: 5-7 minutes

#### Task
```
"Explain what happens when an overdue book is returned and a fine 
is generated. Include:

1. What triggers fine generation?
2. Where is the Fine entity stored?
3. How is the fine amount calculated?
4. What prevents duplicate fines for the same loan?
5. What happens when the member pays the fine?
6. Can a fine be waived? Where would that logic live?

Show code locations and explain the design choices."
```

#### MONOLITH Perfect Answer

```
1. LoanService.returnBook(loanId):
   - if (loan.getReturnDate().isAfter(loan.getDueDate()))
     → trigger fine generation

2. Fine table in shared database
   - Fine.java entity at: com.benchmark.library.fine.Fine
   - FineRepository.java at: com.benchmark.library.fine.FineRepository

3. Fine amount calculation (in LoanService.returnBook):
   - long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate)
   - BigDecimal fineAmount = book.getDailyFineAmount().multiply(daysOverdue)
   - Created as: new Fine with amount, member, loan, generatedDate

4. Duplicate prevention:
   - Foreign key constraint: FineRepository.findByLoanId(loanId)
   - CHECK constraint could add: UNIQUE(loan_id, status='PENDING')
   - But currently, relies on business logic: only call generateFine() once per return

5. Payment flow (in FineService.payFine(fineId)):
   - Find fine by ID
   - Check status == PENDING
   - Set status = PAID, paidDate = today
   - Save to FineRepository

6. Waiver logic:
   - Would be in FineService.waiveFine(fineId)
   - Check permissions (manager role)
   - Set status = WAIVED, reason text
   - Save with AuditLog to track who waived and why
```

#### MICROSERVICES Perfect Answer

```
1. Trigger: loan-service publishes BookReturned event (after returnBook)
   - RabbitMQ topic: library.books.returned
   - Message includes: loanId, memberId, bookId, actualReturnDate

2. fine-service listens to BookReturned event:
   - Consumes message in FineEventListener
   - Calculates days overdue
   - Creates Fine in fine-service database

3. Fine entity:
   - Fine.java in fine-service (separate from loan-service!)
   - Stored in fine-db (separate PostgreSQL database)
   - FineRepository uses fine-db datasource

4. Amount calculation (in fine-service):
   - Calls bookServiceClient.getBook(bookId) [HTTP GET to book-service]
   - Gets dailyFineAmount from response
   - Calculates: daysOverdue * dailyFineAmount
   - Creates Fine in fine-db

5. Duplicate prevention:
   - Needs Idempotency ID on RabbitMQ message
   - FineService.findByLoanId(loanId) to prevent re-creation
   - Problem: Must eventually reconcile with loan-service's view of reality

6. Payment flow (in FineController):
   - POST /api/v1/fines/{id}/pay calls FineService.payFine()
   - Update status = PAID
   - Publish FinePayment event
   - loan-service listens to update its records (eventual consistency)

7. Waiver logic:
   - In FineService.waiveFine()
   - Publish FineWaivedEvent for audit trail
   - Other services react asynchronously
```

---

## Test Execution Template

### For Each Scenario, Evaluate:

```json
{
  "project": "library-system-monolith|library-system-microservices",
  "test_scenario": "scenario_1.1_reservation_cancellation",
  "test_category": "code_generation|bug_fixing|comprehension",
  "timestamp": "2026-05-14T10:30:00Z",
  "metrics": {
    "score_out_of_100": 85,
    "code_generation_accuracy": 90,
    "code_completeness": 80,
    "pattern_adherence": 85,
    "interactions_required": 3,
    "time_spent_seconds": 420,
    "tokens_used": 4250
  },
  "qualitative_notes": "Implementation was correct and complete. Minor issue with transaction boundaries in microservices version. Test compilation and execution passed.",
  "comparison": {
    "monolith_advantage": true,
    "reason": "Service interactions were simpler to identify and modify in single codebase"
  }
}
```

---

## Summary

**3 Categories × 2 Scenarios = 6 primary test scenarios**
- Code Generation (2): Reservation Cancellation, Recommendations
- Bug Fixing (2): Fine Calculation, Queue Corruption
- Comprehension (2): Borrowing Flow, Fine Management

**Key Insight for Benchmark**: 
Each scenario is designed to show where monolith advantages appear:
- Same feature takes longer to implement in microservices
- Same bug takes longer to find across service boundaries
- Same flow takes longer to explain across network calls
