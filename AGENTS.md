# Agent Testing Guide — ModulithBenchMark

## What This Project Is

This is a benchmarking framework that tests whether AI agents perform better when working with **monolithic** vs **microservices** architecture. The hypothesis: monoliths give AI agents a 18–30% improvement in code generation, bug fixing, and comprehension because all logic is co-located in one codebase.

Three domains are implemented in **both** architectures:

| Domain | Monolith Port | Microservices |
|--------|--------------|---------------|
| Library | 8080 | 5 services (8081–8085) |
| Healthcare | 8081 | 7 services (8086–8092) |
| Insurance | 8082 | 7 services (8093–8099) |

---

## Architecture at a Glance

### Monolith (what you're testing)
```
library/monolith/
├── pom.xml                              ← Single Maven project
├── src/main/java/com/benchmark/library/
│   ├── LibraryApplication.java          ← One Spring Boot app
│   ├── book/        (BookService, BookController, Book, BookRepository, ...)
│   ├── member/      (MemberService calls validate member)
│   ├── loan/        (LoanService injects BookService + MemberService directly)
│   ├── fine/        (FineService injects LoanService directly)
│   └── reservation/ (ReservationService injects BookService + MemberService)
```

**Key architectural property**: Cross-module calls are **direct Java method calls** within a single `@Transactional` boundary. No HTTP, no serialization, no eventual consistency.

### Microservices (comparison target)
```
library/microservices/
├── book-service/     ← Separate Spring Boot app, port 8081
├── member-service/   ← Separate Spring Boot app, port 8082
├── loan-service/     ← Separate Spring Boot app, port 8083
├── fine-service/     ← Separate Spring Boot app, port 8084
└── reservation-service/ ← Separate Spring Boot app, port 8085
```

Cross-module operations would require HTTP calls between services.

---

## Quick Start

### Option A: Run Monolith with Docker (recommended)

```bash
# Library monolith
cd library/monolith
docker compose up -d
# App: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Health: http://localhost:8080/actuator/health

# Healthcare monolith
cd healthcare/monolith
docker compose up -d
# App: http://localhost:8081
# Swagger UI: http://localhost:8081/swagger-ui.html

# Insurance monolith
cd insurance/monolith
docker compose up -d
# App: http://localhost:8082
# Swagger UI: http://localhost:8082/swagger-ui.html
```

### Option B: Run with local PostgreSQL

1. Create databases:
```sql
CREATE DATABASE library_db;     -- user: libraryuser, pass: librarypass
CREATE DATABASE healthcare_db;  -- user: healthcareuser, pass: healthcarepass
CREATE DATABASE insurance_db;   -- user: insuranceuser, pass: insurancepass
```

2. Run each monolith:
```bash
cd library/monolith && mvn spring-boot:run
cd healthcare/monolith && mvn spring-boot:run
cd insurance/monolith && mvn spring-boot:run
```

---

## API Reference

### Library Monolith (port 8080)

#### Books
```
POST   /api/v1/books                    Create book
GET    /api/v1/books                    List all books
GET    /api/v1/books/{id}               Get book by ID
GET    /api/v1/books/available          Get available books
GET    /api/v1/books/search?title=X     Search by title
GET    /api/v1/books/search?author=X    Search by author
PUT    /api/v1/books/{id}               Update book
DELETE /api/v1/books/{id}               Delete book
```

#### Members
```
POST   /api/v1/members                  Create member
GET    /api/v1/members                  List all members
GET    /api/v1/members/{id}             Get member by ID
GET    /api/v1/members/search?name=X    Search by name
PUT    /api/v1/members/{id}             Update member
PATCH  /api/v1/members/{id}/deactivate  Deactivate member
DELETE /api/v1/members/{id}             Delete member
```

#### Loans (cross-module: validates member + decrements book copies atomically)
```
POST   /api/v1/loans?bookId=1&memberId=1          Create loan
GET    /api/v1/loans                               List all loans
GET    /api/v1/loans/{id}                          Get loan by ID
GET    /api/v1/loans/member/{memberId}             Loans by member
GET    /api/v1/loans/active                        Active loans
GET    /api/v1/loans/overdue                       Overdue loans
PATCH  /api/v1/loans/{id}/return                   Return book
POST   /api/v1/loans/mark-overdue                  Mark overdue loans
DELETE /api/v1/loans/{id}                          Delete loan
```

#### Fines (cross-module: reads loan data directly)
```
POST   /api/v1/fines/issue?loanId=1               Issue fine for overdue loan
POST   /api/v1/fines                               Create fine manually
GET    /api/v1/fines/member/{memberId}             Fines by member
GET    /api/v1/fines/member/{memberId}/total-unpaid Get unpaid total
PATCH  /api/v1/fines/{id}/pay                      Pay fine
```

#### Reservations (cross-module: validates member + book)
```
POST   /api/v1/reservations?bookId=1&memberId=1   Reserve book
GET    /api/v1/reservations/pending                Pending reservations
GET    /api/v1/reservations/member/{memberId}      By member
GET    /api/v1/reservations/book/{bookId}          By book
PATCH  /api/v1/reservations/{id}/fulfill           Fulfill reservation
PATCH  /api/v1/reservations/{id}/cancel            Cancel reservation
```

---

### Healthcare Monolith (port 8081)

#### Patients
```
POST   /api/v1/patients
GET    /api/v1/patients
GET    /api/v1/patients/{id}
GET    /api/v1/patients/search?name=X
PUT    /api/v1/patients/{id}
DELETE /api/v1/patients/{id}
```

#### Doctors
```
POST   /api/v1/doctors
GET    /api/v1/doctors
GET    /api/v1/doctors/available
GET    /api/v1/doctors/specialization/{spec}
PUT    /api/v1/doctors/{id}
```

#### Departments
```
POST   /api/v1/departments
GET    /api/v1/departments
PUT    /api/v1/departments/{id}
```

#### Appointments (cross-module: validates patient + doctor availability atomically)
```
POST   /api/v1/appointments?patientId=1&doctorId=1&scheduledAt=2026-06-01T10:00:00
GET    /api/v1/appointments
GET    /api/v1/appointments/patient/{patientId}
GET    /api/v1/appointments/doctor/{doctorId}
PATCH  /api/v1/appointments/{id}/complete
PATCH  /api/v1/appointments/{id}/cancel
```

#### Medical Records (cross-module: reads appointment to get patient+doctor IDs)
```
POST   /api/v1/medical-records/from-appointment?appointmentId=1&diagnosis=X&treatment=Y
POST   /api/v1/medical-records
GET    /api/v1/medical-records/patient/{patientId}
PUT    /api/v1/medical-records/{id}
```

#### Prescriptions (cross-module: reads medical record for patient/doctor IDs)
```
POST   /api/v1/prescriptions/from-record?medicalRecordId=1&medications=X
POST   /api/v1/prescriptions
GET    /api/v1/prescriptions/patient/{patientId}
GET    /api/v1/prescriptions/patient/{patientId}/active
```

#### Billing (cross-module: reads appointment for patient ID)
```
POST   /api/v1/billings/from-appointment?appointmentId=1&amount=150.00
GET    /api/v1/billings/patient/{patientId}
GET    /api/v1/billings/patient/{patientId}/outstanding
PATCH  /api/v1/billings/{id}/pay?amount=150.00
PATCH  /api/v1/billings/{id}/cancel
```

---

### Insurance Monolith (port 8082)

#### Customers
```
POST   /api/v1/customers
GET    /api/v1/customers
GET    /api/v1/customers/{id}
PUT    /api/v1/customers/{id}
```

#### Agents
```
POST   /api/v1/agents
GET    /api/v1/agents
GET    /api/v1/agents/active
PUT    /api/v1/agents/{id}
```

#### Policies (cross-module: validates customer + agent atomically)
```
POST   /api/v1/policies          Body: {customerId, agentId, type, startDate, endDate, premiumAmount, coverageAmount}
GET    /api/v1/policies
GET    /api/v1/policies/active
GET    /api/v1/policies/customer/{customerId}
PATCH  /api/v1/policies/{id}/activate
PATCH  /api/v1/policies/{id}/cancel
```

#### Coverages (cross-module: validates policy exists)
```
POST   /api/v1/coverages         Body: {policyId, type, description, maximumAmount, deductibleAmount}
GET    /api/v1/coverages/policy/{policyId}
PUT    /api/v1/coverages/{id}
```

#### Premiums (cross-module: reads policy for premium amount)
```
POST   /api/v1/premiums/schedule?policyId=1&frequency=MONTHLY&dueDate=2026-06-01
GET    /api/v1/premiums/policy/{policyId}
GET    /api/v1/premiums/overdue
PATCH  /api/v1/premiums/{id}/pay
POST   /api/v1/premiums/mark-overdue
```

#### Claims (cross-module: validates policy ownership by customer, single transaction)
```
POST   /api/v1/claims?policyId=1&customerId=1&type=MEDICAL&description=X&claimedAmount=5000.00
GET    /api/v1/claims
GET    /api/v1/claims/customer/{customerId}
GET    /api/v1/claims/policy/{policyId}
PATCH  /api/v1/claims/{id}/review
PATCH  /api/v1/claims/{id}/approve?approvedAmount=4500.00
PATCH  /api/v1/claims/{id}/reject?reason=X
```

#### Settlements (cross-module: reads approved amount from claim)
```
POST   /api/v1/settlements/settle-claim?claimId=1&paymentMethod=BANK_TRANSFER
GET    /api/v1/settlements/claim/{claimId}
PATCH  /api/v1/settlements/{id}/mark-paid
```

---

## Test Scenarios for Agent Evaluation

These 6 scenarios are designed to measure the 3 benchmark metrics. Run each scenario on both the monolith and the equivalent microservices, then score results.

### Scenario 1 — Code Generation: Add a Feature

**Task**: Add a "borrow history" feature to the library that returns a member's full loan history with book details enriched in the response.

**Monolith**: Agent finds `LoanService` and `BookService` in one codebase, adds `getLoanHistoryWithDetails()` method that calls `BookService.getBookById()` for each loan.

**Microservices**: Agent must understand that loan-service must HTTP-call book-service, handle Feign/RestTemplate setup, deal with error handling for partial failures.

**Measure**: Time to working code, correctness, handling of edge cases.

---

### Scenario 2 — Bug Fix: Transaction Boundary Issue

**Task**: There is a bug: when `LoanService.createLoan()` is called and book decrement succeeds but member validation fails (member is inactive), the book copy count is decremented but no loan is created — inventory is now wrong.

**Monolith**: Bug is visible in one file (`LoanService.java`). The fix is reordering validation before the decrement call. All logic is visible at once.

**Microservices**: The same bug spans loan-service + book-service. Agent must trace across HTTP boundaries, understand that the decrement HTTP call happened before the member-service validation HTTP call returned.

**Measure**: Time to identify root cause, correctness of fix.

---

### Scenario 3 — Code Comprehension: Trace a Full Business Flow

**Task**: Explain exactly what happens when `POST /api/v1/appointments?patientId=1&doctorId=1&scheduledAt=...` is called.

**Monolith**: Agent reads `AppointmentController` → `AppointmentService` → `PatientService.validatePatientExists()` + `DoctorService.validateDoctorAvailability()`. All in `healthcare/monolith/src/`.

**Microservices**: Agent must understand the call flow across 3 separate services (appointment-service, patient-service, doctor-service), each in a different directory with its own config and port.

**Measure**: Completeness of explanation, correctness, time.

---

### Scenario 4 — Code Generation: Add Validation

**Task**: Add a rule that a member cannot reserve a book they currently have on active loan.

**Monolith**: `ReservationService` already injects `BookService` and `MemberService`. Agent adds a call to `loanRepository.findByMemberIdAndBookId()` or injects `LoanService`.

**Microservices**: `reservation-service` must call `loan-service` via HTTP. Agent must add HTTP client, handle 404 vs 200, add circuit breaker concerns.

**Measure**: Quality and completeness of implementation.

---

### Scenario 5 — Bug Fix: Data Consistency

**Task**: `BillingService.createBillingForAppointment()` should fail if the appointment doesn't exist — but currently returns a billing with a null appointmentId when given an invalid appointmentId.

**Monolith**: Fix is in `BillingService.java` — the `AppointmentService.getAppointmentById()` call already throws `AppointmentNotFoundException`. Agent just needs to not catch it.

**Microservices**: `billing-service` calls `appointment-service` via HTTP. Agent must check what HTTP status code is returned, map it correctly, ensure error propagation.

**Measure**: Correctness of fix, handling of error propagation.

---

### Scenario 6 — Comprehension + Generation: End-to-End Insurance Flow

**Task**: Describe and then implement an "auto-settle" feature that, when a claim is approved, automatically creates a settlement and marks it as paid.

**Monolith**: Agent reads `ClaimService` and `SettlementService` in the same codebase, adds `@EventListener` or extends `approveClaim()` to call `settlementService.settleApprovedClaim()`.

**Microservices**: Requires understanding claim-service + settlement-service + either a message broker (RabbitMQ) or synchronous HTTP callback between them.

**Measure**: Understanding depth, implementation quality, edge case handling.

---

## Scoring Rubric

For each scenario, score the agent from 0–100 on three dimensions:

### Code Generation (weight 40%)
| Score | Criteria |
|-------|----------|
| 90–100 | Correct, complete, follows existing patterns, handles edge cases |
| 70–89  | Correct main path, minor gaps in error handling |
| 50–69  | Mostly correct but missing validation or edge cases |
| 30–49  | Partially correct, compiles with fixes |
| 0–29   | Incorrect or doesn't compile |

### Bug Fixing (weight 35%)
| Score | Criteria |
|-------|----------|
| 90–100 | Identifies root cause, fixes it correctly, explains why |
| 70–89  | Fixes the symptom correctly but incomplete root cause analysis |
| 50–69  | Partial fix — covers main case but misses edge cases |
| 30–49  | Identifies the problem but fix is wrong |
| 0–29   | Misdiagnoses or can't fix |

### Comprehension (weight 25%)
| Score | Criteria |
|-------|----------|
| 90–100 | Complete trace of execution, mentions all services/modules, correct behavior |
| 70–89  | Mostly complete, minor gaps |
| 50–69  | Gets main flow but misses cross-module interactions |
| 30–49  | Partial understanding |
| 0–29   | Fundamentally incorrect |

---

## Example API Walkthrough

### Full Library Flow (copy-paste ready)

```bash
BASE=http://localhost:8080

# 1. Create a book
curl -s -X POST $BASE/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","isbn":"978-0132350884","author":"Robert Martin","genre":"Programming","totalCopies":3}' | jq .

# 2. Create a member
curl -s -X POST $BASE/api/v1/members \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Alice","lastName":"Smith","email":"alice@example.com","phone":"555-0100"}' | jq .

# 3. Create a loan (cross-module: validates member, decrements book copies atomically)
curl -s -X POST "$BASE/api/v1/loans?bookId=1&memberId=1" | jq .

# 4. Check book availability decreased
curl -s $BASE/api/v1/books/1 | jq '.availableCopies'

# 5. Return the book
curl -s -X PATCH $BASE/api/v1/loans/1/return | jq .

# 6. Verify copies restored
curl -s $BASE/api/v1/books/1 | jq '.availableCopies'
```

### Full Insurance Flow

```bash
BASE=http://localhost:8082

# 1. Create customer
curl -s -X POST $BASE/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Bob","lastName":"Jones","email":"bob@example.com","nationalId":"ID-12345"}' | jq .

# 2. Create agent
curl -s -X POST $BASE/api/v1/agents \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Carol","lastName":"White","email":"carol@example.com","licenseNumber":"LIC-001"}' | jq .

# 3. Create policy (cross-module: validates customer + agent atomically)
curl -s -X POST $BASE/api/v1/policies \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"agentId":1,"type":"HEALTH","startDate":"2026-01-01","endDate":"2026-12-31","premiumAmount":200.00,"coverageAmount":50000.00}' | jq .

# 4. Activate policy
curl -s -X PATCH $BASE/api/v1/policies/1/activate | jq .

# 5. File claim (cross-module: validates policy ownership by customer in one transaction)
curl -s -X POST "$BASE/api/v1/claims?policyId=1&customerId=1&type=MEDICAL&description=Hospital%20visit&claimedAmount=5000.00" | jq .

# 6. Approve claim
curl -s -X PATCH "$BASE/api/v1/claims/1/approve?approvedAmount=4500.00" | jq .

# 7. Settle claim (cross-module: reads approved amount directly)
curl -s -X POST "$BASE/api/v1/settlements/settle-claim?claimId=1" | jq .
```

---

## Cross-Module Dependency Map

Understanding these dependencies is core to the benchmark. Each arrow represents a direct Spring bean injection (monolith) vs an HTTP call (microservices).

### Library
```
LoanService      → BookService (decrement/increment copies)
LoanService      → MemberService (validate active, check limit)
FineService      → LoanService (get loan, calculate overdue)
ReservationService → BookService (verify book exists)
ReservationService → MemberService (validate active member)
```

### Healthcare
```
AppointmentService  → PatientService (validatePatientExists)
AppointmentService  → DoctorService (validateDoctorAvailability)
MedicalRecordService → AppointmentService (getAppointmentById)
MedicalRecordService → PatientService (validatePatientExists)
PrescriptionService  → MedicalRecordService (getRecordById)
PrescriptionService  → PatientService (validatePatientExists)
BillingService       → AppointmentService (getAppointmentById)
BillingService       → PatientService (validatePatientExists)
```

### Insurance
```
PolicyService    → CustomerService (validateActiveCustomer)
PolicyService    → AgentService (validateActiveAgent)
CoverageService  → PolicyService (getPolicyById)
PremiumService   → PolicyService (validateActivePolicy, getPolicyById)
ClaimService     → PolicyService (validateActivePolicy, getPolicyById)
ClaimService     → CustomerService (validateActiveCustomer)
SettlementService → ClaimService (getClaimById)
```

---

## File Locations

```
ModulithBenchMark/
├── library/
│   ├── monolith/          ← THIS IS WHAT'S NEW
│   │   ├── pom.xml
│   │   ├── docker-compose.yml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/benchmark/library/
│   │       ├── LibraryApplication.java
│   │       ├── book/
│   │       ├── member/
│   │       ├── loan/         ← Key: injects BookService + MemberService
│   │       ├── fine/         ← Key: injects LoanService
│   │       └── reservation/  ← Key: injects BookService + MemberService
│   └── microservices/
│       ├── book-service/
│       ├── member-service/
│       ├── loan-service/
│       ├── fine-service/
│       └── reservation-service/
├── healthcare/
│   ├── monolith/          ← THIS IS WHAT'S NEW
│   └── microservices/
└── insurance/
    ├── monolith/          ← THIS IS WHAT'S NEW
    └── microservices/
```

---

## Notes for Benchmark Runners

1. **Give the same task** to the agent for both monolith and microservices versions — only the codebase structure differs.
2. **Measure time to first correct response**, not just correctness.
3. **Don't hint** which architecture is expected to perform better.
4. **Record context usage** — monolith requires fewer files to load for cross-module tasks.
5. **Swagger UI** at `/swagger-ui.html` on each monolith — agents can browse the API without reading source code.
6. **Health endpoint** at `/actuator/health` — verify the service is running before testing.
