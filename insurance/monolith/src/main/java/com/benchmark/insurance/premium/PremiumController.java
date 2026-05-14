package com.benchmark.insurance.premium;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/premiums")
@RequiredArgsConstructor
public class PremiumController {

    private final PremiumService premiumService;

    @PostMapping("/schedule")
    public ResponseEntity<Premium> schedule(
            @RequestParam Long policyId,
            @RequestParam PremiumFrequency frequency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(premiumService.schedulePremium(policyId, frequency, dueDate));
    }

    @PostMapping
    public ResponseEntity<Premium> create(@RequestBody Premium premium) {
        return ResponseEntity.status(HttpStatus.CREATED).body(premiumService.createPremium(premium));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Premium> getById(@PathVariable Long id) {
        return ResponseEntity.ok(premiumService.getPremiumById(id));
    }

    @GetMapping
    public ResponseEntity<List<Premium>> getAll() {
        return ResponseEntity.ok(premiumService.getAllPremiums());
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<Premium>> getByPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(premiumService.getPremiumsByPolicy(policyId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Premium>> getOverdue() {
        return ResponseEntity.ok(premiumService.getOverduePremiums());
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Premium> pay(@PathVariable Long id) {
        return ResponseEntity.ok(premiumService.recordPayment(id));
    }

    @PostMapping("/mark-overdue")
    public ResponseEntity<List<Premium>> markOverdue() {
        return ResponseEntity.ok(premiumService.markOverdue());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        premiumService.deletePremium(id);
        return ResponseEntity.noContent().build();
    }
}
