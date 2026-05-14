package com.benchmark.insurance.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<Settlement> create(@RequestBody Settlement settlement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementService.createSettlement(settlement));
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<Settlement> getById(@PathVariable Long settlementId) {
        return ResponseEntity.ok(settlementService.getSettlementById(settlementId));
    }

    @PutMapping("/{settlementId}")
    public ResponseEntity<Settlement> update(@PathVariable Long settlementId, @RequestBody Settlement settlement) {
        return ResponseEntity.ok(settlementService.updateSettlement(settlementId, settlement));
    }

    @DeleteMapping("/{settlementId}")
    public ResponseEntity<Void> delete(@PathVariable Long settlementId) {
        settlementService.deleteSettlement(settlementId);
        return ResponseEntity.noContent().build();
    }
}