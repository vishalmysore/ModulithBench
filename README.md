# ModulithBenchMark

A benchmark that measures whether AI agents perform better when working with monolithic vs microservices architecture.

**Hypothesis**: Monoliths give AI agents 18–30% better performance on code generation, bug fixing, and comprehension — because all related logic lives in one place.

---

## Repository Structure

```
ModulithBenchMark/
├── library/
│   ├── monolith/          Spring Boot 3.2, port 8080
│   └── microservices/     5 separate services
├── healthcare/
│   ├── monolith/          Spring Boot 3.2, port 8081
│   └── microservices/     7 separate services
└── insurance/
    ├── monolith/          Spring Boot 3.2, port 8082
    └── microservices/     7 separate services
```

Each domain is fully implemented in both architectures with the same business logic, allowing direct comparison.

---

## Quick Start

```bash
# Run any monolith with Docker
cd library/monolith    && docker compose up -d  # http://localhost:8080
cd healthcare/monolith && docker compose up -d  # http://localhost:8081
cd insurance/monolith  && docker compose up -d  # http://localhost:8082

# Swagger UI
http://localhost:8080/swagger-ui.html

# Run tests (no Docker needed — uses H2 in-memory)
cd library/monolith && mvn test -Dtest=CrossModuleIntegrationTest
```

---

## The Key Architectural Difference

**Monolith** — cross-module calls are direct Java method calls in one transaction:

```java
// LoanService.java — all in one @Transactional, one JVM, zero HTTP
memberService.validateActiveMember(memberId);   // direct call
bookService.decrementAvailableCopies(bookId);   // direct call
loanRepository.save(loan);
```

**Microservices** — the same operation requires HTTP calls across service boundaries:

```
loan-service  →  HTTP  →  member-service
loan-service  →  HTTP  →  book-service
(each call can fail independently, no atomicity)
```

---

## Cross-Module Dependencies

```
Library:
  LoanService        → BookService, MemberService
  FineService        → LoanService
  ReservationService → BookService, MemberService

Healthcare:
  AppointmentService   → PatientService, DoctorService
  MedicalRecordService → AppointmentService, PatientService
  PrescriptionService  → MedicalRecordService, PatientService
  BillingService       → AppointmentService, PatientService

Insurance:
  PolicyService     → CustomerService, AgentService
  CoverageService   → PolicyService
  PremiumService    → PolicyService
  ClaimService      → PolicyService, CustomerService
  SettlementService → ClaimService
```

---

## For AI Agents

If you are an AI agent running this benchmark, read:

1. **`AGENT_BENCHMARK_PROTOCOL.md`** — your task list, validation commands, and self-assessment form
2. **`AGENTS.md`** — full API reference and curl examples
3. **`CLAUDE.md`** — build commands and file navigation

The benchmark asks you to perform the same coding tasks on both architectures, validate with `mvn compile`, and honestly report which was harder and why.

---

## Tech Stack

- Java 17, Spring Boot 3.2.0, Spring Modulith 1.1.0
- Spring Data JPA + PostgreSQL (H2 for tests)
- Lombok, springdoc-openapi, Spring Boot Actuator

---

## Research Background

- **3 metrics**: Code Generation (40%), Bug Fixing (35%), Comprehension (25%)
- **6 test scenarios** across all domains in `AGENT_BENCHMARK_PROTOCOL.md`
- Expected monolith advantage: fewer files to read, guaranteed atomicity, no HTTP boilerplate
