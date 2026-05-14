# AI Agent Benchmark Framework: Monolith vs Microservices

## Hypothesis
AI agents perform better with clean monolithic architectures than with microservices due to:
- Simpler codebase navigation
- Fewer distributed concerns
- Easier context retention
- Direct function calls vs API coordination
- Single deployment/testing model

## Test Projects

### 1. Library Management System
**Entities (7)**: Book, Member, Loan, Fine, Reservation, Publisher, Genre

**Business Logic**:
- Members borrow books (max 5 concurrent)
- Track loan due dates and fines
- Handle reservations when books unavailable
- Genre and publisher relationships

---

### 2. Healthcare Management System
**Entities (7)**: Patient, Doctor, Appointment, MedicalRecord, Prescription, Billing, Department

**Business Logic**:
- Schedule appointments (no double-booking)
- Create medical records tied to appointments
- Generate prescriptions from appointments
- Calculate billing based on services and prescriptions
- Doctor availability management

---

### 3. Insurance Management System
**Entities (7)**: Customer, Policy, Claim, Agent, Premium, Settlement, Coverage

**Business Logic**:
- Create and renew policies
- File claims against policies
- Process claim settlements
- Track premium payments
- Apply discounts based on claims history

---

## Evaluation Metrics

### 1. CODE GENERATION ACCURACY (40% weight)
**What we measure**: When asking an AI agent to implement a new feature, how complete and correct is the code?

**Scoring**:
- ✅ **Correctness** (50%): Does code compile? Do tests pass?
- ✅ **Completeness** (30%): Are all required components present (Controller, Service, Repository, Entity, Tests)?
- ✅ **Pattern Adherence** (20%): Does it follow the project's architectural patterns?

**Test Scenario Example**:
```
"Add a reservation cancellation feature. A member should be able to cancel 
a reservation and it should become available for the next person in queue. 
Update the UI if present and ensure consistency."
```

**Scoring Rubric**:
- Perfect (100): Fully working, all layers, tests included, no fixes needed
- Good (80): Mostly working, minor bugs, tests present but incomplete
- Partial (50): Works in isolation, missing tests or integration issues
- Poor (20): Incomplete or doesn't work
- Failed (0): Non-functional or fundamental misunderstanding

---

### 2. BUG FIXING SPEED & ACCURACY (35% weight)
**What we measure**: How quickly and accurately can an AI agent identify and fix bugs?

**Scoring**:
- ✅ **Detection Time** (40%): Steps/tokens to identify root cause
- ✅ **Fix Correctness** (40%): Does fix actually resolve the issue without regressions?
- ✅ **Fix Quality** (20%): Is it the best/minimal fix or a workaround?

**Test Scenario Example**:
```
Bug planted: Loan due date calculation uses wrong month, causing fines 
to be calculated incorrectly. Agent must find and fix.
```

**Scoring Rubric**:
- Immediate (100): Found root cause in 1-2 interactions
- Quick (80): Found within 3-4 interactions
- Moderate (60): Required 5-6 interactions
- Slow (40): Took 7+ interactions
- Failed (0): Couldn't identify or fix

---

### 3. CODEBASE NAVIGATION & COMPREHENSION (25% weight)
**What we measure**: How well can an agent understand code structure and locate relevant components?

**Scoring**:
- ✅ **Location Accuracy** (50%): Did it find the right files/methods?
- ✅ **Understanding Depth** (30%): Does explanation show real comprehension?
- ✅ **Efficiency** (20%): Minimal unnecessary exploration

**Test Scenario Example**:
```
"Walk me through what happens when a member tries to borrow a book 
that's already reserved. Show me all the code involved and explain 
the flow across the system."
```

**Scoring Rubric**:
- Expert (100): Complete accurate flow with all relevant classes
- Good (80): Mostly accurate, minor gaps
- Adequate (60): Basic understanding, some gaps
- Poor (40): Missing key components or misunderstandings
- Failed (0): Fundamental misunderstanding of flow

---

## Project Structure Comparison

### MONOLITH ARCHITECTURE
```
library-service/
├── src/
│   ├── main/java/com/library/
│   │   ├── book/
│   │   │   ├── Book.java
│   │   │   ├── BookRepository.java
│   │   │   ├── BookService.java
│   │   │   └── BookController.java
│   │   ├── member/
│   │   ├── loan/
│   │   ├── fine/
│   │   ├── reservation/
│   │   ├── shared/
│   │   │   ├── config/
│   │   │   ├── exception/
│   │   │   └── utils/
│   │   └── LibraryServiceApp.java
│   └── test/...
├── pom.xml
└── README.md
```

**Key Characteristics**:
- Single Spring Boot application
- All modules share JPA/Hibernate with single DB schema
- Direct service-to-service calls via @Autowired
- Single deployment unit
- Shared transaction boundaries
- Single application configuration

### MICROSERVICES ARCHITECTURE
```
library-system/
├── book-service/
│   ├── src/main/java/com/library/book/
│   ├── pom.xml
│   └── application.yml
├── member-service/
├── loan-service/
├── fine-service/
├── reservation-service/
├── shared-library/ (common DTOs and clients)
├── docker-compose.yml (for local development)
├── README.md
└── SERVICE_COMMUNICATION.md
```

**Key Characteristics**:
- 5 independent Spring Boot applications
- Each service has own database
- Communication via REST APIs and/or message queue (RabbitMQ/Kafka)
- Service discovery or hardcoded endpoints
- Separate deployments
- Cross-service transaction handling via sagas
- Each service owns its schema

---

## Test Execution Flow

### Phase 1: Project Setup
1. Generate both versions of each project
2. Document architecture differences in comments
3. Create README with navigation guides

### Phase 2: Test Scenarios (Applied to all 6 projects)

#### Scenario A: Feature Addition (Code Generation)
- **Prompt**: Clear feature request with acceptance criteria
- **Evaluation**: Review generated code against rubric
- **Time Limit**: 5 agent interactions max before scoring
- **Success Criteria**: Feature works end-to-end

#### Scenario B: Bug Fixing (Debugging)
- **Setup**: Plant specific bug in codebase
- **Prompt**: Describe symptom, not location
- **Evaluation**: Track path to root cause
- **Metric**: Steps and accuracy

#### Scenario C: Comprehension (Navigation)
- **Prompt**: Ask for detailed flow explanation
- **Evaluation**: Accuracy of identification and explanation
- **Metric**: Completeness and accuracy of response

### Phase 3: Analysis
- Compare metrics across monolith vs microservices
- Analyze per-domain variations
- Identify bottlenecks unique to microservices
- Generate statistical comparison

---

## Expected Outcomes

### Hypothesis Validation
**If monolith is better for agents**:
- Faster code generation (fewer service boundaries)
- Quicker bug detection (all code in one place)
- Better navigation (no distributed tracing needed)

**If microservices are better**:
- Lower cognitive load (smaller service contexts)
- Clearer boundaries (easier for agents to reason about)

---

## Metrics Output Format

### Per-Test Result
```json
{
  "project": "library-monolith",
  "test_scenario": "add_reservation_cancellation",
  "metric": "code_generation_accuracy",
  "score": 85,
  "details": {
    "correctness": 90,
    "completeness": 80,
    "pattern_adherence": 85,
    "interactions": 3,
    "time_tokens": 4250
  },
  "notes": "Good implementation, missing some error handling"
}
```

### Summary Comparison Table
| Metric | Library-M | Library-µS | Healthcare-M | Healthcare-µS | Insurance-M | Insurance-µS |
|--------|-----------|-----------|--------------|---------------|-------------|-------------|
| Code Gen Accuracy | | | | | | |
| Bug Fix Speed | | | | | | |
| Navigation Score | | | | | | |
| **OVERALL** | | | | | | |

---

## Next Steps
1. Generate project scaffolds
2. Document inter-service communication patterns for µS
3. Create test scenario templates
4. Execute with AI agents
5. Aggregate and analyze results
