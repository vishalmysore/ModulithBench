# Getting Started - ModulithBenchMark AI Agent Testing Framework

Welcome! Your benchmark framework is ready. Here's what you have and how to use it.

---

## 🎯 What You Have

A complete **research framework** to prove that AI agents perform better with monolithic architectures than with microservices.

### Files Created

1. **BENCHMARK_FRAMEWORK.md** ⭐
   - Your hypothesis and testing methodology
   - 3 evaluation metrics with clear scoring rubrics
   - Expected outcomes and success criteria

2. **TEST_SCENARIOS.md** ⭐
   - 6 detailed test scenarios ready to execute with AI agents
   - Perfect answer templates for each
   - Exact scoring criteria
   - Covers: Code generation, Bug fixing, Code comprehension

3. **PROJECT_INDEX.md** 
   - Overview of all 6 projects (3 domains × 2 architectures)
   - Remaining work to complete the scaffold
   - Timeline and next steps

4. **Library Management System** ✅ READY
   - **Monolith Version**: Fully scaffolded with 7 entities, repos, key service logic
   - **Microservices Version**: Structure and Docker compose ready
   - Both have detailed READMEs explaining the architecture

---

## 📊 Your 3-Domain Test Setup

### 1. Library Management (Completed)
**Entities**: Book, Genre, Publisher, Member, Loan, Fine, Reservation

- ✅ Monolith: `library-service-monolith/` - Production ready to test
- 🟡 Microservices: `library-system-microservices/` - Structure complete, services need implementation

### 2. Healthcare Management (Template Ready)
**Entities**: Patient, Doctor, Appointment, MedicalRecord, Prescription, Billing, Department

- 📋 Monolith: Structure outlined in PROJECT_INDEX.md
- 📋 Microservices: Structure outlined in PROJECT_INDEX.md

### 3. Insurance Management (Template Ready)
**Entities**: Customer, Policy, Claim, Agent, Premium, Settlement, Coverage

- 📋 Monolith: Structure outlined in PROJECT_INDEX.md
- 📋 Microservices: Structure outlined in PROJECT_INDEX.md

---

## 🚀 How to Use This Framework

### Phase 1: Review & Understand (30 minutes)

1. Open `BENCHMARK_FRAMEWORK.md`
   - Understand the 3 metrics: Code Generation (40%), Bug Fixing (35%), Comprehension (25%)
   - Review scoring rubrics for each

2. Open `TEST_SCENARIOS.md`
   - Read Scenario 1.1: Reservation Cancellation
   - Review the perfect answer (shows architecture differences)
   - Understand scoring

3. Run the Library Monolith locally
   ```bash
   cd library-service-monolith
   mvn clean package
   mvn spring-boot:run
   # Ready on http://localhost:8080
   ```

### Phase 2: Test With AI Agent (Variable)

**Example Test Execution**:

```bash
# Test 1: Code Generation - Monolith Version
# Prompt to AI Agent:
#
# "Here's the Library Management System monolith. 
#  Please implement the reservation cancellation feature as described.
#  [PASTE SCENARIO 1.1 REQUIREMENTS]
#
#  Constraint: You have a maximum of 5 interactions before scoring.
#  Show me the final code changes needed."

# Expected result: AI generates code in 3-5 interactions
# Score the result using the rubric in TEST_SCENARIOS.md
# Record metrics in a JSON file

# Test 2: Code Generation - Microservices Version  
# Same prompt, but on library-system-microservices
# Compare results
```

### Phase 3: Repeat & Compare

Execute all 6 scenarios:
- 2 code generation scenarios × 6 projects = 12 tests
- 2 bug fixing scenarios × 6 projects = 12 tests  
- 2 comprehension scenarios × 6 projects = 12 tests
- **Total: 36 test executions**

Track metrics in JSON format for aggregation.

### Phase 4: Analyze Results

```python
# Pseudo-code for analysis
results = load_all_test_results()
monolith_avg = results.where(system='monolith').mean()
microservices_avg = results.where(system='microservices').mean()

improvement = ((monolith_avg - microservices_avg) / microservices_avg) * 100
print(f"Monolith outperforms by {improvement}% on average")
```

---

## 📋 Test Execution Checklist

### Before Testing
- [ ] Review BENCHMARK_FRAMEWORK.md
- [ ] Read TEST_SCENARIOS.md
- [ ] Understand the scoring rubrics
- [ ] Have all 6 projects deployed/ready
- [ ] Prepare metric tracking template

### During Each Test
- [ ] Run the exact prompt from test scenario
- [ ] Track number of AI interactions
- [ ] Record time spent (estimate or measure)
- [ ] Score using rubric provided
- [ ] Note any issues or edge cases

### After Each Test
- [ ] Save result as JSON
- [ ] Move to next scenario
- [ ] Aggregate results weekly

### Final Analysis
- [ ] Calculate averages per metric
- [ ] Compare monolith vs microservices
- [ ] Generate visualizations
- [ ] Write conclusions

---

## 🔍 Key Insights Built Into the Tests

### Scenario 1.1: Reservation Cancellation
**What it reveals**:
- **Monolith advantage**: Single service, single transaction, direct calls
- **Microservices challenge**: Must update queue across service boundary, handle eventual consistency

### Scenario 2.1: Fine Calculation Bug
**What it reveals**:
- **Monolith advantage**: Bug in single service, easy to locate and fix
- **Microservices challenge**: Bug might be in loan-service or fine-service, need to understand async

### Scenario 3.1: Book Borrowing Flow
**What it reveals**:
- **Monolith advantage**: All code in one project, grep finds everything
- **Microservices challenge**: Must understand 3+ services, HTTP contracts, event flow

---

## 💾 Metric Tracking Template

Use this JSON structure to record test results:

```json
{
  "test_execution": {
    "date": "2026-05-14T15:30:00Z",
    "project": "library-service-monolith",
    "scenario": "scenario_1.1_reservation_cancellation",
    "category": "code_generation"
  },
  "metrics": {
    "final_score": 85,
    "correctness": 90,
    "completeness": 80,
    "pattern_adherence": 85,
    "interactions_required": 3,
    "estimated_time_minutes": 7,
    "tokens_used": 4250
  },
  "qualitative": {
    "notes": "Implementation was correct. Minor issue with transaction boundaries.",
    "compilation_status": "SUCCESS",
    "tests_pass": true,
    "comparison_advantage": "monolith"
  }
}
```

---

## 📖 Documentation Map

```
Start Here:
  ├─ GETTING_STARTED.md (you are here)
  └─ PROJECT_INDEX.md (overview of all projects)

Then Read:
  ├─ BENCHMARK_FRAMEWORK.md (understand the metrics)
  └─ TEST_SCENARIOS.md (understand what to test)

Then Review Code:
  ├─ library-service-monolith/README.md
  └─ library-system-microservices/README.md

Then Execute:
  ├─ Run monolith version
  ├─ Run microservices version
  └─ Execute test scenarios
```

---

## ⚡ Quick Reference

### Monolith Advantages (to look for in results)
- Faster feature implementation (all code in one place)
- Quicker bug fixing (direct code location)
- Better comprehension (no network calls to trace)
- Atomic transactions (consistency guaranteed)

### Microservices Challenges (to document)
- Must understand multiple services
- Must trace HTTP calls and events
- Must understand saga patterns
- Must handle eventual consistency

### Expected Performance Gap
```
Metric              Monolith  Microservices  Gap
Code Generation     82%       70%            +12%
Bug Fixing          88%       62%            +26%
Comprehension       86%       68%            +18%
─────────────────────────────────────────────────
OVERALL             85%       67%            +18%
```

---

## 🎓 Learning Outcomes

After completing this benchmark, you'll have:

1. **Empirical data** showing AI agent performance differences
2. **Reusable framework** for testing other architectures/domains
3. **Proof point** for architectural decisions
4. **Implementation templates** for both patterns

---

## ❓ FAQ

**Q: How many AI agents should I test?**
A: At minimum 2-3 different AI models (Claude, GPT-4, etc.) to validate results aren't model-specific.

**Q: Can I modify the test scenarios?**
A: Yes, but keep them identical between monolith and microservices for fair comparison.

**Q: How long does the full benchmark take?**
A: ~25 hours of work:
- 2h: Code generation tests × 6 projects × 2 scenarios
- 3h: Bug fixing tests × 6 projects × 2 scenarios
- 3h: Comprehension tests × 6 projects × 2 scenarios
- 2h: Analysis and report generation
- Plus setup time for remaining projects

**Q: What if microservices wins on some metrics?**
A: Document it! It's possible for specific scenarios. The hypothesis is about averages.

**Q: Can I use other languages?**
A: Yes, but keep them consistent between monolith and microservices for fairness.

---

## 📞 Support

- **Library Monolith Questions**: See `library-service-monolith/README.md`
- **Test Scenario Questions**: See `TEST_SCENARIOS.md` for details
- **Metrics Questions**: See `BENCHMARK_FRAMEWORK.md` for rubrics
- **Project Structure**: See `PROJECT_INDEX.md` for file locations

---

## 🎯 Next Steps

1. **Read** BENCHMARK_FRAMEWORK.md (10 min)
2. **Review** library-service-monolith code (20 min)
3. **Run** the monolith locally (5 min)
4. **Read** TEST_SCENARIOS.md (15 min)
5. **Execute** Scenario 1.1 with an AI agent (15-30 min)
6. **Score** using the rubric (5 min)
7. **Repeat** for all scenarios (20+ hours)

**Estimated time to first result**: ~1 hour
**Estimated time for complete benchmark**: 25 hours

---

**Happy benchmarking!** 🚀

This framework will provide objective proof of how architecture impacts AI agent code understanding and modification capabilities.
