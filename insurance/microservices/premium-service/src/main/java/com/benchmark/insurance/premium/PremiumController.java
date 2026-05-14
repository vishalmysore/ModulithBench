package com.benchmark.insurance.premium;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/premiums")
@RequiredArgsConstructor
public class PremiumController {
    private final PremiumService premiumService;

    @PostMapping
    public ResponseEntity<Premium> create(@RequestBody Premium premium) {
        return ResponseEntity.status(HttpStatus.CREATED).body(premiumService.createPremium(premium));
    }

    @GetMapping("/{premiumId}")
    public ResponseEntity<Premium> getById(@PathVariable Long premiumId) {
        return ResponseEntity.ok(premiumService.getPremiumById(premiumId));
    }

    @PutMapping("/{premiumId}")
    public ResponseEntity<Premium> update(@PathVariable Long premiumId, @RequestBody Premium premium) {
        return ResponseEntity.ok(premiumService.updatePremium(premiumId, premium));
    }

    @DeleteMapping("/{premiumId}")
    public ResponseEntity<Void> delete(@PathVariable Long premiumId) {
        premiumService.deletePremium(premiumId);
        return ResponseEntity.noContent().build();
    }
}