#!/usr/bin/env python3
"""
Test 2 — Deterministic Rubric Scorer

Analyses agent-modified source code against a set of deterministic rules.
Scores are objective — based on code patterns, not agent self-reporting.

Usage:
    python score_rubric.py --monolith library/monolith --task A1
    python score_rubric.py --monolith library/monolith --all-tasks
    python score_rubric.py --compare library/monolith library/microservices --task A2

Rules per task:
    A1  Loan history endpoint exists; enriched DTO has bookTitle + bookAuthor fields
    A2  Validation happens BEFORE state change in LoanService.createLoan()
    A4  LoanService.hasActiveLoan() called from ReservationService
    B1  Cross-module method call exists in AppointmentService to BillingService/Repository
    B2  AppointmentService.cancelAppointment() calls BillingService and/or MedicalRecordService
    C1  ClaimService.approveClaim() triggers SettlementService (direct call or @EventListener)
    D1  OrderService.cancelOrder() calls inventoryService + warehouseService + carrierService
    D2  BillingService.generateProfitabilityReport() calls 4+ service methods
    D3  WarehouseService.dispatch() calls NotificationService or publishes an event
"""

import argparse
import re
from dataclasses import dataclass
from pathlib import Path
from typing import List, Optional


@dataclass
class RuleResult:
    rule: str
    passed: bool
    detail: str
    file: str


@dataclass
class TaskScore:
    task: str
    description: str
    results: List[RuleResult]

    @property
    def score(self): return sum(1 for r in self.results if r.passed)
    @property
    def total(self): return len(self.results)
    @property
    def pct(self): return self.score / self.total * 100 if self.total else 0


def read_file(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8", errors="ignore")
    except Exception:
        return ""


def find_file(base: Path, *name_parts) -> Optional[Path]:
    for part in name_parts:
        matches = list(base.rglob(f"*{part}*"))
        if matches:
            return matches[0]
    return None


def contains(text: str, pattern: str) -> bool:
    return bool(re.search(pattern, text))


def line_order(text: str, pattern_a: str, pattern_b: str) -> bool:
    """Returns True if pattern_a appears before pattern_b in the file."""
    lines = text.splitlines()
    pos_a = pos_b = -1
    for i, line in enumerate(lines):
        if pos_a == -1 and re.search(pattern_a, line):
            pos_a = i
        if pos_b == -1 and re.search(pattern_b, line):
            pos_b = i
    return pos_a != -1 and pos_b != -1 and pos_a < pos_b


# ─── Task-specific rubric functions ──────────────────────────────────────────

def score_A1(monolith_base: Path) -> TaskScore:
    task = TaskScore("A1", "Enriched loan history with book details", [])

    service_file = find_file(monolith_base, "LoanService")
    controller_file = find_file(monolith_base, "LoanController")
    dto_file = find_file(monolith_base, "LoanHistoryDto", "LoanDetail")

    svc = read_file(service_file) if service_file else ""
    ctl = read_file(controller_file) if controller_file else ""
    dto = read_file(dto_file) if dto_file else ""

    task.results.append(RuleResult(
        "getLoanHistoryByMember() method exists in LoanService",
        contains(svc, r"getLoanHistory"),
        f"Searched in {service_file}",
        str(service_file)
    ))
    task.results.append(RuleResult(
        "LoanService calls BookService.getBookById() to enrich data",
        contains(svc, r"bookService\.getBookById\("),
        "Direct cross-module call pattern not found",
        str(service_file)
    ))
    task.results.append(RuleResult(
        "LoanHistoryDto/LoanDetail class exists with bookTitle field",
        contains(dto, r"bookTitle") or contains(dto, r"book_title"),
        f"Searched in {dto_file}",
        str(dto_file)
    ))
    task.results.append(RuleResult(
        "GET /loans/member/{id}/history endpoint exists in controller",
        contains(ctl, r"history"),
        f"Searched in {controller_file}",
        str(controller_file)
    ))
    task.results.append(RuleResult(
        "Implementation uses no HTTP calls (monolith advantage)",
        not contains(svc, r"RestTemplate|WebClient|HttpClient|http://"),
        "Found HTTP client code in LoanService — should be a direct call",
        str(service_file)
    ))
    return task


def score_A2(monolith_base: Path) -> TaskScore:
    task = TaskScore("A2", "Transaction boundary bug fix — validate before state change", [])

    service_file = find_file(monolith_base, "LoanService")
    svc = read_file(service_file) if service_file else ""

    task.results.append(RuleResult(
        "LoanService.createLoan() calls validateActiveMember before decrementAvailableCopies",
        line_order(svc, r"validateActiveMember", r"decrementAvailableCopies"),
        "validateActiveMember must appear BEFORE decrementAvailableCopies — this is the bug fix",
        str(service_file)
    ))
    task.results.append(RuleResult(
        "createLoan() is @Transactional (atomicity guaranteed)",
        contains(svc, r"@Transactional"),
        "Missing @Transactional on createLoan",
        str(service_file)
    ))
    task.results.append(RuleResult(
        "Member validation check before any inventory change",
        line_order(svc, r"validateActiveMember|getMemberById|getMaxBooksAllowed", r"decrementAvailableCopies|reserveStock"),
        "Validation must precede all state changes",
        str(service_file)
    ))
    return task


def score_A4(monolith_base: Path) -> TaskScore:
    task = TaskScore("A4", "Block reservation when member already has active loan", [])

    reservation_file = find_file(monolith_base, "ReservationService")
    loan_file = find_file(monolith_base, "LoanService")
    loan_repo = find_file(monolith_base, "LoanRepository")

    rsv = read_file(reservation_file) if reservation_file else ""
    loan_svc = read_file(loan_file) if loan_file else ""
    loan_r = read_file(loan_repo) if loan_repo else ""

    task.results.append(RuleResult(
        "ReservationService injects LoanService (cross-module dependency)",
        contains(rsv, r"LoanService"),
        f"Searched {reservation_file}",
        str(reservation_file)
    ))
    task.results.append(RuleResult(
        "LoanService.hasActiveLoan() method exists",
        contains(loan_svc, r"hasActiveLoan"),
        f"Searched {loan_file}",
        str(loan_file)
    ))
    task.results.append(RuleResult(
        "LoanRepository has existsByMemberIdAndBookIdAndStatus query",
        contains(loan_r, r"existsBy.*MemberId.*BookId|existsByMemberIdAndBookId"),
        f"Searched {loan_repo}",
        str(loan_repo)
    ))
    task.results.append(RuleResult(
        "createReservation() checks hasActiveLoan before creating reservation",
        line_order(rsv, r"hasActiveLoan", r"reservationRepository\.save"),
        "hasActiveLoan check must happen before save",
        str(reservation_file)
    ))
    return task


def score_B1(monolith_base: Path) -> TaskScore:
    task = TaskScore("B1", "Doctor workload report — cross-module aggregation", [])

    svc_file = find_file(monolith_base, "AppointmentService")
    svc = read_file(svc_file) if svc_file else ""
    dto_file = find_file(monolith_base, "WorkloadSummary", "DoctorWorkload")
    dto = read_file(dto_file) if dto_file else ""

    task.results.append(RuleResult(
        "getDoctorWorkloadSummary() method exists in AppointmentService",
        contains(svc, r"getDoctorWorkload|WorkloadSummary"),
        str(svc_file),
        str(svc_file)
    ))
    task.results.append(RuleResult(
        "Method reads from DoctorService (cross-module: get doctor name)",
        contains(svc, r"doctorService\.|DoctorService"),
        "Should inject DoctorService to get doctor name",
        str(svc_file)
    ))
    task.results.append(RuleResult(
        "DoctorWorkloadSummary DTO includes totalAppointments field",
        contains(dto, r"totalAppointments") or contains(svc, r"totalAppointments"),
        str(dto_file or svc_file),
        str(dto_file or svc_file)
    ))
    return task


def score_B2(monolith_base: Path) -> TaskScore:
    task = TaskScore("B2", "Cascade cancel — appointment cancel triggers billing cancel", [])

    svc_file = find_file(monolith_base, "AppointmentService")
    svc = read_file(svc_file) if svc_file else ""

    task.results.append(RuleResult(
        "cancelAppointment() calls BillingService (cross-module cascade)",
        contains(svc, r"billingService\.|BillingService"),
        "AppointmentService should call BillingService on cancel",
        str(svc_file)
    ))
    task.results.append(RuleResult(
        "cancelAppointment() is @Transactional (atomic cascade)",
        contains(svc, r"@Transactional"),
        "Cancellation must be atomic",
        str(svc_file)
    ))
    return task


def score_C1(monolith_base: Path) -> TaskScore:
    task = TaskScore("C1", "Auto-settle on claim approval — SettlementService called/triggered", [])

    claim_file = find_file(monolith_base, "ClaimService")
    settle_file = find_file(monolith_base, "SettlementService")
    event_file = find_file(monolith_base, "ClaimApprovedEvent")

    claim_svc = read_file(claim_file) if claim_file else ""
    settle_svc = read_file(settle_file) if settle_file else ""

    task.results.append(RuleResult(
        "Settlement triggered on approval (direct call OR @EventListener)",
        contains(claim_svc, r"settlementService\.|SettlementService|ClaimApprovedEvent|applicationEventPublisher") or
        contains(settle_svc, r"@EventListener"),
        "approveClaim() must trigger settlement — either direct call or Spring event",
        str(claim_file)
    ))
    task.results.append(RuleResult(
        "Runs in same transaction (atomicity guaranteed)",
        contains(claim_svc, r"@Transactional") or
        (event_file is not None and contains(settle_svc, r"@EventListener|@TransactionalEventListener")),
        "Approval + settlement must be atomic",
        str(claim_file)
    ))
    return task


def score_D1(monolith_base: Path) -> TaskScore:
    task = TaskScore("D1", "Ghost Shipment — atomic order cancellation (4 modules)", [])

    order_file = find_file(monolith_base, "OrderService")
    svc = read_file(order_file) if order_file else ""

    task.results.append(RuleResult(
        "cancelOrder() calls inventoryService.releaseReservedStock()",
        contains(svc, r"inventoryService\.release|releaseReservedStock"),
        "Must release inventory on cancel",
        str(order_file)
    ))
    task.results.append(RuleResult(
        "cancelOrder() calls warehouseService.cancelPickTask()",
        contains(svc, r"warehouseService\.cancel|cancelPickTask"),
        "Must cancel warehouse task on cancel",
        str(order_file)
    ))
    task.results.append(RuleResult(
        "cancelOrder() calls carrierService.cancelBooking()",
        contains(svc, r"carrierService\.cancel|cancelBooking"),
        "Must cancel carrier booking on cancel",
        str(order_file)
    ))
    task.results.append(RuleResult(
        "cancelOrder() is @Transactional (all 4 steps atomic)",
        contains(svc, r"@Transactional"),
        "All cancellation steps must be in one transaction",
        str(order_file)
    ))
    task.results.append(RuleResult(
        "No HTTP calls in cancelOrder() (monolith: direct calls only)",
        not contains(svc, r"RestTemplate|WebClient|http://"),
        "cancelOrder uses direct service calls, not HTTP",
        str(order_file)
    ))
    return task


def score_D2(monolith_base: Path) -> TaskScore:
    task = TaskScore("D2", "N+1 Profitability Report — reads 4 modules in one transaction", [])

    billing_file = find_file(monolith_base, "BillingService")
    svc = read_file(billing_file) if billing_file else ""

    task.results.append(RuleResult(
        "generateProfitabilityReport() reads OrderService",
        contains(svc, r"orderService\.|OrderService"),
        str(billing_file), str(billing_file)
    ))
    task.results.append(RuleResult(
        "generateProfitabilityReport() reads CarrierService",
        contains(svc, r"carrierService\.|CarrierService"),
        str(billing_file), str(billing_file)
    ))
    task.results.append(RuleResult(
        "generateProfitabilityReport() reads CustomsService",
        contains(svc, r"customsService\.|CustomsService"),
        str(billing_file), str(billing_file)
    ))
    task.results.append(RuleResult(
        "generateProfitabilityReport() reads RouteService",
        contains(svc, r"routeService\.|RouteService"),
        str(billing_file), str(billing_file)
    ))
    task.results.append(RuleResult(
        "Method is @Transactional(readOnly=true) — one DB transaction for 4 modules",
        contains(svc, r"readOnly\s*=\s*true"),
        "Report should be a single read transaction across all modules",
        str(billing_file)
    ))
    task.results.append(RuleResult(
        "No HTTP calls (4 modules accessed without network)",
        not contains(svc, r"RestTemplate|WebClient|http://"),
        "Direct calls only — this is the N+1 advantage",
        str(billing_file)
    ))
    return task


TASK_SCORERS = {
    "A1": score_A1, "A2": score_A2, "A4": score_A4,
    "B1": score_B1, "B2": score_B2, "C1": score_C1,
    "D1": score_D1, "D2": score_D2,
}


def print_task(ts: TaskScore):
    print(f"\n  Task {ts.task}: {ts.description} — {ts.score}/{ts.total} ({ts.pct:.0f}%)")
    for r in ts.results:
        icon = "PASS" if r.passed else "FAIL"
        print(f"    [{icon}] {r.rule}")
        if not r.passed:
            print(f"           → {r.detail}")


def main():
    parser = argparse.ArgumentParser(description="ModulithBench Test 2 — Rubric Scorer")
    parser.add_argument("--monolith", required=True, help="Path to monolith directory")
    parser.add_argument("--task", choices=list(TASK_SCORERS.keys()), help="Score a specific task")
    parser.add_argument("--all-tasks", action="store_true", help="Score all implemented tasks")
    args = parser.parse_args()

    base = Path(args.monolith)
    tasks = list(TASK_SCORERS.keys()) if args.all_tasks else ([args.task] if args.task else [])

    if not tasks:
        parser.print_help()
        return

    print(f"\n{'='*60}")
    print(f"Rubric Scorer — {base}")
    print(f"{'='*60}")

    all_passed = 0
    all_total = 0
    for task_id in tasks:
        scorer = TASK_SCORERS[task_id]
        ts = scorer(base)
        print_task(ts)
        all_passed += ts.score
        all_total += ts.total

    if len(tasks) > 1:
        overall = all_passed / all_total * 100 if all_total else 0
        print(f"\n{'─'*60}")
        print(f"OVERALL RUBRIC SCORE: {all_passed}/{all_total} ({overall:.0f}%)")


if __name__ == "__main__":
    main()
