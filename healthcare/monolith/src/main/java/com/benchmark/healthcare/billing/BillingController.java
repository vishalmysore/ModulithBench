package com.benchmark.healthcare.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billings")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/from-appointment")
    public ResponseEntity<Billing> createFromAppointment(
            @RequestParam Long appointmentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.createBillingForAppointment(appointmentId, amount, description));
    }

    @PostMapping
    public ResponseEntity<Billing> create(@RequestBody Billing billing) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createBilling(billing));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Billing> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getBillingById(id));
    }

    @GetMapping
    public ResponseEntity<List<Billing>> getAll() {
        return ResponseEntity.ok(billingService.getAllBillings());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Billing>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getBillingsByPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/outstanding")
    public ResponseEntity<BigDecimal> getOutstanding(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getOutstandingBalance(patientId));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Billing> pay(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(billingService.recordPayment(id, amount));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Billing> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.cancelBilling(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billingService.deleteBilling(id);
        return ResponseEntity.noContent().build();
    }
}
