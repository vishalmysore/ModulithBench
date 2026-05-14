package com.benchmark.supplychain.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/invoice")
    public ResponseEntity<Invoice> generateInvoice(@RequestParam Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.generateInvoice(orderId));
    }

    @GetMapping("/invoice/{id}")
    public ResponseEntity<Invoice> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getById(id));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getAll() {
        return ResponseEntity.ok(billingService.getAll());
    }

    @GetMapping("/invoices/customer/{customerId}")
    public ResponseEntity<List<Invoice>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(billingService.getByCustomer(customerId));
    }

    /**
     * THE N+1 REPORT ENDPOINT
     * Reads from 4 modules (Order + Carrier + Customs + Route) in one transaction.
     * Compare the implementation here vs the microservices version.
     */
    @GetMapping("/report/profitability/{orderId}")
    public ResponseEntity<ShipmentProfitabilityReport> getProfitabilityReport(@PathVariable Long orderId) {
        return ResponseEntity.ok(billingService.generateProfitabilityReport(orderId));
    }

    @PatchMapping("/invoice/{id}/pay")
    public ResponseEntity<Invoice> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.markPaid(id));
    }
}
