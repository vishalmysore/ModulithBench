package com.benchmark.healthcare.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billings")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<Billing> create(@RequestBody Billing billing) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createBilling(billing));
    }

    @GetMapping("/{billingId}")
    public ResponseEntity<Billing> getById(@PathVariable Long billingId) {
        return ResponseEntity.ok(billingService.getBillingById(billingId));
    }

    @PutMapping("/{billingId}")
    public ResponseEntity<Billing> update(@PathVariable Long billingId, @RequestBody Billing billing) {
        return ResponseEntity.ok(billingService.updateBilling(billingId, billing));
    }

    @DeleteMapping("/{billingId}")
    public ResponseEntity<Void> delete(@PathVariable Long billingId) {
        billingService.deleteBilling(billingId);
        return ResponseEntity.noContent().build();
    }
}