package com.benchmark.insurance.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/settle-claim")
    public ResponseEntity<Settlement> settleClaim(
            @RequestParam Long claimId,
            @RequestParam(required = false, defaultValue = "BANK_TRANSFER") String paymentMethod,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.settleApprovedClaim(claimId, paymentMethod, notes));
    }

    @PostMapping
    public ResponseEntity<Settlement> create(@RequestBody Settlement settlement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementService.createSettlement(settlement));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Settlement> getById(@PathVariable Long id) {
        return ResponseEntity.ok(settlementService.getSettlementById(id));
    }

    @GetMapping
    public ResponseEntity<List<Settlement>> getAll() {
        return ResponseEntity.ok(settlementService.getAllSettlements());
    }

    @GetMapping("/claim/{claimId}")
    public ResponseEntity<List<Settlement>> getByClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(settlementService.getSettlementsByClaim(claimId));
    }

    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<Settlement> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(settlementService.markPaid(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Settlement> update(@PathVariable Long id,
                                              @RequestParam BigDecimal amount,
                                              @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(settlementService.updateSettlement(id, amount, notes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        settlementService.deleteSettlement(id);
        return ResponseEntity.noContent().build();
    }
}
