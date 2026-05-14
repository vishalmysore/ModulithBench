# ModulithBench

---

> ## STOP — IF YOU ARE AN AI AGENT READING THIS FILE
>
> This repository is your test. Do not read further into this file.
>
> **Follow these steps in order:**
>
> 1. Read [`CLAUDE.md`](CLAUDE.md) — project orientation and build commands
> 2. Read [`AGENT_BENCHMARK_PROTOCOL.md`](AGENT_BENCHMARK_PROTOCOL.md) — your 11 tasks, validation commands, and self-assessment form
> 3. Run every task on **both** the monolith and microservices codebases
> 4. Validate each implementation: `mvn compile -q`
> 5. Submit results to the `benchmark-results` branch — **NEVER commit to `main`**
>    See [`results/SUBMIT_RESULTS.md`](results/SUBMIT_RESULTS.md) for exact steps
>
> Fill out [`results/template.md`](results/template.md) and save it as `results/runs/{agent-name}-{YYYY-MM-DD}.md`.
>
> **Everything below this line is background context for humans.**

---

A benchmark that measures how software architecture affects AI coding agent performance across four enterprise domains.

---

## The Core Argument

No architecture is universally good. Each makes a different tradeoff:

| | Traditional Monolith | Microservices | Modular Monolith |
|---|---|---|---|
| **Scalability** | Poor — scale everything or nothing | Excellent — scale each service independently | Good — scale the whole app; extract hot modules only when needed |
| **High Availability** | Single point of failure | Excellent — independent failure domains | Good — HA at the app level; module isolation prevents cascading failures |
| **DevOps Complexity** | Simple — one deployment | High — service mesh, distributed tracing, N CI/CD pipelines | Low — one deployment, one config, one pipeline |
| **AI Agent Productivity** | Good — high locality, but no module boundaries so agents get lost in the "big ball of mud" | Poor — context fragmentation, repo-hopping, HTTP boundaries, eventual consistency | **Best** — high locality AND clear module boundaries give agents a perfect signal-to-noise ratio |
| **Developer Experience** | Easy to start, hard to maintain at scale | Autonomous teams, but network plumbing is exhausting | Best of both: logical order with physical simplicity |
| **Transaction Model** | ACID | Eventual consistency / Sagas | ACID |
| **Refactoring** | Hard — tight coupling everywhere | Complex — breaking API contracts | Easy — module boundaries guide every change |

**The sweet spot is the Modular Monolith.**

It preserves the locality that AI agents need, avoids the operational complexity that destroys DevOps velocity, and still enforces clean module boundaries that keep the codebase navigable. You can extract a module into a microservice later — but only when you actually need to, not speculatively upfront.

---

## Why AI Agents Struggle With Microservices

AI coding agents have finite context windows and no persistent memory of a codebase. When business logic is spread across services:

- Understanding one feature requires reading 4+ repositories
- Tracing a bug means following HTTP calls across service boundaries
- Implementing a cross-service feature requires reasoning about API contracts, error propagation, and eventual consistency simultaneously
- Atomic multi-service operations require saga patterns — the agent must reason about compensating transactions and partial failure states

This is **context fragmentation**. It is the architectural equivalent of CPU cache misses — the agent spends most of its reasoning budget navigating the architecture rather than solving the actual problem.

**Modular monoliths eliminate context fragmentation** while preserving the logical structure that helps agents find and understand code quickly.

---

## What This Benchmark Measures

Four domains, each implemented in both architectures:

| Domain | Modules | Port | Key Cross-Module Scenario |
|--------|---------|------|--------------------------|
| Library | 5 | 8080 | Loan creation validates member + decrements book inventory atomically |
| Healthcare | 7 | 8081 | Appointment scheduling validates patient + doctor availability in one transaction |
| Insurance | 7 | 8082 | Claim filing verifies policy ownership by customer without HTTP |
| Supply Chain | 8 | 8083 | Ghost Shipment: order cancellation releases inventory + warehouse task + carrier booking atomically |

Three benchmark metrics — Code Generation (40%), Bug Fixing (35%), Comprehension (25%) — scored across 11 tasks that each require cross-module reasoning.

---

## The Two Scenarios That Make the Case

### Ghost Shipment (Supply Chain)

A customer cancels an order while the warehouse is picking it and the carrier already has a booking.

**Monolith**: `OrderService.cancelOrder()` releases inventory, cancels the warehouse task, and cancels the carrier booking in **one `@Transactional`**. If any step throws, everything rolls back atomically. Partial state is structurally impossible.

**Microservices**: Three HTTP calls to three independent services. If `carrier-service` returns 503 after the first two succeed, you now have: cancelled order, released inventory, but an **active carrier booking**. The ghost shipment exists. The agent must now implement a distributed saga with compensating transactions, idempotency keys, and a dead letter queue — none of which is the actual business problem.

### N+1 Profitability Report (Supply Chain)

Generate a shipment profitability report combining revenue (Order), shipping cost (Carrier), duties (Customs), and fuel estimate (Route).

**Monolith**: `BillingService.generateProfitabilityReport()` — four direct method calls in one `@Transactional(readOnly = true)`, roughly 20 lines of pure business logic.

**Microservices**: Four HTTP calls, four JSON schemas to deserialize, four independent error states to handle. The agent writes roughly 80 lines — most of it infrastructure boilerplate with no business value. The reasoning cost is 4x higher.

---

## Quick Start

```bash
# Run any monolith (requires Docker)
cd library/monolith      && docker compose up -d   # http://localhost:8080
cd healthcare/monolith   && docker compose up -d   # http://localhost:8081
cd insurance/monolith    && docker compose up -d   # http://localhost:8082
cd supply-chain/monolith && docker compose up -d   # http://localhost:8083

# Swagger UI on any running monolith
http://localhost:{port}/swagger-ui.html

# Run integration tests — no Docker needed, uses H2 in-memory
cd library/monolith      && mvn test -Dtest=CrossModuleIntegrationTest
cd healthcare/monolith   && mvn test -Dtest=CrossModuleIntegrationTest
cd insurance/monolith    && mvn test -Dtest=CrossModuleIntegrationTest
cd supply-chain/monolith && mvn test -Dtest=GhostShipmentTest
```

---

## For AI Agents Running This Benchmark

```
1. Read CLAUDE.md                      — project orientation and build commands
2. Read AGENT_BENCHMARK_PROTOCOL.md   — your 11 tasks, validation commands, self-assessment form
3. Run every task on BOTH architectures — do not skip the microservices side
4. Validate each implementation: mvn compile -q
5. Submit results to the benchmark-results branch — NEVER commit to main
   (results/SUBMIT_RESULTS.md has exact steps and naming convention)
```

Fill out `results/template.md` and save it as `results/runs/{agent-name}-{YYYY-MM-DD}.md`.

---

## Tech Stack

Java 17 · Spring Boot 3.2 · Spring Modulith 1.1 · Spring Data JPA · PostgreSQL · H2 (tests) · Lombok · springdoc-openapi
