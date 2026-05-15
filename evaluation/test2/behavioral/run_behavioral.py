#!/usr/bin/env python3
"""
Test 2 — Behavioral Test Runner

Calls specific endpoints against a RUNNING monolith and asserts correct behavior.
This tests what the agent actually implemented, not just whether it compiled.

Usage:
    python run_behavioral.py --base-url http://localhost:8080 --domain library
    python run_behavioral.py --base-url http://localhost:8081 --domain healthcare
    python run_behavioral.py --base-url http://localhost:8082 --domain insurance
    python run_behavioral.py --base-url http://localhost:8083 --domain supply-chain
    python run_behavioral.py --all  # runs all 4 domains
"""

import argparse
import json
import sys
import time
from dataclasses import dataclass, field
from typing import Optional
import urllib.request
import urllib.error


@dataclass
class TestResult:
    name: str
    passed: bool
    detail: str
    task: str  # which benchmark task this validates


@dataclass
class DomainResults:
    domain: str
    results: list = field(default_factory=list)

    @property
    def passed(self): return sum(1 for r in self.results if r.passed)
    @property
    def total(self): return len(self.results)
    @property
    def score(self): return (self.passed / self.total * 100) if self.total else 0


def get(url, expect_status=200):
    try:
        with urllib.request.urlopen(url, timeout=5) as resp:
            body = json.loads(resp.read())
            return resp.status, body
    except urllib.error.HTTPError as e:
        return e.code, {}
    except Exception as e:
        return 0, {"error": str(e)}


def post(url, data=None, params=None):
    if params:
        url += "?" + "&".join(f"{k}={v}" for k, v in params.items())
    req = urllib.request.Request(url, method="POST")
    if data:
        req.add_header("Content-Type", "application/json")
        req.data = json.dumps(data).encode()
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            return resp.status, json.loads(resp.read())
    except urllib.error.HTTPError as e:
        return e.code, {}
    except Exception as e:
        return 0, {"error": str(e)}


def patch(url, params=None):
    if params:
        url += "?" + "&".join(f"{k}={v}" for k, v in params.items())
    req = urllib.request.Request(url, method="PATCH")
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            return resp.status, json.loads(resp.read())
    except urllib.error.HTTPError as e:
        return e.code, {}
    except Exception as e:
        return 0, {"error": str(e)}


# ─────────────────────────────────────────────────────────────────────────────
# LIBRARY DOMAIN TESTS
# ─────────────────────────────────────────────────────────────────────────────

def run_library_tests(base: str) -> DomainResults:
    results = DomainResults("library")

    # --- Setup: create book and member ---
    _, book = post(f"{base}/api/v1/books", {
        "title": "Behavioral Test Book", "isbn": "BT-001",
        "author": "Test Author", "totalCopies": 3, "availableCopies": 3
    })
    _, member = post(f"{base}/api/v1/members", {
        "firstName": "Test", "lastName": "Agent",
        "email": "agent.test@benchmark.com"
    })
    book_id = book.get("id")
    member_id = member.get("id")

    if not book_id or not member_id:
        results.results.append(TestResult("Setup", False, "Could not create book or member", "setup"))
        return results

    # --- Test: Loan creation decrements available copies (Task A1 baseline) ---
    status, loan = post(f"{base}/api/v1/loans", params={"bookId": book_id, "memberId": member_id})
    loan_id = loan.get("id")
    results.results.append(TestResult(
        "Loan creation succeeds",
        status == 201 and loan_id is not None,
        f"POST /loans returned {status}, loanId={loan_id}",
        "baseline"
    ))

    _, updated_book = get(f"{base}/api/v1/books/{book_id}")
    results.results.append(TestResult(
        "Loan creation decrements available copies (cross-module atomicity)",
        updated_book.get("availableCopies") == 2,
        f"availableCopies={updated_book.get('availableCopies')}, expected 2",
        "baseline-cross-module"
    ))

    # --- Test: Task A1 — enriched loan history endpoint exists and returns book title ---
    status, history = get(f"{base}/api/v1/loans/member/{member_id}/history")
    has_book_title = (
        status == 200
        and isinstance(history, list)
        and len(history) > 0
        and history[0].get("bookTitle") is not None
    )
    results.results.append(TestResult(
        "Task A1: GET /loans/member/{id}/history returns enriched data with bookTitle",
        has_book_title,
        f"status={status}, response sample={history[0] if history else 'empty'}",
        "A1"
    ))
    results.results.append(TestResult(
        "Task A1: Enriched history includes bookAuthor field",
        status == 200 and bool(history) and history[0].get("bookAuthor") is not None,
        f"bookAuthor={history[0].get('bookAuthor') if history else 'N/A'}",
        "A1"
    ))

    # --- Test: Task A2 — inactive member cannot borrow (validation before decrement) ---
    _, member2 = post(f"{base}/api/v1/members", {
        "firstName": "Inactive", "lastName": "User", "email": "inactive.test@benchmark.com"
    })
    m2_id = member2.get("id")
    if m2_id:
        patch(f"{base}/api/v1/members/{m2_id}/deactivate")
        status, _ = post(f"{base}/api/v1/loans", params={"bookId": book_id, "memberId": m2_id})
        _, book_after = get(f"{base}/api/v1/books/{book_id}")
        results.results.append(TestResult(
            "Task A2: Inactive member loan rejected (status 4xx)",
            status >= 400,
            f"Expected 4xx, got {status}",
            "A2"
        ))
        results.results.append(TestResult(
            "Task A2: Book copies NOT decremented when validation fails (atomicity)",
            book_after.get("availableCopies") == 2,
            f"availableCopies={book_after.get('availableCopies')}, expected unchanged at 2",
            "A2"
        ))

    # --- Test: Task A4 — cannot reserve book already on active loan ---
    status, reservation = post(f"{base}/api/v1/reservations", params={"bookId": book_id, "memberId": member_id})
    results.results.append(TestResult(
        "Task A4: Member cannot reserve book they already have on active loan",
        status >= 400,
        f"Expected 4xx for duplicate borrow+reserve, got {status}",
        "A4"
    ))

    # --- Test: Return book restores copies ---
    if loan_id:
        status, _ = patch(f"{base}/api/v1/loans/{loan_id}/return")
        _, book_returned = get(f"{base}/api/v1/books/{book_id}")
        results.results.append(TestResult(
            "Book return increments available copies (cross-module atomicity)",
            book_returned.get("availableCopies") == 3,
            f"availableCopies={book_returned.get('availableCopies')}, expected 3",
            "baseline-cross-module"
        ))

    return results


# ─────────────────────────────────────────────────────────────────────────────
# HEALTHCARE DOMAIN TESTS
# ─────────────────────────────────────────────────────────────────────────────

def run_healthcare_tests(base: str) -> DomainResults:
    results = DomainResults("healthcare")

    _, patient = post(f"{base}/api/v1/patients", {
        "firstName": "Test", "lastName": "Patient", "email": "pt.behavioral@benchmark.com"
    })
    _, doctor = post(f"{base}/api/v1/doctors", {
        "firstName": "Test", "lastName": "Doctor", "email": "dr.behavioral@benchmark.com",
        "specialization": "General", "available": True
    })
    patient_id = patient.get("id")
    doctor_id = doctor.get("id")

    if not patient_id or not doctor_id:
        results.results.append(TestResult("Setup", False, "Could not create patient or doctor", "setup"))
        return results

    # Schedule appointment
    import urllib.parse
    scheduled_at = "2026-12-01T10:00:00"
    status, appt = post(f"{base}/api/v1/appointments", params={
        "patientId": patient_id, "doctorId": doctor_id, "scheduledAt": scheduled_at
    })
    appt_id = appt.get("id")
    results.results.append(TestResult(
        "Appointment scheduling validates patient+doctor atomically",
        status == 201 and appt_id is not None,
        f"POST /appointments returned {status}",
        "baseline"
    ))

    # Task B1: Doctor workload endpoint
    status, workload = get(f"{base}/api/v1/appointments/doctor/{doctor_id}/workload")
    results.results.append(TestResult(
        "Task B1: GET /appointments/doctor/{id}/workload endpoint exists",
        status == 200,
        f"status={status}",
        "B1"
    ))
    results.results.append(TestResult(
        "Task B1: Workload response includes totalAppointments field",
        status == 200 and workload.get("totalAppointments") is not None,
        f"response keys: {list(workload.keys()) if isinstance(workload, dict) else 'not a dict'}",
        "B1"
    ))

    # Unavailable doctor cannot be scheduled
    _, doctor2 = post(f"{base}/api/v1/doctors", {
        "firstName": "Busy", "lastName": "Doctor", "email": "busy.dr@benchmark.com",
        "specialization": "Cardiology", "available": False
    })
    d2_id = doctor2.get("id")
    if d2_id:
        status, _ = post(f"{base}/api/v1/appointments", params={
            "patientId": patient_id, "doctorId": d2_id, "scheduledAt": "2026-12-02T10:00:00"
        })
        results.results.append(TestResult(
            "Unavailable doctor rejected at scheduling (cross-module validation)",
            status >= 400,
            f"Expected 4xx, got {status}",
            "baseline-cross-module"
        ))

    # Medical record created from appointment copies patient+doctor IDs
    if appt_id:
        status, record = post(f"{base}/api/v1/medical-records/from-appointment", params={
            "appointmentId": appt_id, "diagnosis": "Test diagnosis", "treatment": "Rest"
        })
        results.results.append(TestResult(
            "Medical record from appointment copies patientId (cross-module read)",
            status == 201 and record.get("patientId") == patient_id,
            f"record.patientId={record.get('patientId')}, expected {patient_id}",
            "baseline-cross-module"
        ))

    return results


# ─────────────────────────────────────────────────────────────────────────────
# INSURANCE DOMAIN TESTS
# ─────────────────────────────────────────────────────────────────────────────

def run_insurance_tests(base: str) -> DomainResults:
    results = DomainResults("insurance")

    _, customer = post(f"{base}/api/v1/customers", {
        "firstName": "Test", "lastName": "Customer",
        "email": "ins.behavioral@benchmark.com", "nationalId": "BT-INS-001"
    })
    _, agent = post(f"{base}/api/v1/agents", {
        "firstName": "Test", "lastName": "Agent",
        "email": "agent.ins@benchmark.com", "licenseNumber": "LIC-BT-001"
    })
    cust_id = customer.get("id")
    agent_id = agent.get("id")

    if not cust_id or not agent_id:
        results.results.append(TestResult("Setup", False, "Could not create customer or agent", "setup"))
        return results

    # Create and activate policy
    _, policy = post(f"{base}/api/v1/policies", {
        "customerId": cust_id, "agentId": agent_id, "type": "HEALTH",
        "startDate": "2026-01-01", "endDate": "2026-12-31",
        "premiumAmount": 200.00, "coverageAmount": 50000.00
    })
    policy_id = policy.get("id")
    if policy_id:
        patch(f"{base}/api/v1/policies/{policy_id}/activate")

    results.results.append(TestResult(
        "Policy creation validates customer+agent atomically",
        policy_id is not None,
        f"policyId={policy_id}",
        "baseline"
    ))

    # Wrong customer cannot file claim on another customer's policy
    _, other_customer = post(f"{base}/api/v1/customers", {
        "firstName": "Other", "lastName": "Customer",
        "email": "other.ins@benchmark.com", "nationalId": "BT-INS-002"
    })
    other_id = other_customer.get("id")
    if policy_id and other_id:
        status, _ = post(f"{base}/api/v1/claims", params={
            "policyId": policy_id, "customerId": other_id,
            "type": "MEDICAL", "description": "Unauthorized claim",
            "claimedAmount": 1000.00
        })
        results.results.append(TestResult(
            "Task C2: Claim rejected when customer does not own policy (cross-module ownership check)",
            status >= 400,
            f"Expected 4xx, got {status}",
            "C2"
        ))

    # Valid claim, approve, then settle
    if policy_id:
        status, claim = post(f"{base}/api/v1/claims", params={
            "policyId": policy_id, "customerId": cust_id,
            "type": "MEDICAL", "description": "Behavioral test claim",
            "claimedAmount": 5000.00
        })
        claim_id = claim.get("id")
        results.results.append(TestResult(
            "Valid claim filing succeeds",
            status == 201 and claim_id is not None,
            f"claimId={claim_id}",
            "baseline"
        ))

        if claim_id:
            patch(f"{base}/api/v1/claims/{claim_id}/approve", params={"approvedAmount": 4500.00})
            # Task C1: settlement created automatically (if agent implemented it)
            status, settlements = get(f"{base}/api/v1/settlements/claim/{claim_id}")
            results.results.append(TestResult(
                "Task C1: Settlement auto-created when claim approved",
                status == 200 and isinstance(settlements, list) and len(settlements) > 0,
                f"settlements count={len(settlements) if isinstance(settlements, list) else 'error'}",
                "C1"
            ))

    return results


# ─────────────────────────────────────────────────────────────────────────────
# SUPPLY CHAIN DOMAIN TESTS
# ─────────────────────────────────────────────────────────────────────────────

def run_supply_chain_tests(base: str) -> DomainResults:
    results = DomainResults("supply-chain")

    # Create inventory
    _, inv = post(f"{base}/api/v1/inventory", {
        "sku": "BT-SKU-001", "productName": "Behavioral Test Product",
        "warehouseCode": "WH-TEST", "quantityOnHand": 10, "quantityReserved": 0
    })

    # Create order
    _, order = post(f"{base}/api/v1/orders", {
        "customerId": 1, "destinationAddress": "123 Test St",
        "destinationCountry": "US", "originWarehouse": "WH-TEST",
        "items": "[{\"sku\":\"BT-SKU-001\",\"qty\":2}]"
    })
    order_id = order.get("id")
    results.results.append(TestResult(
        "Order creation succeeds",
        order_id is not None,
        f"orderId={order_id}",
        "baseline"
    ))

    if order_id:
        # Confirm order — reserves inventory
        patch(f"{base}/api/v1/orders/{order_id}/confirm", params={"sku": "BT-SKU-001", "quantity": 2})
        _, inv_after = get(f"{base}/api/v1/inventory/warehouse/WH-TEST")
        inv_record = next((i for i in (inv_after if isinstance(inv_after, list) else []) if i.get("sku") == "BT-SKU-001"), {})
        results.results.append(TestResult(
            "Order confirmation reserves inventory (cross-module atomicity)",
            inv_record.get("quantityReserved") == 2,
            f"quantityReserved={inv_record.get('quantityReserved')}, expected 2",
            "baseline-cross-module"
        ))

        # THE GHOST SHIPMENT TEST: cancel order — must release inventory atomically
        status, cancelled = patch(f"{base}/api/v1/orders/{order_id}/cancel",
                                   params={"sku": "BT-SKU-001", "quantity": 2})
        results.results.append(TestResult(
            "Order cancellation succeeds",
            status == 200 and cancelled.get("status") == "CANCELLED",
            f"status={status}, orderStatus={cancelled.get('status')}",
            "D1"
        ))

        _, inv_restored = get(f"{base}/api/v1/inventory/warehouse/WH-TEST")
        inv_record2 = next((i for i in (inv_restored if isinstance(inv_restored, list) else []) if i.get("sku") == "BT-SKU-001"), {})
        results.results.append(TestResult(
            "Task D1 (Ghost Shipment): Inventory reservation released atomically on cancel",
            inv_record2.get("quantityReserved") == 0,
            f"quantityReserved={inv_record2.get('quantityReserved')}, expected 0 — Ghost Shipment prevention",
            "D1"
        ))

    # Profitability report endpoint exists (Task D2)
    if order_id:
        status, report = get(f"{base}/api/v1/billing/report/profitability/{order_id}")
        results.results.append(TestResult(
            "Task D2: GET /billing/report/profitability/{orderId} endpoint exists",
            status == 200,
            f"status={status}",
            "D2"
        ))
        results.results.append(TestResult(
            "Task D2: Report includes revenue field (from Order module)",
            status == 200 and "revenue" in report,
            f"fields present: {list(report.keys()) if isinstance(report, dict) else 'error'}",
            "D2"
        ))

    return results


# ─────────────────────────────────────────────────────────────────────────────
# RUNNER
# ─────────────────────────────────────────────────────────────────────────────

DOMAIN_MAP = {
    "library":      (run_library_tests,      "http://localhost:8080"),
    "healthcare":   (run_healthcare_tests,   "http://localhost:8081"),
    "insurance":    (run_insurance_tests,    "http://localhost:8082"),
    "supply-chain": (run_supply_chain_tests, "http://localhost:8083"),
}


def print_results(domain_results: DomainResults):
    print(f"\n{'='*60}")
    print(f"Domain: {domain_results.domain.upper()}")
    print(f"{'='*60}")
    for r in domain_results.results:
        icon = "PASS" if r.passed else "FAIL"
        print(f"  [{icon}] [{r.task}] {r.name}")
        if not r.passed:
            print(f"         → {r.detail}")
    print(f"\nScore: {domain_results.passed}/{domain_results.total} ({domain_results.score:.1f}%)")


def main():
    parser = argparse.ArgumentParser(description="ModulithBench Test 2 — Behavioral Tests")
    parser.add_argument("--domain", choices=list(DOMAIN_MAP.keys()), help="Domain to test")
    parser.add_argument("--base-url", help="Base URL of running monolith (overrides default)")
    parser.add_argument("--all", action="store_true", help="Run all domains")
    parser.add_argument("--output", help="Write results to this file (markdown)")
    args = parser.parse_args()

    domains_to_run = list(DOMAIN_MAP.keys()) if args.all else ([args.domain] if args.domain else [])
    if not domains_to_run:
        parser.print_help()
        sys.exit(1)

    all_results = []
    for domain in domains_to_run:
        fn, default_url = DOMAIN_MAP[domain]
        url = args.base_url if args.base_url else default_url
        print(f"\nConnecting to {url}...")
        try:
            code, _ = get(f"{url}/actuator/health")
            if code != 200:
                print(f"  WARNING: Health check returned {code}. Service may not be ready.")
        except Exception as e:
            print(f"  ERROR: Cannot reach {url} — {e}. Is the service running?")
            continue

        r = fn(url)
        print_results(r)
        all_results.append(r)

    if all_results:
        total_passed = sum(r.passed for r in all_results)
        total_tests = sum(r.total for r in all_results)
        overall = total_passed / total_tests * 100 if total_tests else 0
        print(f"\n{'='*60}")
        print(f"OVERALL: {total_passed}/{total_tests} ({overall:.1f}%)")
        print(f"{'='*60}")

        if args.output:
            write_markdown(all_results, args.output)
            print(f"\nResults written to {args.output}")


def write_markdown(results_list, path):
    lines = ["# Test 2 — Behavioral Test Results\n"]
    for r in results_list:
        lines.append(f"## {r.domain.title()}: {r.passed}/{r.total} ({r.score:.1f}%)\n")
        lines.append("| Result | Task | Test | Detail |")
        lines.append("|--------|------|------|--------|")
        for t in r.results:
            icon = "✅" if t.passed else "❌"
            lines.append(f"| {icon} | {t.task} | {t.name} | {t.detail if not t.passed else ''} |")
        lines.append("")
    with open(path, "w") as f:
        f.write("\n".join(lines))


if __name__ == "__main__":
    main()
