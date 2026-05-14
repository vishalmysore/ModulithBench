# ModulithBenchMark — Claude Code Guide

## IMPORTANT: Do Not Commit Your Changes

**Never run `git add`, `git commit`, or `git push` in this repository.**

`main` is the clean benchmark baseline. Your solutions go in your report, not in the repo.
When you finish, discard everything: `git checkout -- . && git clean -fd`

See `AGENT_BENCHMARK_PROTOCOL.md` for the full rules.

---

## What This Project Is

A benchmark comparing AI agent performance on **monolithic** vs **microservices** architecture across 3 domains: Library, Healthcare, Insurance.

Each domain has two implementations:
- `{domain}/monolith/` — Single Spring Boot app, all modules in one codebase
- `{domain}/microservices/` — One Spring Boot app per module, each independently deployed

## Running the Monoliths

```bash
# Library (port 8080) — requires Docker
cd library/monolith && docker compose up -d

# Healthcare (port 8081)
cd healthcare/monolith && docker compose up -d

# Insurance (port 8082)
cd insurance/monolith && docker compose up -d
```

Or with Maven + local PostgreSQL:
```bash
cd library/monolith && mvn spring-boot:run
```

## Key Code Locations

### Library Monolith
- Main class: `library/monolith/src/main/java/com/benchmark/library/LibraryApplication.java`
- Cross-module logic: `library/monolith/src/main/java/com/benchmark/library/loan/LoanService.java`
  - Injects `BookService` and `MemberService` directly
  - `createLoan()` validates member, checks book availability, decrements copies — all in one `@Transactional`

### Healthcare Monolith
- Main class: `healthcare/monolith/src/main/java/com/benchmark/healthcare/HealthcareApplication.java`
- Cross-module logic: `healthcare/monolith/src/main/java/com/benchmark/healthcare/appointment/AppointmentService.java`
  - Injects `PatientService` + `DoctorService` directly
  - `scheduleAppointment()` validates both patient and doctor atomically

### Insurance Monolith
- Main class: `insurance/monolith/src/main/java/com/benchmark/insurance/InsuranceApplication.java`
- Cross-module logic: `insurance/monolith/src/main/java/com/benchmark/insurance/claim/ClaimService.java`
  - Injects `PolicyService` + `CustomerService` directly
  - `fileClaim()` verifies policy ownership by customer in one transaction

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Spring Modulith 1.1.0
- Spring Data JPA + PostgreSQL
- Lombok
- springdoc-openapi (Swagger UI at `/swagger-ui.html`)
- Spring Boot Actuator (health at `/actuator/health`)

## Cross-Module Dependency Diagram

```
Library:          Loan → Book, Member | Fine → Loan | Reservation → Book, Member
Healthcare:       Appointment → Patient, Doctor | MedicalRecord → Appointment, Patient
                  Prescription → MedicalRecord, Patient | Billing → Appointment, Patient
Insurance:        Policy → Customer, Agent | Coverage → Policy | Premium → Policy
                  Claim → Policy, Customer | Settlement → Claim
```

## Package Structure Pattern

Every module follows this structure:
```
{module}/
├── {Entity}.java          — JPA entity with real domain fields
├── {Entity}Repository.java — Spring Data JPA with custom queries
├── {Entity}Service.java    — Business logic, @Transactional, cross-module calls here
├── {Entity}Controller.java — REST endpoints
└── {Entity}NotFoundException.java
```

## Build

```bash
cd library/monolith && mvn clean package -DskipTests
cd healthcare/monolith && mvn clean package -DskipTests
cd insurance/monolith && mvn clean package -DskipTests
```

## Running Tests

Each monolith has integration tests that verify cross-module behavior using H2 in-memory DB — no PostgreSQL needed:

```bash
cd library/monolith    && mvn test -Dtest=CrossModuleIntegrationTest
cd healthcare/monolith && mvn test -Dtest=CrossModuleIntegrationTest
cd insurance/monolith  && mvn test -Dtest=CrossModuleIntegrationTest
```

## If You Are an AI Agent

**Read this file next: `AGENT_BENCHMARK_PROTOCOL.md`**

It contains:
- 6 concrete coding tasks to run on BOTH monolith and microservices
- `mvn compile` validation commands to check your work after each task
- A structured self-report template to fill out comparing your experience
- Runtime curl commands to validate against a running service

`AGENTS.md` has the full REST API reference and human evaluator scoring rubric.
